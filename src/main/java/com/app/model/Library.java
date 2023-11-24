package com.app.model;

import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.nio.graphml.GraphMLExporter.AttributeCategory;

import com.app.Request.RequestType;
import com.app.model.DepResponse.DepResult;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;

public class Library {
    private Identifier identifier;
    private String scope;
    private List<String> licenses;
    private Integer vulnerabilityCount;

    public Library(RequestType requestType, DepResult dependencyResult) {
        if (RequestType.SOURCE == requestType) {
            identifier = new Identifier(dependencyResult.sourceNamespace, dependencyResult.sourceName, dependencyResult.sourceVersion);
            vulnerabilityCount = dependencyResult.sourceOssIndexInfo.vulnerabilityCount;
        } else {
            identifier = new Identifier(dependencyResult.dependencyNamespace, dependencyResult.dependencyName, dependencyResult.dependencyVersion);
            vulnerabilityCount = dependencyResult.dependencyOssIndexInfo.vulnerabilityCount;
        }
    
        scope = dependencyResult.scope;
        licenses = dependencyResult.licenses;

        if (vulnerabilityCount == null) {
            vulnerabilityCount = 0;
        }
    }

    public Map<String, Attribute> toExportFormat() {
        return Map.of(
            "Id", DefaultAttribute.createAttribute(identifier.toString()),
            "Scope", DefaultAttribute.createAttribute(scope),
            "Licenses", DefaultAttribute.createAttribute(String.join(";", licenses)),
            "VulnerabilityCount", DefaultAttribute.createAttribute(vulnerabilityCount)
        );
    }

    public static void registerAttributes(GraphMLExporter<Library, DefaultEdge> exporter) {
        exporter.registerAttribute("Id", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("Scope", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("Licenses", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("VulnerabilityCount", AttributeCategory.NODE, AttributeType.INT);
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
