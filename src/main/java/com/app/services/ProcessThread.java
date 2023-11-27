package com.app.services;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.app.exceptions.MissingDependencyException;
import com.app.exceptions.TooManyRequestsException;
import com.app.model.DependencyResponse;
import com.app.model.ProcessResult;
import com.app.model.VersionResponse;
import com.app.model.Requests.SimpleRequest;
import com.app.utils.LibraryStatus;
import com.app.utils.RequestType;

public class ProcessThread extends Thread {
    public static class ExitSignal {
        private boolean exit = false;

        public void signalNow() {
            exit = true;
        }

        public boolean shouldContinue() {
            return !exit;
        }
    }

    private Thread thread;
    private ConcurrentLinkedQueue<SimpleRequest> requestQueue;
    private ConcurrentLinkedQueue<ProcessResult<?>> resultQueue;
    private ExitSignal exitSignal;

    public ProcessThread(ConcurrentLinkedQueue<SimpleRequest> requestQueue, ConcurrentLinkedQueue<ProcessResult<?>> resultQueue, ExitSignal exitSignal) {
        this.requestQueue = requestQueue;
        this.resultQueue = resultQueue;
        this.exitSignal = exitSignal;
    }

    @Override
    public void run() {
        while (exitSignal.shouldContinue()) {
            SimpleRequest request = requestQueue.poll();
            if (request == null) {
                try {
                    Thread.sleep(50, 0);
                    continue;
                } catch (InterruptedException e) {
                    System.out.println("do not awake me from my slumber");
                }
            }
            
            System.out.println(request.requestType.name() + " " + request.identifier);

            try {
                if (request.requestType == RequestType.VERSION) {
                    VersionResponse requestResult = Requester.getVersionInformation(request.identifier);
                    ProcessResult<VersionResponse> result = new ProcessResult<>();
                    result.from = request.identifier;
                    result.result = List.of(requestResult);
                    result.type = request.requestType;
                    resultQueue.add(result);
                } else {
                    List<DependencyResponse> requestResult = Requester.getLibrary(request);
                    ProcessResult<DependencyResponse> result = new ProcessResult<>();
                    result.from = request.identifier;
                    result.result = requestResult;
                    result.type = request.requestType;
                    resultQueue.add(result);
                }
            } catch (IOException | TooManyRequestsException e) {
                System.out.println("too many requests");
                exitSignal.signalNow();
                return;
            } catch (MissingDependencyException e) {
                ProcessResult<VersionResponse> result = new ProcessResult<>();
                result.from = request.identifier;
                result.result = List.of();
                result.type = RequestType.VERSION;
                result.status = LibraryStatus.MISSING;
                resultQueue.add(result);
            }
        }
    }

    @Override 
    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }
}
