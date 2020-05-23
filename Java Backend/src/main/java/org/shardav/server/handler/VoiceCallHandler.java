package org.shardav.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.shardav.server.Server;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseEvent;
import org.shardav.server.comms.Response.ResponseType;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.comms.voice.VoiceCall.VoiceEvents;
import org.shardav.server.comms.voice.VoiceResponse;

import java.util.concurrent.TimeUnit;

public class VoiceCallHandler {

    final private ClientHandler client;
    private final Gson gson;
    private boolean isTimedOut;
    private boolean isOnVoiceCall;
    private Thread timeOut;

    private UserDetails otherClient;

    public VoiceCallHandler(ClientHandler client) {

        this.client = client;
        this.gson = new Gson();
        this.timeOut = new Thread(this::timeOut);
        this.otherClient = null;

    }

    void handleJson(JsonObject root) {
        VoiceEvents event = VoiceEvents.valueOf(root.getAsJsonPrimitive("event").getAsString());
        switch (event) {
            case call:
                handleCallEvent(gson.fromJson(root.getAsJsonObject("details"), UserDetails.class));
                break;
            case accept:
            case reject:
            default:

        }
    }

    void handleCallEvent(UserDetails details) {
        if (details.hasEmail()) {
            VoiceResponse<UserDetails> voiceResponse = new VoiceResponse<>(VoiceEvents.call, details);

            if (!isOnVoiceCall) {
                ClientHandler clientToCall = Server.CLIENT_MAP.getOrDefault(details.getEmail(), null);
                if (clientToCall == null || !clientToCall.isLoggedIn) {
                    voiceResponse.setEvent(VoiceEvents.offline);
                    client.out.println(gson.toJson(voiceResponse));
                    client.out.flush();
                } else if (clientToCall.isOnVoiceCall()) {
                    Response<UserDetails> failedResponse = new Response<>(ResponseEvent.failed, ResponseType.voice);
                    failedResponse.setMessage("User is already on another call");
                    failedResponse.setDetails(details);
                    client.out.println(gson.toJson(failedResponse));
                    client.out.flush();
                } else {
                    voiceResponse.setEvent(VoiceEvents.request);
                    voiceResponse.setDetails(new UserDetails(client.getEmail(), null));
                    clientToCall.out.println(gson.toJson(voiceResponse));
                    clientToCall.out.flush();
                    clientToCall.startTimeOut();
                    this.isTimedOut = false;
                    this.otherClient = details;
                    startTimeOut();
                }
            }

        } else {
            Response<Void> errorResponse = new Response<>(ResponseEvent.failed, ResponseType.voice,
                    "Provide the email of the user to be called.");
            client.out.println(gson.toJson(errorResponse));
            client.out.flush();
        }
    }

    void handleAcceptEvent(UserDetails clientWhoCalled) {
        if (clientWhoCalled.hasEmail()) {
            if (!isTimedOut) {
                this.timeOut.interrupt();
                ClientHandler clientToAccept = Server.CLIENT_MAP.getOrDefault(clientWhoCalled.getEmail(), null);
                if (clientToAccept == null || !clientToAccept.isLoggedIn) {
                    VoiceResponse<UserDetails> voiceResponse = new VoiceResponse<>(VoiceEvents.offline, clientWhoCalled);
                    client.out.println(gson.toJson(voiceResponse));
                    client.out.flush();
                } else {
                    // Send IP details
                }
            }
        } else {
            Response<Void> errorResponse = new Response<>(ResponseEvent.failed, ResponseType.voice,
                    "Provide the email of the user whose call is to be accepted.");
            client.out.println(gson.toJson(errorResponse));
            client.out.flush();
        }
    }

    void handleRejectEvent(UserDetails details) {
        if (details.hasEmail()) {
            if (!isTimedOut) {
                this.timeOut.interrupt();
                this.isOnVoiceCall = false;
                this.isTimedOut = false;
                ClientHandler clientToAccept = Server.CLIENT_MAP.getOrDefault(details.getEmail(), null);
                if (clientToAccept == null || !clientToAccept.isLoggedIn) {
                    VoiceResponse<UserDetails> voiceResponse = new VoiceResponse<>(VoiceEvents.offline, details);
                    client.out.println(gson.toJson(voiceResponse));
                    client.out.flush();
                } else {
                    VoiceResponse<UserDetails> reject = new VoiceResponse<>(VoiceEvents.reject, details);
                    client.out.println(gson.toJson(reject));
                    client.out.flush();
                }
            }
        } else {
            Response<Void> errorResponse = new Response<>(ResponseEvent.failed, ResponseType.voice,
                    "Provide the email of the user whose call is to be rejected.");
            client.out.println(gson.toJson(errorResponse));
            client.out.flush();
        }
    }

    void startTimeOut() {
        if (this.timeOut.isInterrupted()) {
            this.timeOut = new Thread(this::timeOut);
        }
        this.timeOut.start();
    }

    private void timeOut() {
        try {
            this.isOnVoiceCall = true;
            this.isTimedOut = false;
            TimeUnit.SECONDS.sleep(30);
            this.isTimedOut = true;
            this.isOnVoiceCall = false;
            VoiceResponse<UserDetails> timedOut = new VoiceResponse<>(VoiceEvents.time, this.otherClient);
            client.out.println(gson.toJson(timedOut));
            client.out.flush();
        } catch (InterruptedException ignore) {
        }
    }

    boolean isOnVoiceCall() {
        return this.isOnVoiceCall;
    }
}
