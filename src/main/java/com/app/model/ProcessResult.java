package com.app.model;

import java.util.List;

import com.app.model.HttpResponse.HttpContent;
import com.app.services.Requester.RequestType;

public class ProcessResult {
    public Identifier from;
    public List<HttpContent> result;
    public RequestType type;
}
