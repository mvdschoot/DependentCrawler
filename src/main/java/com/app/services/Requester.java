package com.app.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.app.exceptions.TooManyRequestsException;
import com.app.model.HttpResponse;
import com.app.model.HttpResponse.HttpContent;
import com.app.model.Request.HttpRequest;
import com.app.model.Request.SimpleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Requester {
    public enum RequestType {
        DEPENDENCY, SOURCE
    }

    public static final String dependentsUrl = "https://central.sonatype.com/api/internal/browse/dependents";
    public static final String dependenciesUrl = "https://central.sonatype.com/api/internal/browse/dependencies";

    public static List<HttpContent> getDep(SimpleRequest request) throws IOException, TooManyRequestsException {
        return getDep(request.requestType, new HttpRequest(request.identifier));
    }

    public static List<HttpContent> getDep(RequestType type, HttpRequest request) throws IOException, TooManyRequestsException {        
        HttpResponse response = sendDepRequest(type, request);

        List<HttpContent> dependents = new ArrayList<>(response.totalResultCount);
        dependents.addAll(response.components);

        request.page = 1;
        while (request.page < response.pageCount) {
            response = sendDepRequest(type, request);
            dependents.addAll(response.components);

            request.page += 1;
        }

        return dependents;
    }

    private static HttpResponse sendDepRequest(RequestType type, HttpRequest request) throws IOException, TooManyRequestsException {
        String requestBodyString = new ObjectMapper().writeValueAsString(request);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost postRequest = new HttpPost(type == RequestType.SOURCE ? dependentsUrl : dependenciesUrl);

            StringEntity body = new StringEntity(requestBodyString);
            postRequest.setEntity(body);
            postRequest.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = client.execute(postRequest)) {
                if (response.getStatusLine().getStatusCode() == 429) {
                    throw new TooManyRequestsException();
                } 
                else if (response.getStatusLine().getStatusCode() != 200) {
                    System.out.println("Status: " + response.getStatusLine().getStatusCode() + "\nBody: " + EntityUtils.toString(response.getEntity()));
                    throw new IOException("Bad response");
                }
                
                String responseBody = EntityUtils.toString(response.getEntity());

                return new ObjectMapper().readValue(responseBody, HttpResponse.class);
            }
        }
    }
}