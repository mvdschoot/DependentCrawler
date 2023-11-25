package com.app.model;

import java.util.List;

public class HttpResponse {
    public static class HttpContent {
        public String sourcePurl;
        public String dependencyPurl;
        public String dependencyType;
        public String dependencyRef;
        public String scope;
        public String sourceNamespace;
        public String sourceName;
        public String sourceVersion;
        public String sourcePackaging;
        public SourceOssIndexInfo sourceOssIndexInfo;
        public SourceBomDrInfo sourceBomDrInfo;
        public String dependencyNamespace;
        public String dependencyName;
        public String dependencyVersion;
        public String dependencyPackaging;
        public String dependencyClassifier;
        public DependencyOssIndexInfo dependencyOssIndexInfo;
        public DependencyBomDrInfo dependencyBomDrInfo;
        public String description;
        public int childCount;
        public boolean ingested;
        public List<String> licenses;
    }

    public static class DependencyBomDrInfo{
        public String url;
    }

    public static class DependencyOssIndexInfo{
        public String url;
        public Integer vulnerabilityCount;
    }

    public static class SourceBomDrInfo{
        public String url;
    }

    public static class SourceOssIndexInfo{
        public String url;
        public Integer vulnerabilityCount;
    }


    public List<HttpContent> components;
    public int page;
    public int pageSize;
    public int pageCount;
    public int totalResultCount;
    public int totalCount;
}
