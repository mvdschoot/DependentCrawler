package com.app.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.app.exceptions.MissingDependencyException;
import com.app.exceptions.TooManyRequestsException;
import com.app.model.DependencyResponse;
import com.app.model.Identifier;
import com.app.model.PagedResponse;
import com.app.model.VersionResponse;
import com.app.model.Requests.DependencyRequest;
import com.app.model.Requests.SimpleRequest;
import com.app.model.Requests.VersionRequest;
import com.app.utils.RequestType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Requester {
    private static final String dependentsUrl = "https://central.sonatype.com/api/internal/browse/dependents";
    private static final String dependenciesUrl = "https://central.sonatype.com/api/internal/browse/dependencies";
    private static final String versionsUrl = "https://central.sonatype.com/api/internal/browse/component/versions";

    public static VersionResponse getVersionInformation(Identifier identifier) throws IOException, TooManyRequestsException, MissingDependencyException {
        VersionRequest request = new VersionRequest(identifier);
        PagedResponse<VersionResponse> response = sendVersionRequest(request);

        if (response.components.size() == 0) {
            throw new MissingDependencyException();
        }
        return response.components.get(0);
    }

    public static List<DependencyResponse> getLibrary(SimpleRequest request) throws IOException, TooManyRequestsException {
        return getLibrary(request.requestType, new DependencyRequest(request.identifier));
    }

    public static List<DependencyResponse> getLibrary(RequestType type, DependencyRequest request) throws IOException, TooManyRequestsException {        
        PagedResponse<DependencyResponse> response = sendLibraryRequest(type, request);

        List<DependencyResponse> deps = new ArrayList<>(response.totalResultCount);
        deps.addAll(response.components);

        request.page = 1;
        while (request.page < response.pageCount) {
            response = sendLibraryRequest(type, request);
            deps.addAll(response.components);

            request.page += 1;
        }

        return deps;
    }

    private static PagedResponse<DependencyResponse> sendLibraryRequest(RequestType type, DependencyRequest request) throws IOException, TooManyRequestsException {
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

                return new ObjectMapper().readValue(responseBody, new TypeReference<PagedResponse<DependencyResponse>>() {});
            }
        }
    }

    private static PagedResponse<VersionResponse> sendVersionRequest(VersionRequest request) throws IOException, TooManyRequestsException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet getRequest = new HttpGet(request.encode(versionsUrl));

            getRequest.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = client.execute(getRequest)) {
                if (response.getStatusLine().getStatusCode() == 429) {
                    throw new TooManyRequestsException();
                } 
                else if (response.getStatusLine().getStatusCode() != 200) {
                    System.out.println("Status: " + response.getStatusLine().getStatusCode() + "\nBody: " + EntityUtils.toString(response.getEntity()));
                    throw new IOException("Bad response");
                }
                
                String responseBody = EntityUtils.toString(response.getEntity());

                return new ObjectMapper().readValue(responseBody, new TypeReference<PagedResponse<VersionResponse>>() {});
            }
        }
    }
}
