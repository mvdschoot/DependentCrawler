package com.app.model;

import java.util.List;

public class DepRequest {
    public String purl;
    public int page;
    public int size;
    public String searchTerm;
    public List<String> filter;

    public DepRequest(Identifier id) {
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
