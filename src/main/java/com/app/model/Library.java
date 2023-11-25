package com.app.model;

import java.io.Serializable;
import java.util.List;

import com.app.model.HttpResponse.HttpContent;
import com.app.services.Requester.RequestType;

public class Library implements Serializable {
    private Identifier identifier;
    private String scope;
    private List<String> licenses;
    private Integer vulnerabilityCount;

    public Library(RequestType requestType, HttpContent dependencyResult) {
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

}
