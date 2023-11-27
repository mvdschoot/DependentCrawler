package com.app.model;

import java.util.List;

import com.app.model.GeneralResponseTypes.BomDrInfo;
import com.app.model.GeneralResponseTypes.OssIndexInfo;

public class DependencyResponse {
    public String sourcePurl;
    public String dependencyPurl;
    public String dependencyType;
    public String dependencyRef;
    public String scope;
    public String sourceNamespace;
    public String sourceName;
    public String sourceVersion;
    public String sourcePackaging;
    public OssIndexInfo sourceOssIndexInfo;
    public BomDrInfo sourceBomDrInfo;
    public String dependencyNamespace;
    public String dependencyName;
    public String dependencyVersion;
    public String dependencyPackaging;
    public String dependencyClassifier;
    public OssIndexInfo dependencyOssIndexInfo;
    public BomDrInfo dependencyBomDrInfo;
    public String description;
    public int childCount;
    public boolean ingested;
    public List<String> licenses;
}
