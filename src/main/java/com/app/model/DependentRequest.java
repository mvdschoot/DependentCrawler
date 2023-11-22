package com.app.model;

import java.util.List;

public class DependentRequest {
    private String purl;
    private int page;
    private int size;
    private String searchTerm;
    List<String> filter;

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

    public String getPurl() {
        return purl;
    }
    public void setPurl(String purl) {
        this.purl = purl;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public String getSearchTerm() {
        return searchTerm;
    }
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
    public List<String> getFilter() {
        return filter;
    }
    public void setFilter(List<String> filter) {
        this.filter = filter;
    }    
}
