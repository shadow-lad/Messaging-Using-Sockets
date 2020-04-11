package org.shardav.server;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerExecutors {


    private static final ExecutorService clientHandlerService = Executors.newFixedThreadPool(20);
    private static final ExecutorService verificationHandlerService = Executors.newFixedThreadPool(20);
    private static final ExecutorService databaseService = Executors.newFixedThreadPool(2);
    private static final ExecutorService serverExecutor = Executors.newFixedThreadPool(2);

    private ServerExecutors(){}

    public static ExecutorService getClientHandlerExecutor() {
        return clientHandlerService;
    }

    public static ExecutorService getVerificationHandlerExecutor() {
        return verificationHandlerService;
    }

    public static ExecutorService getDatabaseExecutor() {
        return databaseService;
    }

    public static ExecutorService getServerExecutor() {
        return serverExecutor;
    }

    public static void close() {
        try {
            clientHandlerService.awaitTermination(2000, TimeUnit.MILLISECONDS);
            verificationHandlerService.awaitTermination(2000, TimeUnit.MILLISECONDS);
            databaseService.awaitTermination(2000, TimeUnit.MILLISECONDS);
            serverExecutor.awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore) {
            
        }
    }

}
