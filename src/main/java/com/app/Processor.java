package com.app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.nio.graphml.GraphMLExporter.AttributeCategory;

import com.app.Request.RequestType;
import com.app.model.Identifier;
import com.app.model.Library;
import com.app.model.DepResponse.DepResult;

public class Processor {
    public enum TraverseType {
        DOWN, UP, BOTH
    }

    private TraverseType traverseType;

    private Graph<Library, DefaultEdge> graph;
    private Map<Identifier, Library> depStore;

    public Processor(TraverseType traverseType) {
        this.traverseType = traverseType;
        
        graph = new DirectedMultigraph<>(DefaultEdge.class);
        depStore = new HashMap<>();
    }

    public void start(Identifier initial) throws IOException {
        Queue<Identifier> queue = new LinkedList<>();
        queue.add(initial);

        if (traverseType == TraverseType.DOWN) {
            traverse(queue, RequestType.DEPENDENCY);
        } else if (traverseType == TraverseType.UP) {
            traverse(queue, RequestType.SOURCE);
        } else {
            traverse(queue, RequestType.SOURCE);
            traverse(new LinkedList<>(queue), RequestType.DEPENDENCY);
        }
    }

    public void store(String location) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(location));

        GraphMLExporter<Library, DefaultEdge> exporter = new GraphMLExporter<>();
        exporter.setVertexAttributeProvider((l) -> l.toExportFormat());
        Library.registerAttributes(exporter);
        exporter.exportGraph(graph, writer);

        writer.flush();
    }

    private void traverse(Queue<Identifier> queue, RequestType requestType) throws IOException {
        Identifier current = queue.peek();
        List<DepResult> results = Request.getDep(requestType, current);

        // Insert the initial library
        if (!results.isEmpty()) {
            Library toInstert = new Library(RequestType.DEPENDENCY == requestType ? RequestType.SOURCE : RequestType.DEPENDENCY, results.get(0));
            if (!depStore.containsKey(current)) {
                graph.addVertex(toInstert);
                depStore.put(current, toInstert);
            }
        } else {
            return;
        }

        while (!queue.isEmpty()) {
            insert(queue, requestType);
        }
    }

    private void insert(Queue<Identifier> queue, RequestType requestType) throws IOException {
        Identifier current = queue.poll();
        List<DepResult> results = Request.getDep(requestType, current);

        for (DepResult dep : results) {
            Library toInsert = new Library(requestType, dep);
    
            if (!depStore.containsKey(toInsert.getIdentifier())) {
                queue.add(toInsert.getIdentifier());
                graph.addVertex(toInsert);
                depStore.put(toInsert.getIdentifier(), toInsert);
    
                if (requestType == RequestType.DEPENDENCY) {
                    graph.addEdge(depStore.get(current), toInsert);
                } else {
                    graph.addEdge(toInsert, depStore.get(current));
                }
            } else {
                if (requestType == RequestType.DEPENDENCY) {
                    graph.addEdge(depStore.get(current), depStore.get(toInsert.getIdentifier()));
                } else {
                    graph.addEdge(depStore.get(toInsert.getIdentifier()), depStore.get(current));
                }
            }
        }
    }
}
