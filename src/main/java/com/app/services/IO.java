package com.app.services;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.Map;
import java.util.Queue;

import org.jgrapht.Graph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.nio.graphml.GraphMLExporter.AttributeCategory;

import com.app.model.Edge;
import com.app.model.Identifier;
import com.app.model.Library;
import com.app.model.Request.SimpleRequest;

public class IO {
    public static class PartialStorage {
        public Graph<Library, Edge> graph;
        public Queue<SimpleRequest> requestQueue;
        public Map<Identifier, Library> depStore;

        public PartialStorage(Graph<Library, Edge> graph, Queue<SimpleRequest> requestQueue, Map<Identifier, Library> depStore) {
            this.graph = graph;
            this.requestQueue = requestQueue;
            this.depStore = depStore;
        }
    }

    public static String vertexNameProvider(Library library) {
        return library.getIdentifier().toString();
    }

    // public static String edgeNameProvider(DependentOn edge) {
    //     return 
    // }

    public static Map<String, Attribute> vertexExporter(Library library) {
        return Map.of(
            "Id", DefaultAttribute.createAttribute(library.getIdentifier().toString()),
            "GroupId", DefaultAttribute.createAttribute(library.getIdentifier().groupId),
            "ArtifactId", DefaultAttribute.createAttribute(library.getIdentifier().artifactId),
            "Version", DefaultAttribute.createAttribute(library.getIdentifier().version),
            "Scope", DefaultAttribute.createAttribute(library.getScope()),
            "Licenses", DefaultAttribute.createAttribute(String.join(";", library.getLicenses())),
            "VulnerabilityCount", DefaultAttribute.createAttribute(library.getVulnerabilityCount())
        );
    }

    public static void registerVertexAttributes(GraphMLExporter<Library, Edge> exporter) {
        exporter.registerAttribute("Id", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("GroupId", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("ArtifactId", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("Version", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("Scope", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("Licenses", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("VulnerabilityCount", AttributeCategory.NODE, AttributeType.INT);
    }

    public static Map<String, Attribute> edgeExporter(Edge edge) {
        return Map.of(
            "Relation", DefaultAttribute.createAttribute(edge.getScope())
        );
    }

    public static void registerEdgeAttributes(GraphMLExporter<Library, Edge> exporter) {
        exporter.registerAttribute("Relation", AttributeCategory.EDGE, AttributeType.STRING);
    }

    public static void storeGraphML(Graph<Library, Edge> graph, String location) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter("results/" + location));

        GraphMLExporter<Library, Edge> exporter = new GraphMLExporter<>((l) -> vertexNameProvider(l));
        exporter.setVertexAttributeProvider((l) -> vertexExporter(l));
        registerVertexAttributes(exporter);
        exporter.setEdgeAttributeProvider((e) -> edgeExporter(e));
        registerEdgeAttributes(exporter);

        try {
            exporter.exportGraph(graph, writer);
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        writer.flush();
    }

    /*
     * Result queue not included, should be empty.
     */
    public static void storeGraphMLpartially(PartialStorage variables) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("results/graph.tmp");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(variables.graph);
        objectOutputStream.close();

        fileOutputStream = new FileOutputStream("results/requestQueue.tmp");
        objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(variables.requestQueue);
        objectOutputStream.close();

        fileOutputStream = new FileOutputStream("results/depStore.tmp");
        objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(variables.depStore);
        objectOutputStream.close();
    }

    public static PartialStorage loadPartialStorage() throws IOException {
        try {
            FileInputStream fileInputStream = new FileInputStream("results/graph.tmp");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Graph<Library, Edge> graph = (Graph<Library, Edge>) objectInputStream.readObject();
            objectInputStream.close();

            fileInputStream = new FileInputStream("results/requestQueue.tmp");
            objectInputStream = new ObjectInputStream(fileInputStream);
            Queue<SimpleRequest> requestQueue = (Queue<SimpleRequest>) objectInputStream.readObject();
            objectInputStream.close();

            fileInputStream = new FileInputStream("results/depStore.tmp");
            objectInputStream = new ObjectInputStream(fileInputStream);
            Map<Identifier, Library> depStore = (Map<Identifier, Library>) objectInputStream.readObject();
            objectInputStream.close();

            return new PartialStorage(graph, requestQueue, depStore);
        } catch (ClassNotFoundException e) {
            System.out.println("Loading failed");
            e.printStackTrace();
            throw new IOException();
        }
    }
}
