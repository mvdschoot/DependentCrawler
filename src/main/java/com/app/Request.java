package com.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.app.model.DependentRequest;
import com.app.model.DependentResponse;
import com.app.model.DependentResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Request {
    public static List<DependentResult> getDependents(String group, String artifact, String version) throws IOException {
        DependentRequest request = new DependentRequest(group, artifact, version);
        
        DependentResponse response = sendRequest(request);

        List<DependentResult> dependents = new ArrayList<>(response.pageCount);
        dependents.addAll(response.components);

        request.page = 1;
        while (request.page != response.pageCount) {
            response = sendRequest(request);
            dependents.addAll(response.components);

            request.page += 1;
        }

        return dependents;
    }

    private static DependentResponse sendRequest(DependentRequest request) throws IOException {
        String requestBodyString = new ObjectMapper().writeValueAsString(request);

        String url = "https://central.sonatype.com/api/internal/browse/dependents";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost postRequest = new HttpPost(url);

            StringEntity body = new StringEntity(requestBodyString);
            postRequest.setEntity(body);
            postRequest.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = client.execute(postRequest)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.out.println("Status: " + response.getStatusLine().getStatusCode() + "\nBody: " + EntityUtils.toString(response.getEntity()));
                    throw new IOException("Bad response");
                }
                
                String responseBody = EntityUtils.toString(response.getEntity());

                return new ObjectMapper().readValue(responseBody, DependentResponse.class);
            }
        }
    }
}
