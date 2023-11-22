package com.app;

import java.io.IOException;
import java.util.List;

import com.app.model.DependentResponse;
import com.app.model.DependentResult;

public class App 
{
    public static void main( String[] args )
    {
        String group = "org.springframework";
        String artifact = "spring-webmvc";
        String version = "6.0.12";

        try {
            List<DependentResult> response = Request.getDependents(group, artifact, version);
            System.out.println("result");
        } catch (IOException e) {
            System.out.println("error");
        }
    }
}
