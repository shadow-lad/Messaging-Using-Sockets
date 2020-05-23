package org.shardav.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerExecutors {

    private static final ExecutorService CLIENT_HANDLER_SERVICE = Executors.newFixedThreadPool(20);

    private static final ExecutorService DATABASE_SERVICE = Executors.newFixedThreadPool(2);
    private static final ExecutorService DATABASE_RESULT_SERVICE = Executors.newSingleThreadExecutor();

    private static final ExecutorService SERVER_EXECUTOR = Executors.newFixedThreadPool(2);

    private static final ExecutorService OTP_EXECUTOR = Executors.newFixedThreadPool(2);

    private ServerExecutors() {
    }

    public static ExecutorService getClientHandlerExecutor() {
        return CLIENT_HANDLER_SERVICE;
    }

    public static ExecutorService getDatabaseExecutor() {
        return DATABASE_SERVICE;
    }

    public static ExecutorService getServerExecutor() {
        return SERVER_EXECUTOR;
    }

    public static ExecutorService getDatabaseResultExecutor() {
        return DATABASE_RESULT_SERVICE;
    }

    public static ExecutorService getOtpExecutor() {
        return OTP_EXECUTOR;
    }

    public static void close() {
        try {
            CLIENT_HANDLER_SERVICE.awaitTermination(2000, TimeUnit.MILLISECONDS);
            DATABASE_SERVICE.awaitTermination(2000, TimeUnit.MILLISECONDS);
            SERVER_EXECUTOR.awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore) {
        }
    }

}
