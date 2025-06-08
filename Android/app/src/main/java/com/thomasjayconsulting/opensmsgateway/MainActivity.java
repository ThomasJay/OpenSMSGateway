package com.thomasjayconsulting.opensmsgateway;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements MessageListenerInterface {

    // To setup this application you need the phone number, an API End points to send SMS Messages that have been received and one to poll for outbound messages
    // as well as an API key
    public static String phoneNumber = "+151055047976";
    //public static String phoneNumber = "+15105045395";
    private static final String API_QUERY_SMS = "http://10.10.10.13:8181/checkSMSSendQueue";
    public static final String API_RECEIVED_SMS = "http://10.10.10.13:8181/receivedSMS";
    private static String apiKey = "xxyyzz";

    private static final int REQUEST_CODE = 43391;

    private static int pollIndicator = 0;

    private static final int POLL_INTERVAL = 1000; // Time interval to check for new outbound messages

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("TJ", "phoneNumber: " + phoneNumber );

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        MessageBroadcastReceiver.bindListener(this);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 100);
        }

        // Fire off polling to check for send data
        handler.postDelayed(runPollingTask, POLL_INTERVAL);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkForSmsReceivePermissions();

    }

    public void messageReceived(String sender, String message) {

        Log.d("TJ", "messageReceived SMS sender: " + sender + "  message: " +  message);

       sendMsgReceived(sender, message);

    }


    void checkForSmsReceivePermissions(){

        // Check if App already has permissions for receiving SMS
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.RECEIVE_SMS") == PackageManager.PERMISSION_GRANTED) {
            // App has permissions to listen incoming SMS messages

        } else {
            // App does not have permissions to listen incoming SMS messages
            // Request permissions from user
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECEIVE_SMS}, REQUEST_CODE);
        }
    }

    Runnable runPollingTask = new Runnable() {
        @Override
        public void run() {

            if (pollIndicator++ > 5) {
                Log.d("TJ", "Poll");
                pollIndicator = 0;
            }

            try {
                checkMsgToSend();
            }
            catch (Exception e) {
                Log.d("TJ", "Poll Exception : " + e.getMessage());
            }

            handler.postDelayed(runPollingTask, POLL_INTERVAL);
        }
    };

    private void  checkMsgToSend() {
        String url = API_QUERY_SMS + "/" + phoneNumber;

        new Thread(new Runnable() {

            @Override
            public void run() {


                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("apiKey", apiKey);
                } catch (JSONException e) {
                    //e.printStackTrace();
                }


                RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));


                Request request = new Request.Builder()
                        .header("Content-Type", "application/json")
                        .url(url)
                        .post(body)
                        .build();


                OkHttpClient client = new OkHttpClient();

                Call call = client.newCall(request);

                Response response = null;

                try {
                    //  Log.d("TJ", "Poll send post");

                    response = call.execute();

                    int code = response.code();




                    //  Log.d("TJ", "Poll Code: " + code);

                    if (code != 200) {
                        Log.d("TJ", "Code: " + code);
                        return;
                    }


                    String serverResponse = response.body().string();

                    //Log.d("TJ", "Poll serverResponse: " + serverResponse);


                    if (serverResponse == null || serverResponse.length() != 0) {

                        // Log.d("TJ", "Poll processing response");

                        //Log.d("TJ", "serverResponse: " + serverResponse);
                        JSONObject root = new JSONObject(serverResponse);
                        JSONArray items = root.getJSONArray("items");


                        //Log.d("TJ", "got items len: " + items.length());

                        for (int i=0;i<items.length();i++) {
                            JSONObject item = (JSONObject)items.get(i);

                            String message = item.getString("message");
                            String sender = item.getString("sender");


                            MessageInfo messageInfo = new MessageInfo();
                            messageInfo.setMessage(message);
                            messageInfo.setSender(sender);

                            sendSMS(messageInfo);

                        }






                    }



                } catch (IOException e) {
                    Log.d("TJ", "IOException Error: " + e.getMessage());
                }
                catch (JSONException e) {
                    Log.d("TJ", "JSOn Map Error: " + e.getMessage());
                }


            }


        }).start();

    }

    private void sendSMS(MessageInfo messageInfo) {

        String phone = messageInfo.getSender();
        String message = messageInfo.getMessage();

        int len = message.length();


        // Log.d("TJ", "sendSMS prepare len: " + len + " phone: " + phone + " message: " + message);


        try {
            SmsManager smsManager  = SmsManager.getDefault();

            if (len < 140) {
                smsManager.sendTextMessage(phone, null, message, null, null);
            }
            else {
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(phone, null, parts, null, null);
            }


            Log.d("TJ", "sendSMS UI Thread sent message: " + message);

        }
        catch (Exception e) {
            Log.d("TJ", "sendSMS UI Thread failed: " + e.getMessage());
        }


    }

    private void sendMsgReceived(String sender, String message) {

        String url = MainActivity.API_RECEIVED_SMS;

        new Thread(new Runnable() {

            @Override
            public void run() {
                //RequestBody postForm = new FormBody.Builder().add("name", "Tom").build();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("phoneNumber", phoneNumber);
                    jsonObject.put("sender", sender);
                    jsonObject.put("message", message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));


                Request request = new Request.Builder()
                        .header("Content-Type", "application/json")
                        .url(url)
                        .post(body)
                        .build();


                OkHttpClient client = new OkHttpClient();

                Call call = client.newCall(request);

                Response response = null;

                try {
                    response = call.execute();

                    int code = response.code();

                    //Log.d("TJ", "recevied sms sent code: " + code);



                    //String serverResponse = response.body().string();

                    //Log.d("Handlers", "serverResponse: " + serverResponse);

                    //JSONObject root = new JSONObject(serverResponse);

                    //String name = root.getString("name");




                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }


        }).start();

    }


}