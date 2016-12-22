package com.sidereal.lumm.architecture.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummComponent;
import com.sidereal.lumm.architecture.LummConfiguration;
import com.sidereal.lumm.architecture.LummModule;
import com.sidereal.lumm.architecture.concrete.ConcreteLummModule;

public class Debug extends LummModule {

    private boolean enabled;

    private final HashMap<Class<? extends LummComponent>, ComponentDebugger> behaviors;

    private float currentDebugCooldown;
    private boolean startLogOnStartup;
    private final float defaultDebugCooldown;
    public OnFinishLogListener onFinishLogListener;

    private static String DEFAULT_TAG = "Lumm";
    Log currentLog;

    public interface OnFinishLogListener {

        public void finishLog(Log log, String logType);

    }

    public class LogMessage {

        @SuppressWarnings("unused")
        private String tag;
        @SuppressWarnings("unused")
        private String message;
        private String logType;
        private String errorId;
        @SuppressWarnings("unused")
        private long timestamp;

        LogMessage(String logType, String tag, String message, String errorId) {
            this.logType = logType;
            this.tag = tag;
            this.message = message;
            this.errorId = errorId != null ? errorId : "";
            this.timestamp = System.currentTimeMillis();
        }

        public String getTag() {
            return tag;
        }

        public String getMessage() {
            return message;
        }

        public String getLogType() {
            return logType;
        }

        public String getErrorId() {
            return errorId;
        }

        public long getTimestamp() {
            return timestamp;
        }


    }

    public class Log {

        public final static String LOG_MESSAGE = "Message";
        public final static String LOG_DEBUG = "Debug";
        public final static String LOG_ERROR = "Error";
        /**
         * Will store only message and debug types of logs
         */
        public static final String LOG_INFO = "Info";
        /**
         * Stores all types of logs
         */
        public static final String LOG_ALL = "All";
        public final static int MAX_LOG_MESSAGES = 1000;

        private String logType;
        @SuppressWarnings("unused")
        private String id;
        @SuppressWarnings("unused")
        private String appVersionId;
        @SuppressWarnings("unused")
        private String sessionId;
        @SuppressWarnings("unused")

        private long timeStart;
        @SuppressWarnings("unused")
        private long timeEnd;
        private ArrayList<LogMessage> messages;


        Log(String logtype) {
            this.logType = logtype;
            this.appVersionId = appVersionId;
            this.sessionId = sessionId;
            this.timeStart = System.currentTimeMillis();
            this.messages = new ArrayList<Debug.LogMessage>();
        }

        void addLogMessage(LogMessage message) {

            if (logType.equals(Log.LOG_ALL) || logType.equals(message.logType)) {
                messages.add(message);
                return;
            }

            if (logType == LOG_INFO && (message.logType == LOG_MESSAGE || message.logType == LOG_DEBUG))
                messages.add(message);

            if (messages.size() >= 500) {
                send();
                addLogMessage(message);
            }
        }

        void send() {

            timeEnd = System.currentTimeMillis();

            if (onFinishLogListener != null)
                onFinishLogListener.finishLog(this, this.logType);
            else
                logDebug("Called Debug.finishLog() without having set the OnFininshLogListener. Use ", null);

            // TODO make request to web api if app is connected
            // TODO afterwards clear up the existing log
        }


        public String getLogType() {
            return logType;
        }

        public void setLogType(String logType) {
            this.logType = logType;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAppVersionId() {
            return appVersionId;
        }

        public void setAppVersionId(String appVersionId) {
            this.appVersionId = appVersionId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public long getStartTime() {
            return timeStart;
        }

        public void setStartTime(long startTime) {
            this.timeStart = startTime;
        }

        public long getEndTime() {
            return timeEnd;
        }

        public void setEndTime(long endTime) {
            this.timeEnd = endTime;
        }

        public ArrayList<LogMessage> getMessages() {
            return messages;
        }

        public void setMessages(ArrayList<LogMessage> messages) {
            this.messages = messages;
        }


    }

    public Debug(LummConfiguration cfg) {

        super(cfg);
        this.enabled = cfg.debugEnabled;
        setUpdateFrequency(100f);
        this.behaviors = new HashMap<Class<? extends LummComponent>, ComponentDebugger>();
        this.defaultDebugCooldown = 0.125f;
        this.currentDebugCooldown = this.defaultDebugCooldown;
        this.startLogOnStartup = cfg.startDebugLogOnStartup;
        if (this.startLogOnStartup)
            startLog(Log.LOG_ALL);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onUpdate() {

        if (!enabled)
            return;

        // decrease duration until it can handle toggling on/off debuggers
        currentDebugCooldown -= Lumm.time.getDeltaTime();

        // there's still time until we can do anything, stop
        if (currentDebugCooldown > 0)
            return;

        // go over every entry in the map
        for (Entry<Class<? extends LummComponent>, ComponentDebugger> entry : behaviors.entrySet()) {
            // debug keys hasn't been setup or nothing was added to it, ignore
            // it
            if (entry.getValue().keysToActivate == null || entry.getValue().keysToActivate.size() == 0)
                continue;

            // whether or not we encountered a key that wasn't presed
            boolean mustSkip = false;

            // iterate through the required keys
            for (int j = 0; j < entry.getValue().keysToActivate.size(); j++) {
                // didn't press at least one of the keys required for activation
                if (!Gdx.input.isKeyPressed(entry.getValue().keysToActivate.get(j).intValue())) {
                    // break from current loop, set a flag to true which will
                    // skip the next part of the
                    // loop for the current entry
                    mustSkip = true;
                    break;
                }
            }

            // at least one key wasn't pressed, skip toggling on/off
            if (mustSkip)
                continue;

            // update the debugging status to the opposite of what it was before
            entry.getValue().enabled = !entry.getValue().enabled;

            log("Debugging enable status for " + entry.getKey().getName() + ": " + entry.getValue().enabled, null);

            // reset timer to the default value
            currentDebugCooldown = defaultDebugCooldown;
        }

    }

    /**
     * Prints the message in the console if debugging is enabled
     *
     * @param logType Type of logging
     * @param tag     log tag
     * @param message
     */
    private void log(String logType, String tag, String message, Throwable exception) {

        if (logType.equals(Log.LOG_ERROR)) {
            if (exception != null)
                Gdx.app.error(tag, message, exception);
            else
                Gdx.app.error(tag, message);
        } else if (logType.equals(Log.LOG_MESSAGE)) {
            if (exception != null)
                Gdx.app.log(tag, message, exception);
            else
                Gdx.app.log(tag, message);
        } else if (logType.equals(Log.LOG_DEBUG)) {
            if (exception != null)
                Gdx.app.debug(tag, message, exception);
            else
                Gdx.app.debug(tag, message);
        }

        LogMessage log = new LogMessage(logType, tag, message, null);

        if (currentLog != null)
            currentLog.addLogMessage(log);
    }

    /**
     * Logs an error to the platform's logging application with the
     * {@value #DEFAULT_TAG} tag. See also {@link #logError(String, String)}.
     *
     * @param message   The message to log
     * @param exception Exception thrown along with error, can be null
     */
    public void logError(String message, Throwable exception) {
        logError(message, DEFAULT_TAG, exception);
    }


    /**
     * Logs an error to the platform's logging application with a specific tag.
     * <p>
     * See also {@link #logError(String)}
     *
     * @param message   The message to log
     * @param tag       The tag of the log message
     * @param exception Exception thrown along with error, can be null
     */
    public void logError(String message, String tag, Throwable exception) {
        log(Log.LOG_ERROR, tag, message, exception);
    }

    /**
     * Logs a debug message to the platform's logging application with the
     * {@value #DEFAULT_TAG} tag.
     * <p>
     * See also {@link #logDebug(String, String))}.
     *
     * @param message   The message to log
     * @param exception Exception thrown along with error, can be null
     */
    public void logDebug(String message, Throwable exception) {
        logDebug(message, DEFAULT_TAG, exception);
    }

    /**
     * Logs a debug message to the platform's logging application with a
     * specific tag.
     * <p>
     * See also {@link #logDebug(String)}.
     *
     * @param message   The message to log
     * @param tag       The tag of the log message
     * @param exception Exception thrown along with error, can be null
     */
    public void logDebug(String message, String tag, Throwable exception) {
        log(Log.LOG_DEBUG, tag, message, exception);
    }

    /**
     * Logs a message to the platform's logging application.
     *
     * @param message   The message to log
     * @param exception Exception thrown along with error, can be null
     */
    public void log(String message, Throwable exception) {
        log(message, DEFAULT_TAG, exception);
    }

    /**
     * Logs a message to the platform's logging application.
     *
     * @param message   The message to log
     * @param tag       The tag of the log message
     * @param exception Exception thrown along with error, can be null
     */
    public void log(String message, String tag, Throwable exception) {
        log(Log.LOG_MESSAGE, tag, message, exception);

    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean containsComponentDebugger(Class<? extends LummComponent> clazz) {
        return behaviors.containsKey(clazz);
    }

    public void addComponentDebugger(Class<? extends LummComponent> clazz) {
        if (!containsComponentDebugger(clazz))
            behaviors.put(clazz, new ComponentDebugger());
    }

    public ComponentDebugger getComponentDebugger(Class<? extends LummComponent> clazz) {
        return behaviors.get(clazz);
    }

    /**
     * Starts internally storing all log messages for submission to an L-NET
     * instance. If a log is in progress when calling this nothing will happen,
     * you have to either call {@link #finishLog()} or {@link #cancelLog}
     * <p>
     * The messages are stored until either the maximum number of log messages
     * is achieved ( @link {@link Log#MAX_LOG_MESSAGES}, after which
     * {@link #onFinishLogListener} is called.
     *
     * @param logType
     */
    public void startLog(String logType) {
        if (currentLog != null) {
            logError(
                    "Attempting to call Debug.startLog() without calling Debug.finishLog() or Debug.cancelLog() first, ignored.",
                    new NullPointerException());
            return;
        }

        currentLog = new Log(logType);
    }

    /**
     * Ends the log and sends it to the connected L-NET instance if the
     * application is set up for that.
     * <p>
     * To cancel a log call {@link #cancelLog()}
     */
    public void finishLog() {
        if (currentLog == null) {
            logError("Attempting to call Debug.finishLog() without calling Debug.startLog() first, ignored.",
                    new NullPointerException());
            return;
        }

        currentLog.send();
        currentLog = null;
    }

    public void cancelLog() {
        currentLog = null;
    }

    public boolean isLogging() {
        return currentLog != null;
    }

    @Override
    public List<Class<? extends LummModule>> getDependencies() {
        return null;
    }

}
