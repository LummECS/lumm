package com.sidereal.lumm.architecture.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.net.HttpRequestHeader;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummConfiguration;
import com.sidereal.lumm.architecture.LummModule;
import com.sidereal.lumm.architecture.core.Debug.Log;
import com.sidereal.lumm.architecture.core.Debug.LogMessage;
import com.sidereal.lumm.architecture.core.Debug.OnFinishLogListener;
import com.sidereal.lumm.architecture.listeners.OnDisposeListener;
import com.sidereal.lumm.util.Utility;

public class Net extends LummModule {

    private static final String getSession = "Get session";
    private static final String addLog = "Add log";
    public static String addError = "Add error";

    private static String DEFAULT_TAG = "L-Net";

    private String serverPath = "http://lummenvironment.vmm2wnt24w.eu-central-1.elasticbeanstalk.com/lumm/api";
    private String appVersion;
    private String appKey;
    private String sessionId;
    private Boolean debug;
    private Boolean startSessionAutomatically;
    private Session session;
    private HttpResponseListener lNetListener;

    private NetRequestQueue lNetRequestQueue;

    private HashSet<NetRequestQueue> requestQueue;

    public static long TIME_BEFORE_SESSION_REFRESH = 1000 * 60 * 15;

    static class Session {
        private String id;
        private String appKey;
        private String platform;
        private String os;
        private String osVersion;
        private boolean debug;

        private long timeStart;
        private long timeEnd;
        private long timeUpdated;
        private long timeDeleted;
    }

    static class ResponseWrapper {

        private int code;
        private String throwable;
        private String message;
        private Boolean success;
        private String action;
        private Object object;
        private JSONObject fullObject;

        static ResponseWrapper get(String response) {
            ResponseWrapper wrapper = new ResponseWrapper();
            JSONObject object = null;
            try {
                object = new JSONObject(response);


                wrapper.action = object.getString("action");
                wrapper.success = object.getBoolean("success");
                wrapper.code = object.getInt("code");
                wrapper.throwable = object.optString("throwable");
                wrapper.message = object.getString("message");
                wrapper.fullObject = object;
            } catch (Exception e) {
                Lumm.debug.logError("Unable to parse LNet response to JSON", e);
            }
            return wrapper;
        }

    }

    static class Request {
        HttpRequest request;
        HttpResponseListener responseListener;

        public Request(HttpRequest request, HttpResponseListener listener) {
            this.request = request;
            this.responseListener = listener;
        }

    }

    public class NetRequestQueue {

        private List<Request> requests;

        private Request currRequest;

        private volatile boolean running;
        private volatile boolean mustFinish;
        private volatile boolean finishQueueFirst;
        private volatile Thread thread;
        private Runnable runnable;

        public NetRequestQueue() {
            requests = new LinkedList<Net.Request>();
            currRequest = null;
            mustFinish = false;
            runnable = new Runnable() {

                @Override
                public void run() {

                    while (true) {

                        Request request = getNext();

                        if (request != null) {

                            if (currRequest != null) {
                                synchronized (currRequest) {
                                    currRequest = request;
                                }

                            } else {
                                currRequest = request;
                            }

                            Gdx.net.sendHttpRequest(currRequest.request, new HttpResponseListener() {

                                @Override
                                public void handleHttpResponse(HttpResponse httpResponse) {
                                    synchronized (currRequest) {
                                        if (currRequest.responseListener != null)
                                            currRequest.responseListener.handleHttpResponse(httpResponse);
                                        currRequest = null;
                                    }
                                }

                                @Override
                                public void failed(Throwable t) {

                                    synchronized (currRequest) {
                                        if (currRequest.responseListener != null)
                                            currRequest.responseListener.failed(t);
                                        currRequest = null;
                                    }
                                }

                                @Override
                                public void cancelled() {

                                    synchronized (currRequest) {
                                        if (currRequest.responseListener != null)
                                            currRequest.responseListener.cancelled();

                                        currRequest = null;
                                    }
                                    // req message
                                }
                            });
                        }

                        // try {
                        // Thread.sleep(10);
                        // } catch (InterruptedException e) {
                        // Lumm.debug.logError(e.getMessage(), e);
                        // }

                        handleFinish();

                    }

                }
            };
        }

        public void cancelRequest(final HttpRequest request) {

            Thread t = new Thread() {

                public void run() {

                    boolean isCurrentRequest = false;

                    if (currRequest != null) {
                        synchronized (currRequest) {
                            if (request.equals(currRequest)) {
                                isCurrentRequest = true;
                            }
                        }
                    }

                    if (isCurrentRequest) {
                        Gdx.net.cancelHttpRequest(request);
                    } else {
                        synchronized (requests) {
                            int reqIndex = -1;
                            for (int i = 0; i < requests.size(); i++) {
                                if (requests.get(i).request.equals(request))
                                    reqIndex = i;
                            }
                            if (reqIndex != -1) {

                                requests.get(reqIndex).responseListener.cancelled();
                                requests.remove(reqIndex);
                            }
                        }
                    }

                }
            };

            t.start();

        }

        public void addRequest(final HttpRequest request, final HttpResponseListener responseListener) {
            if (request == null) {
                Lumm.debug.logError("Attempting to call NetRequestQeue.addRequest with a null HttpRequest parameter.",
                        new NullPointerException());
                return;
            }

            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {

                    synchronized (requests) {

                        // scheduled to finish
                        if (mustFinish) {
                            if (finishQueueFirst) {
                                requests.add(new Request(request, responseListener));

                            } else {
                                Lumm.debug.logError(
                                        "attepting to add a request while trying to end another one using NetRequestQueue.finish(false)",
                                        null);
                            }
                        } else {
                            synchronized (requests) {
                                requests.add(new Request(request, responseListener));
                            }
                            // previously had things in queue but they ended, so
                            // the thread is no longer running, start a new one
                            if (running && thread == null)
                                startQueue();

                        }
                    }

                }
            });

            t.start();

        }

        public void startQueue() {
            running = true;
            if (thread != null) {
                Lumm.debug.logError(
                        "Attempting to call NetRequestQueue.startQueue on a queue that is already in progress", null);
            } else {
                thread = new Thread(runnable);
                thread.start();
            }
        }

        public void finish(final boolean finishQueueFirst) {

            Thread t = new Thread() {

                public void run() {

                    running = false;
                    NetRequestQueue.this.finishQueueFirst = finishQueueFirst;
                    mustFinish = false;

                }

                ;
            };
            t.start();

        }

        private void handleFinish() {
            if (mustFinish) {

                if (finishQueueFirst) {
                    synchronized (requests) {

                        synchronized (currRequest) {

                            // if the thing is empty
                            if (finishQueueFirst && requests.size() == 0 && currRequest == null) {
                                terminateThread();
                                // TODO remove this net queue
                            }
                        }
                    }
                } else {

                    Request request = null;
                    if (currRequest != null) {
                        synchronized (currRequest) {
                            request = currRequest;

                        }
                    }
                    if (request != null) {
                        cancelRequest(request.request);
                        if (currRequest != null) {
                            synchronized (currRequest) {
                                currRequest = null;
                            }
                        }
                    }

                    synchronized (requests) {
                        while (requests.size() > 0) {
                            requests.get(0).responseListener.cancelled();
                        }
                    }

                    mustFinish = false;

                }
            }
        }

        private Request getNext() {
            Request request = null;
            boolean inProgress = false;
            if (currRequest != null)
                inProgress = true;

            if (!inProgress) {
                synchronized (requests) {

                    if (requests.size() > 0) {
                        request = requests.remove(0);
                    } else {
                        terminateThread();
                    }
                }
            }
            return request;
        }

        private void terminateThread() {
            thread = null;
            mustFinish = false;
            Thread.currentThread().interrupt();
        }
    }

    // http://www.gamefromscratch.com/post/2014/03/11/LibGDX-Tutorial-10-Basic-networking.aspx
    public Net(LummConfiguration config) {
        super(config);
        this.appVersion = config.applicationVersion;
        this.appKey = config.applicationLNetKey;
        this.debug = config.debugEnabled;
        this.startSessionAutomatically = config.startLNetSessionOnStartup;

        lNetListener = new HttpResponseListener() {

            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                String response = httpResponse.getResultAsString();
                Lumm.debug.log("response: " + response + " status: " + httpResponse.getStatus(), null);
                ResponseWrapper wrapper = ResponseWrapper.get(response);

                if (wrapper.action.equals(Net.getSession)) {

                    if (wrapper.success && wrapper.fullObject.has("object")) {

                        try {
                            session = new Session();
                            JSONObject sess = wrapper.fullObject.getJSONObject("object");
                            session.id = sess.getString("id");
                            session.timeStart = sess.getLong("timeStart");
                            session.timeEnd = sess.getLong("timeEnd");
                            session.timeUpdated = sess.getLong("timeUpdated");
                            session.debug = sess.getBoolean("debug");
                            session.platform = sess.getString("platform");
                            session.timeDeleted = sess.optLong("timeDeleted");

                            Lumm.debug.onFinishLogListener = new OnFinishLogListener() {

                                @Override
                                public void finishLog(Log log, String logType) {

                                    try {
                                        JSONObject object = new JSONObject();
                                        object.put("timeStart", log.getStartTime());
                                        object.put("timeEnd", log.getEndTime());
                                        object.put("type", log.getLogType());
                                        object.put("userSessionId", session.id);

                                        JSONArray array = new JSONArray();
                                        for (int i = 0; i < log.getMessages().size(); i++) {
                                            LogMessage logMessage = log.getMessages().get(i);
                                            JSONObject logMessageJson = new JSONObject();
                                            logMessageJson.put("type", logMessage.getLogType());
                                            logMessageJson.put("message", logMessage.getMessage());
                                            logMessageJson.put("tag", logMessage.getTag());
                                            logMessageJson.put("errorId", logMessage.getErrorId());
                                            logMessageJson.put("timestamp", logMessage.getTimestamp());
                                            array.put(logMessageJson);
                                        }
                                        object.put("logs", array);
                                        Lumm.net.doLNetRequest(addLog, object);
                                    } catch (JSONException exception) {
                                        //TODO handle this
                                    }

                                }
                            };

                        } catch (JSONException exception) {
                            //TODO handle this
                        }

                    } else {
                        //TODO handle exception
                    }

                } else if (wrapper.action.equals(Net.addError)) {
                    if (wrapper.success && wrapper.fullObject.has("object")) {
                        try {
                            String is = wrapper.fullObject.getString("object");
                            LogMessage message = Lumm.debug.new LogMessage(Debug.Log.LOG_ERROR, DEFAULT_TAG, wrapper.message, is);
                            Lumm.debug.currentLog.addLogMessage(message);
                        } catch (JSONException exception) {
                            //TODO handle this
                        }
                    }
                }
            }

            @Override
            public void failed(Throwable t) {
                Lumm.debug.log(t.getMessage(), t);
            }

            @Override
            public void cancelled() {
                Lumm.debug.log(DEFAULT_TAG, "Cancelled requests", null);
            }
        };
    }

    @Override
    public void onCreate() {

        requestQueue = new HashSet<Net.NetRequestQueue>();

        onDisposeListener = new OnDisposeListener<LummModule>() {

            @Override
            public void onDispose(LummModule caller) {

                Iterator<NetRequestQueue> requestQueueIterator = requestQueue.iterator();
                while (requestQueueIterator.hasNext()) {

                    NetRequestQueue queue = requestQueueIterator.next();
                    queue.finish(true);
                }

            }
        };

        if (this.startSessionAutomatically) {
            startLNet();
            getLNetSession();
        }


    }

    @Override
    public void onUpdate() {
        // TODO Auto-generated method stub

        // it's going to expire soon
        if (isInSession() && session.timeEnd + TIME_BEFORE_SESSION_REFRESH < System.currentTimeMillis())
            getLNetSession();

    }

    public boolean isInSession() {
        if (session != null && session.timeDeleted == 0) {
            return System.currentTimeMillis() < session.timeEnd;
        } else {
            session = null;
        }
        return false;

    }

    public NetRequestQueue createRequestQueue() {
        NetRequestQueue queue = new NetRequestQueue();
        requestQueue.add(queue);
        return queue;
    }

    public void getLNetSession() {
        try {
            String action = getSession;
            JSONObject object = new JSONObject();
            if (isInSession()) {
                object.put("id", session.id);
            } else {
                session = null;
            }
            object.put("appKey", appKey);

            doLNetRequest(action, object);
        } catch (JSONException exception) {
            //TODO handle this
        }

    }

    private void doLNetRequest(String action, JSONObject object) {
        if (lNetRequestQueue == null) {
            Lumm.debug.logError(
                    "Attempting to to L-Net request before starting the L-Net request queue. Request Queue will be created.",
                    null);
            startLNet();
            return;
        }

        if (appVersion == null || appKey == null) {
            Lumm.debug.logError(
                    "Attempting to do L-Net request withouth setting LummConfiguration.appVersion and LummConfiguration.appKey first, request will be ignored",
                    null);
            return;
        }

        try {
            if (object == null)
                object = new JSONObject();
            object.put("action", action);
            object.put("appVersion", appVersion);
            object.put("platform", Utility.getPlatform());
            object.put("osVersion", Integer.toString(Gdx.app.getVersion()));
            object.put("debug", debug);
        } catch (JSONException exception) {
            //TODO handle this
        }
        Lumm.debug.log("Request message: " + object.toString(), null);

        HttpRequestBuilder builder = new HttpRequestBuilder().newRequest().url(serverPath).method(HttpMethods.POST)
                .header(HttpRequestHeader.ContentType, "application/json").content(object.toString());

        lNetRequestQueue.addRequest(builder.build(), lNetListener);

    }

    public void startLNet() {
        if (lNetRequestQueue != null) {
            Lumm.debug.logError("Attempting to start LNet after it has already been started, request will be ignored",
                    null);
            return;
        }
        lNetRequestQueue = createRequestQueue();
        lNetRequestQueue.startQueue();


    }

    public void logThrowable(Throwable throwable) {
        if (session == null || session.id == null) {
            return;
            //TODO cache
        }
        try {
            JSONObject object = new JSONObject();
            object.put("message", throwable.getMessage());
            object.put("timestamp", System.currentTimeMillis());
            object.put("userSessionId", session.id);
            object.put("class", throwable.getClass().getName());
            JSONArray array = new JSONArray();
            for (int i = 0; i < throwable.getStackTrace().length; i++) {
                StackTraceElement element = throwable.getStackTrace()[i];
                if (element == null)
                    continue;

                JSONObject logMessageJson = new JSONObject();

                if (element.getClassName() != null)
                    logMessageJson.put("className", element.getClassName());

                if (element.getFileName() != null)
                    logMessageJson.put("fileName", element.getFileName());

                if (element.getMethodName() != null)
                    logMessageJson.put("methodName", element.getMethodName());

                logMessageJson.put("isNativeMethod", element.isNativeMethod());
                logMessageJson.put("lineNumber", element.getLineNumber());

                array.put(logMessageJson);
            }

            object.put("stackTrace", array);

            Lumm.net.doLNetRequest(addError, object);

        } catch (JSONException exception) {
            //TODO handle this
        }
    }

    @Override
    public List<Class<? extends LummModule>> getDependencies() {
        return null;
    }

}
