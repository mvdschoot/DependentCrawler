package com.app.model;

import java.io.Serializable;

public class Edge implements Serializable {
    private String scope;

    public Edge(String scope) {
        this.scope = scope;
    }

    public Edge(DependencyResponse depResult) {
        this.scope = depResult.scope;
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
    
}
