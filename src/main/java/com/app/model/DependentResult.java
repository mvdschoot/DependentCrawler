package com.app.model;

import java.util.ArrayList;
import java.util.List;

public class DependentResult {
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

class DependencyBomDrInfo{
    public String url;
}

class DependencyOssIndexInfo{
    public String url;
    public Integer vulnerabilityCount;
}

class SourceBomDrInfo{
    public String url;
}

class SourceOssIndexInfo{
    public String url;
    public Integer vulnerabilityCount;
}
