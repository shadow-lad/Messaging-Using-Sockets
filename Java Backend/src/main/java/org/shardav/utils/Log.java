package org.shardav.utils;

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

    enum LOG {

        DEBUG,
        ERROR,
        INFO,
        VERBOSE

    }

    private static String path = null;

    private static boolean LOG_VERBOSE = false;

    private static final String MESSAGE_FORMAT = "[%s\t%s]\t%s\t|\t%s\t:\t%s";

    private static final Object LOCK = new Object();

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");

    private static void printLogMessage(LOG level, String className, String TAG, String message, Throwable throwable ) {

        synchronized (LOCK) {

            PrintWriter out = null;

            try {
                SimpleDateFormat logFormat = new SimpleDateFormat("yyyy-MM-dd+hh-mm-ssaa");
                if (path == null) {
                    path = Paths.get(Log.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
                    path = path.substring(0, path.lastIndexOf(File.separatorChar) + 1) + "server" + logFormat.format(new Date()) + ".log";
                }

                out = new PrintWriter(new FileWriter(path, true));

                if (TAG == null) {
                    TAG = "";
                }

                String output = String.format(MESSAGE_FORMAT,
                        getCurrentTime(),
                        level.toString(),
                        className,
                        TAG,
                        message);

                out.println(output);
                out.flush();
                if(throwable !=null) {
                    throwable.printStackTrace(out);
                    out.flush();
                }

                if(level == LOG.VERBOSE & !LOG_VERBOSE) {
                    return;
                }

                if(level == LOG.ERROR) {
                    System.err.println(output);
                } else {
                    System.out.println(output);
                }

                if (throwable!=null) {
                    throwable.printStackTrace();
                }

            } catch (Exception ex) {
                System.err.println("Error creating log.txt: " + ex.getMessage());
            } finally {
                if(out != null) {
                    out.flush();
                    out.close();
                }
            }
        }

    }

    /**
     * Output a log of level INFO.
     * @param message LOG message
     */
    public static void i(String TAG, String message) {
        i(TAG, message, null);
    }

    /**
     * Output a log of level INFO.
     *
     * @param TAG       Nullable TAG for the log message
     * @param message   LOG message
     * @param throwable (Optional) any object of type {@link Throwable}
     */
    public static void i(String TAG, String message, Throwable throwable) {

        printLogMessage(LOG.INFO,
                Thread.currentThread().getStackTrace()[2].getClassName(),
                TAG,
                message,
                throwable);

    }

    /**
     * Output a log of level DEBUG.
     *
     * @param TAG     Nullable TAG for the log message
     * @param message LOG message
     */
    public static void d(String TAG, String message) {
        d(TAG, message, null);
    }

    /**
     * Output a log of level DEBUG.
     *
     * @param TAG       Nullable TAG for the log message
     * @param message   LOG message
     * @param throwable (Optional) any object of type {@link Throwable}
     */
    public static void d(String TAG, String message, Throwable throwable) {

        printLogMessage(LOG.DEBUG,
                Thread.currentThread().getStackTrace()[2].getClassName(),
                TAG,
                message,
                throwable);

    }

    /**
     * Output a log of level VERBOSE.
     *
     * @param TAG     Nullable TAG for the log message
     * @param message LOG message
     */
    public static void v(String TAG, String message) {
        v(TAG, message, null);
    }

    /**
     * Output a log of level VERBOSE.
     *
     * @param TAG       Nullable TAG for the log message
     * @param message   LOG message
     * @param throwable (Optional) any object of type {@link Throwable}
     */
    public static void v(String TAG, String message, Throwable throwable) {

        printLogMessage(LOG.VERBOSE,
                Thread.currentThread().getStackTrace()[2].getClassName(),
                TAG,
                message,
                throwable);

    }

    /**
     * Output a log of level ERROR.
     *
     * @param TAG     Nullable TAG for the log message
     * @param message LOG message
     */
    public static void e(String TAG, String message) {
        e(TAG, message, null);
    }

    /**
     * Output a log of level ERROR.
     *
     * @param TAG       Nullable TAG for the log message
     * @param message   LOG message
     * @param throwable (Optional) any object of type {@link Throwable}
     */
    public static void e(String TAG, String message, Throwable throwable) {
        printLogMessage(LOG.ERROR,
                Thread.currentThread().getStackTrace()[2].getClassName(),
                TAG,
                message,
                throwable);
    }

    private static String getCurrentTime() {
        return timeFormat.format(new Date());
    }

    /**
     * Used to set if VERBOSE level logging is to shown or not.
     *
     * @param show true if VERBOSE level logging is to be shown.
     */
    public static void showVerbose(boolean show) {
        LOG_VERBOSE = show;
    }

    /**
     * Returns true if verbose logging is being shown
     *
     * @return boolean value that indicates if verbose logging is shown.
     */
    public static boolean verboseIsShown() {
        return LOG_VERBOSE;
    }

    private Log() {
        //Default constructor of type private so that no one can create any objects.
    }

}
