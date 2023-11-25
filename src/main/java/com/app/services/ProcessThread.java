package com.app.services;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.app.exceptions.TooManyRequestsException;
import com.app.model.ProcessResult;
import com.app.model.HttpResponse.HttpContent;
import com.app.model.Request.SimpleRequest;

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
    private ConcurrentLinkedQueue<ProcessResult> resultQueue;
    private ExitSignal exitSignal;

    public ProcessThread(ConcurrentLinkedQueue<SimpleRequest> requestQueue, ConcurrentLinkedQueue<ProcessResult> resultQueue, ExitSignal exitSignal) {
        this.requestQueue = requestQueue;
        this.resultQueue = resultQueue;
        this.exitSignal = exitSignal;
    }

    @Override
    public void run() {
        while (exitSignal.shouldContinue()) {
            if (requestQueue.isEmpty()) {
                try {
                    Thread.sleep(50, 0);
                    continue;
                } catch (InterruptedException e) {
                    System.out.println("do not awake me from my slumber");
                }
            }

            SimpleRequest request = requestQueue.poll();
            System.out.println(request.identifier);
            List<HttpContent> requestResult;
            try {
                requestResult = Requester.getDep(request);
            } catch (IOException | TooManyRequestsException e) {
                System.out.println("too many requests");
                exitSignal.signalNow();
                return;
            }
            ProcessResult result = new ProcessResult();
            result.from = request.identifier;
            result.result = requestResult;
            result.type = request.requestType;
            resultQueue.add(result);
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
