package com.app.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.app.exceptions.TooManyRequestsException;
import com.app.model.Edge;
import com.app.model.Identifier;
import com.app.model.Library;
import com.app.model.ProcessResult;
import com.app.model.HttpResponse.HttpContent;
import com.app.model.Request.SimpleRequest;
import com.app.services.IO.PartialStorage;
import com.app.services.ProcessThread.ExitSignal;
import com.app.services.Requester.RequestType;

public class Processor {
    public static enum TraverseType {
        DOWN, UP, BOTH
    }

    private TraverseType traverseType;

    private Graph<Library, Edge> graph;
    private Map<Identifier, Library> depStore;
    private ConcurrentLinkedQueue<ProcessResult> resultQueue;
    private ConcurrentLinkedQueue<SimpleRequest> requestQueue;
    private List<Thread> threads;
    private ExitSignal exitSignal;

    public Processor(TraverseType traverseType) {
        this.traverseType = traverseType;
        
        graph = new DefaultDirectedGraph<>(Edge.class);
        depStore = new HashMap<>();
        resultQueue = new ConcurrentLinkedQueue<>();
        requestQueue = new ConcurrentLinkedQueue<>();
        exitSignal = new ExitSignal();
    }

    public void start(PartialStorage partialStorage, int threadNo) throws IOException, InterruptedException, TooManyRequestsException {
        graph = partialStorage.graph;
        requestQueue = new ConcurrentLinkedQueue<SimpleRequest>(partialStorage.requestQueue);
        depStore = partialStorage.depStore;

        startThreads(threadNo);
        mainLoop();
        exitSignal.signalNow();
        threads.forEach(Thread::interrupt);
    }

    public void start(Identifier initial, int threadNo) throws IOException, InterruptedException, TooManyRequestsException {
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

    public void store(String location) throws IOException {
        IO.storeGraphML(graph, location);
    }

    private void mainLoop() throws IOException {
        long time = System.currentTimeMillis();
        while ((System.currentTimeMillis() - time) < 10000) {
            if (!resultQueue.isEmpty()) {
                time = System.currentTimeMillis();
                insertResult(resultQueue.poll());
            } else if (!exitSignal.shouldContinue()) {
                IO.storeGraphMLpartially(new PartialStorage(graph, requestQueue, depStore));
                break;
            }
        }
    }

    private void initializeNew(SimpleRequest initial) throws IOException, TooManyRequestsException {
        List<HttpContent> results = Requester.getDep(initial);

        if (results.isEmpty()) {
            return;
        }

        // Insert the initial library
        Library toInstert = new Library(RequestType.DEPENDENCY == initial.requestType ? RequestType.SOURCE : RequestType.DEPENDENCY, results.get(0));
        if (!depStore.containsKey(initial.identifier)) {
            graph.addVertex(toInstert);
            depStore.put(initial.identifier, toInstert);
        }
        ProcessResult resultObject = new ProcessResult();
        resultObject.from = toInstert.getIdentifier();
        resultObject.result = results;
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

    private void insertResult(ProcessResult result) throws IOException {
        for (HttpContent dep : result.result) {
            Library toInsert = new Library(result.type, dep);
    
            if (!depStore.containsKey(toInsert.getIdentifier())) {
                requestQueue.add(new SimpleRequest(result.type, toInsert.getIdentifier()));
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
