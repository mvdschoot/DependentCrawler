package com.app;

import java.io.IOException;

import com.app.model.DependentResponse;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String group = "org.springframework";
        String artifact = "spring-webmvc";
        String version = "6.1.0";

        try {
            DependentResponse response = Request.getDependents(group, artifact, version);
            int a = 5;
        } catch (IOException e) {
        }
    }
}
