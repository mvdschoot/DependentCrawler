package com.app.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;

import com.app.utils.RequestType;

public class Requests {
    public static class DependencyRequest {
        public String purl;
        public int page;
        public int size;
        public String searchTerm;
        public List<String> filter;

        public DependencyRequest(Identifier id) {
            purl = createPurl(id);
            page = 0;
            size = 20;
            searchTerm = "";
            filter = List.of("dependencyRef:DIRECT");
        }
    }

    public static class VersionRequest {
        public int page;
        public int size;
        public String filter;

        public VersionRequest(Identifier identifier) {
            page = 0;
            size = 20;
            filter = "id:" + createPurl(identifier);
        }

        public URI encode(String baseUrl) {
            try {
                return new URIBuilder(baseUrl)
                        .addParameter("page", Integer.toString(page))
                        .addParameter("size", Integer.toString(size))
                        .addParameter("filter", filter)
                        .build();
            } catch (URISyntaxException e) {
                System.out.println("encoding error");
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class SimpleRequest {
        public RequestType requestType;
        public Identifier identifier;

        public SimpleRequest(RequestType requestType, Identifier identifier) {
            this.requestType = requestType;
            this.identifier = identifier;
        }
    }

    public static String createPurl(Identifier id) {
        return new StringBuilder("pkg:maven/")
                .append(id.groupId)
                .append("/")
                .append(id.artifactId)
                .append("@")
                .append(id.version)
                .toString();
    }
}