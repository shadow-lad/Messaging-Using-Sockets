package org.shardav.server.handler;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.shardav.server.Server;
import org.shardav.server.comms.Request.RequestType;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.mail.GMailService;
import org.shardav.server.sql.Database;
import org.shardav.utils.Log;

import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Random;

public class RegistrationHandler implements Runnable {

    private static final String LOG_TAG = Server.class.getSimpleName() + ":" + RegistrationHandler.class.getSimpleName();

    final private Socket client;
    final private BufferedReader in;
    final private PrintWriter out;
    final private UserDetails details;

    private String OTP;

    public RegistrationHandler(final Socket client, final BufferedReader in, final PrintWriter out, final UserDetails details){

        this.client = client;
        this.in = in;
        this.out = out;
        this.details = details;

    }

    @Override
    public void run() {

        Response response = new Response(ResponseStatus.FAILED);

        StringBuilder OTP = new StringBuilder(String.valueOf(new Random().nextInt(10000)));
        while(OTP.length() < 4)
            OTP.append("0");

        try {
            GMailService.sendRegistrationOTP(details.getEmail(), OTP.toString());
            response.setResponseStatus(ResponseStatus.SENT);
            response.setMessage("OTP sent to "+details.getEmail());
            this.OTP = OTP.toString();
        } catch (GeneralSecurityException | MessagingException | IOException ex){
            Log.e(LOG_TAG, "An error occurred while trying to send OTP "+ex.getMessage());
            response.setMessage(ex.getMessage());
        }

        out.println(response.toJSON());

        if(response.getResponseStatus() == ResponseStatus.FAILED)
            disconnect();
        else
            login();

    }

    private void disconnect(){
        try {
            client.close();
            in.close();
            out.close();
        } catch (IOException ex){
            Log.e(LOG_TAG, "An error occurred while trying to disconnect "+client.getInetAddress()+": "+ex.getMessage());
        }
    }

    private void login(){

        try {
            String json = in.readLine();

            JSONObject verify = new JSONObject(new JSONTokener(json));

            if(verify.has("request")
                    && RequestType.getRequestType(verify.getString("request")) == RequestType.VERIFY){

                if(verify.has("otp")
                    && verify.getString("otp")!=null){

                    if(this.OTP.equals(verify.getString("otp"))) {

                        try {
                            //TODO: Ponder upon how to properly handle this and then do it
                            Database database = Database.getInstance("root", "toor");
                            database.insertUser(details);
                        } catch (SQLException ex){
                            ex.printStackTrace();
                            return;
                        }

                        ClientHandler clientHandler = new ClientHandler(client, details.getEmail(), in, out);
                        Server.activeClients.add(clientHandler);
                        new Thread(clientHandler).start();

                    } else {
                        Response failed = new Response(ResponseStatus.FAILED, "Invalid OTP");
                        out.println(failed.toJSON());
                    }

                } else {
                    Response errorResponse = new Response(ResponseStatus.INVALID, "Key OTP should be present");
                    out.println(errorResponse.toJSON());
                }

            } else {
                Response errorResponse = new Response(ResponseStatus.INVALID, "Excepted an request of type verify");
                out.println(errorResponse.toJSON());

            }

        } catch (IOException ex){
            Log.e(LOG_TAG, "Error Reading from "+details.getEmail()+"'s input stream",ex);
        }

    }
}
