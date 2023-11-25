package com.app.model;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.app.model.HttpResponse.HttpContent;
import com.app.services.Requester.RequestType;

public class Identifier implements Serializable {
    public String groupId;
    public String artifactId;
    public String version;

    public Identifier(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public Identifier(RequestType type, HttpContent dependencyResult) {
        if (RequestType.DEPENDENCY == type) {
            groupId = dependencyResult.dependencyNamespace;
            artifactId = dependencyResult.dependencyName;
            version = dependencyResult.dependencyVersion;
        } else {
            groupId = dependencyResult.sourceNamespace;
            artifactId = dependencyResult.sourceName;
            version = dependencyResult.sourceVersion;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Identifier)) {
            return false;
        }
        Identifier other = (Identifier) o;
        return Objects.equals(groupId, other.groupId) && Objects.equals(artifactId, other.artifactId) && Objects.equals(version, other.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    @Override
    public String toString() {
        return String.join(":", List.of(groupId, artifactId, version));
    }
    
}
