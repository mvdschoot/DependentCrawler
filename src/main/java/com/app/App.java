package com.app;

import java.io.IOException;

import com.app.Processor.TraverseType;
import com.app.model.Identifier;

public class App 
{
    public static void main( String[] args )
    {
        String group = "junit";
        String artifact = "junit";
        String version = "4.13.2";

        try {
            Processor processor = new Processor(TraverseType.DOWN);
            processor.start(new Identifier(group, artifact, version));
            processor.store("storage.graphml");
        } catch (IOException e) {
            System.out.println("error");
        }
    }
}
