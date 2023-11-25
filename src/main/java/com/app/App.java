package com.app;

import java.io.IOException;

import com.app.exceptions.TooManyRequestsException;
import com.app.model.Identifier;
import com.app.services.Processor;
import com.app.services.Processor.TraverseType;

public class App 
{
    public static void main( String[] args )
    {
        String group = "org.springframework";
        String artifact = "spring-webmvc";
        String version = "6.1.1";
        // String group = "org.springframework.boot";
        // String artifact = "spring-boot-starter-web";
        // String version = "3.2.0";

        try {
            Processor processor = new Processor(TraverseType.BOTH);
            processor.start(new Identifier(group, artifact, version), 8);
            processor.store("storage.graphml");
        } catch (IOException | InterruptedException | TooManyRequestsException e) {
            System.out.println("error");
        }
    }
}
