package com.app.model;

import java.util.List;

import com.app.model.GeneralResponseTypes.BomDrInfo;
import com.app.model.GeneralResponseTypes.OssIndexInfo;

public class VersionResponse {
    public String id;
    public String type;
    public String namespace;
    public String name;
    public String version;
    public Long publishedEpochMillis;
    public List<String> licenses;
    public String description;
    public List<String> categories;
    public LatestVersionInfo latestVersionInfo;
    public List<String> contributors;
    public Integer nsPopularityAppCount;
    public Integer nsPopularityOrgCount;
    public int dependentOnCount;
    public int dependencyOfCount;
    public int childCount;
    public List<String> classifiers;
    public String packaging;
    public OssIndexInfo ossIndexInfo;
    public String qualityScore;
    public BomDrInfo bomDrInfo;
    public String cherryBomUrl;

    public class LatestVersionInfo{
        public String version;
        public Long timestampUnixWithMS;
        public List<String> licenses;
    }
}
