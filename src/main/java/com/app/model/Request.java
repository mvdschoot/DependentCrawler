package com.app.model;

import java.util.List;

import com.app.services.Requester.RequestType;

public class Request {
    public static class HttpRequest {
        public String purl;
        public int page;
        public int size;
        public String searchTerm;
        public List<String> filter;

        public HttpRequest(Identifier id) {
            purl = new StringBuilder("pkg:maven/")
                    .append(id.groupId)
                    .append("/")
                    .append(id.artifactId)
                    .append("@")
                    .append(id.version)
                    .toString();

            page = 0;
            size = 20;
            searchTerm = "";
            filter = List.of("dependencyRef:DIRECT");
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
}