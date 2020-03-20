package org.shardav.utils;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a utility class used for providing logging capabilities in java applications.
 * The logging is provided in the terminal.
 *
 * @author deeznuts
 * @version 1.0.0
 * @since 2020
 */
public class Log {

    private static boolean LOG_VERBOSE = false;

    private static final String MESSAGE_FORMAT = "[%s %s] %s | %s : %s";

    private static PrintWriter out;

    static {

        try {
            String path = Paths.get(Log.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
            path = path.substring(0, path.lastIndexOf(File.separatorChar) + 1) + "log.txt";
            out = new PrintWriter(new FileOutputStream(path, false));
        } catch (Exception ex) {

            System.err.println("Error creating log.txt: " + ex.getMessage());

        }

    }

    /**
     * Output a log of level INFO.
     *
     * @param TAG     Nullable TAG for the log message
     * @param message LOG message
     */
    public static void i(@Nullable String TAG, @NotNull String message) {
        TAG = TAG == null ? "" : TAG;
        String currentTime = getCurrentTime();
        String output = String.format(MESSAGE_FORMAT, currentTime, "INFO",
                Thread.currentThread().getStackTrace()[2].getClassName(), TAG, message);
        System.out.println(output);
        out.println(output);
    }

    /**
     * Output a log of level INFO.
     *
     * @param TAG       Nullable TAG for the log message
     * @param message   LOG message
     * @param throwable (Optional) any object of type {@link Throwable}
     */
    public static void i(@Nullable String TAG, @NotNull String message, @Nullable Throwable throwable) {
        i(TAG, message);
        if (throwable != null) {
            throwable.printStackTrace();
            throwable.printStackTrace(out);
        }
    }

    /**
     * Output a log of level DEBUG.
     *
     * @param TAG     Nullable TAG for the log message
     * @param message LOG message
     */
    public static void d(@Nullable String TAG, @NotNull String message) {
        TAG = TAG == null ? "" : TAG;
        String currentTime = getCurrentTime();
        String output = String.format(MESSAGE_FORMAT, currentTime, "DEBUG",
                Thread.currentThread().getStackTrace()[2].getClassName(), TAG, message);
        System.out.println(output);
        out.println(output);
    }

    /**
     * Output a log of level DEBUG.
     *
     * @param TAG       Nullable TAG for the log message
     * @param message   LOG message
     * @param throwable (Optional) any object of type {@link Throwable}
     */
    public static void d(@Nullable String TAG, @NotNull String message, @Nullable Throwable throwable) {

        d(TAG, message);
        if (throwable != null) {
            throwable.printStackTrace();
            throwable.printStackTrace(out);
        }

    }

    /**
     * Output a log of level VERBOSE.
     *
     * @param TAG     Nullable TAG for the log message
     * @param message LOG message
     */
    public static void v(@Nullable String TAG, @NotNull String message) {

        String currentTime = getCurrentTime();
        String output = String.format(MESSAGE_FORMAT, currentTime, "VERBOSE",
                Thread.currentThread().getStackTrace()[2].getClassName(), TAG, message);
        if (LOG_VERBOSE) {
            TAG = TAG == null ? "" : TAG;
            System.out.println(output);
        }
        out.println(output);

    }

    /**
     * Output a log of level VERBOSE.
     *
     * @param TAG       Nullable TAG for the log message
     * @param message   LOG message
     * @param throwable (Optional) any object of type {@link Throwable}
     */
    public static void v(@Nullable String TAG, @NotNull String message, @Nullable Throwable throwable) {
        v(TAG, message);
        if (throwable != null) {
            if (LOG_VERBOSE)
                throwable.printStackTrace();
            throwable.printStackTrace(out);
        }

    }

    /**
     * Output a log of level ERROR.
     *
     * @param TAG     Nullable TAG for the log message
     * @param message LOG message
     */
    public static void e(@Nullable String TAG, @NotNull String message) {
        TAG = TAG == null ? "" : TAG;
        String currentTime = getCurrentTime();
        System.err.println(String.format(MESSAGE_FORMAT, currentTime, "ERROR",
                Thread.currentThread().getStackTrace()[2].getClassName(), TAG, message));
    }

    /**
     * Output a log of level ERROR.
     *
     * @param TAG       Nullable TAG for the log message
     * @param message   LOG message
     * @param throwable (Optional) any object of type {@link Throwable}
     */
    public static void e(@Nullable String TAG, @NotNull String message, @Nullable Throwable throwable) {
        e(TAG, message);
        if (throwable != null) {
            throwable.printStackTrace();
            throwable.printStackTrace(out);
        }
    }

    private static String getCurrentTime() {

        SimpleDateFormat time = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss aa");
        return time.format(new Date());

    }

    /**
     * Used to set if VERBOSE level logging is to shown or not.
     *
     * @param show true if VERBOSE level logging is to be shown.
     */
    public static void showVerbose(boolean show) {
        LOG_VERBOSE = show;
    }

    public static boolean verboseIsShown() {
        return LOG_VERBOSE;
    }

    private Log() {
        //Default constructor of type private so that no one can create any objects.
    }

}
