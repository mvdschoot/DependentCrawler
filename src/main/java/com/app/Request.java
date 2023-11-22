package com.app;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.app.model.DependentRequest;
import com.app.model.DependentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Request {
    public static DependentResponse getDependents(String group, String artifact, String version) throws IOException {
        DependentRequest requestBodyPojo = new DependentRequest(group, artifact, version);
        String requestBodyString = new ObjectMapper().writeValueAsString(requestBodyPojo);

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
