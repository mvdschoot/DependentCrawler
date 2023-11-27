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
        // String group = "org.junit.platform";
        // String artifact = "junit-platform-commons";
        // String version = "1.10.1";

        Identifier id = new Identifier(group, artifact, version);
        TraverseType traverseType = TraverseType.DOWN;
        try {
            Processor processor = new Processor(traverseType, 8, id);
            processor.store(traverseType.name() + ":" + id.toString() + ".graphml");
        } catch (IOException | TooManyRequestsException e) {
            System.out.println("error");
        }
    }
}
