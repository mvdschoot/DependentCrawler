package com.app.model;

import java.util.List;

public class DependentRequest {
    public String purl;
    public int page;
    public int size;
    public String searchTerm;
    public List<String> filter;

    public DependentRequest(String group, String artifact, String version) {
        purl = new StringBuilder("pkg:maven/")
                .append(group)
                .append("/")
                .append(artifact)
                .append("@")
                .append(version)
                .toString();

        page = 0;
        size = 20;
        searchTerm = "";
        filter = List.of("dependencyRef:DIRECT");
    }
}
