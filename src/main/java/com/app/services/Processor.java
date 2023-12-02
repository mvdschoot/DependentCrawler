package com.app.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.app.exceptions.MissingDependencyException;
import com.app.exceptions.TooManyRequestsException;
import com.app.model.DependencyResponse;
import com.app.model.Edge;
import com.app.model.Identifier;
import com.app.model.Library;
import com.app.model.ProcessResult;
import com.app.model.VersionResponse;
import com.app.model.Requests.SimpleRequest;
import com.app.services.IO.PartialStorage;
import com.app.services.ProcessThread.ExitSignal;
import com.app.utils.LibraryStatus;
import com.app.utils.RequestType;

public class Processor {
    public static enum TraverseType {
        DOWN, UP, BOTH
    }

    private Graph<Library, Edge> graph;
    private Map<Identifier, Library> depStore;
    private ConcurrentLinkedQueue<ProcessResult<?>> resultQueue;
    private ConcurrentLinkedQueue<SimpleRequest> requestQueue;
    private List<Thread> threads;
    private ExitSignal exitSignal;

    public Processor(TraverseType traverseType, int threadNo, Identifier initial) throws IOException, TooManyRequestsException {
        graph = new DefaultDirectedGraph<>(Edge.class);
        depStore = new HashMap<>();
        resultQueue = new ConcurrentLinkedQueue<>();
        requestQueue = new ConcurrentLinkedQueue<>();
        exitSignal = new ExitSignal();

        next(traverseType, threadNo, initial);
    }

    /**
     * Constructor which picks up from a partial storage.
     * 
     * @param partialStorage The stored data.
     * @param threadNo Number of threads to spawn.
     * @throws IOException In case the partial storage in {@link mainLoop} fails.
     */
    public Processor(PartialStorage partialStorage, int threadNo) throws IOException {
        resultQueue = new ConcurrentLinkedQueue<>();
        exitSignal = new ExitSignal();

        graph = partialStorage.graph;
        requestQueue = new ConcurrentLinkedQueue<SimpleRequest>(partialStorage.requestQueue);
        depStore = partialStorage.depStore;

        startThreads(threadNo);
        mainLoop();
        exitSignal.signalNow();
        threads.forEach(Thread::interrupt);
    }

    public void next(TraverseType traverseType, int threadNo, Identifier initial) throws IOException, TooManyRequestsException {
        if (traverseType == TraverseType.DOWN) {
            initializeNew(new SimpleRequest(RequestType.DEPENDENCY, initial));
        } else if (traverseType == TraverseType.UP) {
            initializeNew(new SimpleRequest(RequestType.SOURCE, initial));
        } else {
            initializeNew(new SimpleRequest(RequestType.SOURCE, initial));
            initializeNew(new SimpleRequest(RequestType.DEPENDENCY, initial));
        }
        startThreads(threadNo);
        mainLoop();
        exitSignal.signalNow();
        threads.forEach(Thread::interrupt);
    }

    public void store(String fileName) throws IOException {
        IO.storeGraphML(graph, fileName);
    }

    /**
     * Main thread loop. Responsible for insertion & storing partial data in case of {@link TooManyRequestsException}.
     * 
     * @throws IOException When partial storing fails.
     */
    private void mainLoop() throws IOException {
        long time = System.currentTimeMillis();
        while ((System.currentTimeMillis() - time) < 5000) {
            if (!resultQueue.isEmpty()) {
                time = System.currentTimeMillis();
                insertResult(resultQueue.poll());
            } else if (!exitSignal.shouldContinue()) {
                depStore.values().stream()
                        .filter(l -> l.getStatus() == null)
                        .forEach(l -> {
                            l.setMissing();
                            l.setStatus(LibraryStatus.PARTIAL);
                        });
                IO.storeGraphMLpartially(new PartialStorage(graph, requestQueue, depStore));
                break;
            }
        }
    }

    /**
     * Requests & inserts the initial package & its children.
     * 
     * @param initial The initial package.
     * @throws IOException In case of a HTTP error.
     * @throws TooManyRequestsException Very unlikely, because this method only does the first request.
     */
    private void initializeNew(SimpleRequest initial) throws IOException, TooManyRequestsException {
        List<DependencyResponse> libraryResults = Requester.getLibrary(initial);

        if (libraryResults.isEmpty()) {
            return;
        }

        // Insert the initial library
        Library toInstert = new Library(RequestType.DEPENDENCY == initial.requestType ? RequestType.SOURCE : RequestType.DEPENDENCY, libraryResults.get(0));
        if (!depStore.containsKey(initial.identifier)) {
            try {
                toInstert.addVersionInformation(Requester.getVersionInformation(toInstert.getIdentifier()));
            } catch (MissingDependencyException exception) {
                return;
            }
            graph.addVertex(toInstert);
            depStore.put(initial.identifier, toInstert);
        }
        ProcessResult<DependencyResponse> resultObject = new ProcessResult<>();
        resultObject.from = toInstert.getIdentifier();
        resultObject.result = libraryResults;
        resultObject.type = initial.requestType;
        insertResult(resultObject);
    }

    private void startThreads(int threadNo) {
        // Spin up the threads
        threads = new ArrayList<>(threadNo);
        for (int x = 0; x < threadNo; x++) {
            Thread t = new ProcessThread(requestQueue, resultQueue, exitSignal);
            t.start();
            threads.add(t);
        }
    }

    /**
     * Inserts request results from the threads into the storage and graph. Also adds all children to the requestQueue.
     * 
     * @param result The result.
     */
    private void insertResult(ProcessResult<?> result) {
        if (RequestType.VERSION == result.type) {
            if (LibraryStatus.MISSING == result.status) {
                depStore.get(result.from).setMissing();
                return;
            }

            VersionResponse versionResult = ((List<VersionResponse>) result.result).get(0);
            depStore.get(result.from).addVersionInformation(versionResult);
        } else {
            List<DependencyResponse> dependencyResult = (List<DependencyResponse>) result.result;
            for (DependencyResponse dep : dependencyResult) {
                Library toInsert = new Library(result.type, dep);
        
                if (!depStore.containsKey(toInsert.getIdentifier())) {
                    requestQueue.add(new SimpleRequest(result.type, toInsert.getIdentifier()));
                    requestQueue.add(new SimpleRequest(RequestType.VERSION, toInsert.getIdentifier()));
                    graph.addVertex(toInsert);
                    depStore.put(toInsert.getIdentifier(), toInsert);
        
                    if (result.type == RequestType.DEPENDENCY) {
                        graph.addEdge(depStore.get(result.from), toInsert, new Edge(dep));
                    } else {
                        graph.addEdge(toInsert, depStore.get(result.from), new Edge(dep));
                    }
                } else {
                    if (result.type == RequestType.DEPENDENCY) {
                        graph.addEdge(depStore.get(result.from), depStore.get(toInsert.getIdentifier()), new Edge(dep));
                    } else {
                        graph.addEdge(depStore.get(toInsert.getIdentifier()), depStore.get(result.from), new Edge(dep));
                    }
                }
            }
        }
    }
}
