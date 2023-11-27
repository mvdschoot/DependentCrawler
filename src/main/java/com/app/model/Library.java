package com.app.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.app.utils.LibraryStatus;
import com.app.utils.RequestType;

public class Library implements Serializable {
    private Identifier identifier;
    private String scope;
    private List<String> licenses;
    private Integer vulnerabilityCount;
    private List<String> categories;
    private Integer dependencyCount;
    private Integer dependentCount;
    private Integer popularityAppCount;
    private Integer popularityOrgCount;
    private String releaseTime;
    private LibraryStatus status;

    public Library(RequestType requestType, DependencyResponse dependencyResult) {
        if (RequestType.SOURCE == requestType) {
            identifier = new Identifier(dependencyResult.sourceNamespace, dependencyResult.sourceName, dependencyResult.sourceVersion);
            vulnerabilityCount = dependencyResult.sourceOssIndexInfo.vulnerabilityCount;
        } else {
            identifier = new Identifier(dependencyResult.dependencyNamespace, dependencyResult.dependencyName, dependencyResult.dependencyVersion);
            vulnerabilityCount = dependencyResult.dependencyOssIndexInfo.vulnerabilityCount;
        }
    
        scope = dependencyResult.scope == null ? "" : dependencyResult.scope;
        licenses = dependencyResult.licenses == null || dependencyResult.licenses.isEmpty() ? List.of("") : dependencyResult.licenses;

        if (vulnerabilityCount == null) {
            vulnerabilityCount = 0;
        }
    }

    public void addVersionInformation(VersionResponse versionInformation) {
        categories = Objects.requireNonNullElse(versionInformation.categories, List.of());
        dependencyCount = Objects.requireNonNullElse(versionInformation.dependentOnCount, 0);
        dependentCount = Objects.requireNonNullElse(versionInformation.dependencyOfCount, 0);
        popularityAppCount = Objects.requireNonNullElse(versionInformation.nsPopularityAppCount, 0);
        popularityOrgCount = Objects.requireNonNullElse(versionInformation.nsPopularityOrgCount, 0);
        releaseTime = Instant.ofEpochMilli(Objects.requireNonNullElse(versionInformation.publishedEpochMillis, 0L)).toString();
        status = LibraryStatus.GOOD;
    }

    public void setMissing() {
        categories = List.of();
        dependencyCount = 0;
        dependentCount = 0;
        popularityAppCount = 0;
        popularityOrgCount = 0;
        releaseTime = "";
        status = LibraryStatus.MISSING;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getLicenses() {
        return this.licenses;
    }

    public void setLicenses(List<String> licenses) {
        this.licenses = licenses;
    }

    public Integer getVulnerabilityCount() {
        return this.vulnerabilityCount;
    }

    public void setVulnerabilityCount(Integer vulnerabilityCount) {
        this.vulnerabilityCount = vulnerabilityCount;
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Integer getDependencyCount() {
        return this.dependencyCount;
    }

    public void setDependencyCount(Integer dependencyCount) {
        this.dependencyCount = dependencyCount;
    }

    public Integer getDependentCount() {
        return this.dependentCount;
    }

    public void setDependentCount(Integer dependentCount) {
        this.dependentCount = dependentCount;
    }

    public Integer getPopularityAppCount() {
        return this.popularityAppCount;
    }

    public void setPopularityAppCount(Integer popularityAppCount) {
        this.popularityAppCount = popularityAppCount;
    }

    public Integer getPopularityOrgCount() {
        return this.popularityOrgCount;
    }

    public void setPopularityOrgCount(Integer popularityOrgCount) {
        this.popularityOrgCount = popularityOrgCount;
    }

    public String getReleaseTime() {
        return this.releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
    }

    public LibraryStatus getStatus() {
        return this.status;
    }

    public void setStatus(LibraryStatus status) {
        this.status = status;
    }
}
