package com.dji.sdk.sample.internal.controller;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.utils.DialogUtils;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;

public class ChatBoxActivity extends AppCompatActivity {

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_chat_box);

        try {
            socket = IO.socket("http://192.168.86.246:3000");
            socket.connect();
            socket.emit("join", "connecté");
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }



        socket.on("newmessage", new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        JSONObject data = (JSONObject) args[0];
                        try {
                            //extract data from fired event
                            int i = 0;


                            String message = data.getString("message");
                            String[] splited = message.split(" ");
                            message = splited[0];

                            FlightController flightController = ModuleVerificationUtil.getFlightController();
                            /*if (flightController == null) {
                                return;
                            }*/

                            //Toast.makeText(ChatBoxActivity.this, '_' + message + i + '_', Toast.LENGTH_SHORT).show();
                            if (message.equals("takeOff") )
                            {
                                Toast.makeText(ChatBoxActivity.this, "takeOff", Toast.LENGTH_SHORT).show();
                                if (flightController == null) {
                                    return;
                                }
                                        flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                                            @Override
                                            public void onResult(DJIError djiError) {
                                                DialogUtils.showDialogBasedOnError(ChatBoxActivity.this, djiError);
                                            }

                                        });


                            }

                            else if (message.equals("landing")) {

                                Toast.makeText(ChatBoxActivity.this, "landing", Toast.LENGTH_SHORT).show();
                                if (flightController == null) {
                                    return;
                                }
                                flightController.startLanding(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        DialogUtils.showDialogBasedOnError(ChatBoxActivity.this, djiError);
                                    }
                                });

                            }
                            else {

                                Toast.makeText(ChatBoxActivity.this, '_'+message+'_', Toast.LENGTH_SHORT).show();

                            }
/*
                            if (message.equals("atterissage")) {

                                Toast.makeText(ChatBoxActivity.this, "atterissage", Toast.LENGTH_SHORT).show();

                            }
*/

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                     }
                });
            }
        });


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        socket.disconnect();
    }
}
