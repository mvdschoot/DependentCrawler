package com.app.model;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.app.utils.RequestType;

public class Identifier implements Serializable {
    public String groupId;
    public String artifactId;
    public String version;

    public Identifier(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public Identifier(RequestType type, DependencyResponse dependencyResponse) {
        if (RequestType.DEPENDENCY == type) {
            groupId = dependencyResponse.dependencyNamespace;
            artifactId = dependencyResponse.dependencyName;
            version = dependencyResponse.dependencyVersion;
        } else if (RequestType.SOURCE == type) {
            groupId = dependencyResponse.sourceNamespace;
            artifactId = dependencyResponse.sourceName;
            version = dependencyResponse.sourceVersion;
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
