package com.dji.sdk.sample.internal.controller;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.OnScreenJoystickListener;
import com.dji.sdk.sample.internal.utils.DialogUtils;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.OnScreenJoystick;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import dji.common.error.DJIError;
import dji.common.flightcontroller.simulator.InitializationData;
import dji.common.flightcontroller.simulator.SimulatorState;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.mobilerc.MobileRemoteController;
import dji.sdk.products.Aircraft;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static dji.common.gimbal.Rotation.NO_ROTATION;
import static java.lang.Thread.sleep;


public class ChatBoxActivity extends AppCompatActivity {

    private Socket socket;
    private OnScreenJoystick screenJoystickLeft;
    private MobileRemoteController mobileRemoteController;
    private ToggleButton btnSimulator;
    private FlightControllerKey isSimulatorActived;
    private TextView textView;

    public void leftStick(float pX, float pY) {
        try {
            mobileRemoteController =
                    ((Aircraft) DJISampleApplication.getAircraftInstance()).getMobileRemoteController();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (Math.abs(pX) < 0.02) {
            pX = 0;
        }

        if (Math.abs(pY) < 0.02) {
            pY = 0;
        }

        if (mobileRemoteController != null) {
            mobileRemoteController.setLeftStickHorizontal(pX);
            mobileRemoteController.setLeftStickVertical(pY);
        }
    }


    public void rightStick(float pX, float pY) {
        try {
            mobileRemoteController =
                    ((Aircraft) DJISampleApplication.getAircraftInstance()).getMobileRemoteController();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (Math.abs(pX) < 0.02) {
            pX = 0;
        }

        if (Math.abs(pY) < 0.02) {
            pY = 0;
        }
        if (mobileRemoteController != null) {
            mobileRemoteController.setRightStickHorizontal(pX);
            mobileRemoteController.setRightStickVertical(pY);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_chat_box);

        try {
            socket = IO.socket("http://kamino-google-home.francecentral.cloudapp.azure.com:8002");
            socket.connect();
            socket.emit("connect", "connecté");
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }



        socket.on("Here is some data", new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String message = (String)args[0];
                        try {
                            //extract data from fired event
                            int i = 0;


                            //String message = data.getString("message");
                            //String[] splited = message.split(" ");
                            //message = splited[0];

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
                            // à tester
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
                            else if (message.equals("forceLand")){
                                Toast.makeText(ChatBoxActivity.this, "forceLand", Toast.LENGTH_SHORT).show();

                                if (flightController == null) {
                                    return;
                                }
                                flightController.confirmLanding(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        DialogUtils.showDialogBasedOnError(ChatBoxActivity.this, djiError);
                                    }
                                });
                            }


                            else if (message.equals("yawLeft")){

                                Toast.makeText(ChatBoxActivity.this, "yawLeft", Toast.LENGTH_SHORT).show();
                                leftStick(-5,0);
                                Thread.sleep(1000);
                                leftStick(0,0);
                            }
                            else if (message.equals("yawRight")){

                                Toast.makeText(ChatBoxActivity.this, "yawRight", Toast.LENGTH_SHORT).show();
                                leftStick(5,0);
                                Thread.sleep(1000);
                                leftStick(0,0);
                            }

                            //TEST CASQUE

                            else if (message.equals("RotateLeft")){
                                // YAW LEFT
                                Toast.makeText(ChatBoxActivity.this, "RotateLeft", Toast.LENGTH_SHORT).show();
                                leftStick(-5,0);
                                Thread.sleep(1000);
                                leftStick(0,0);
                            }
                            else if (message.equals("RotateRight")){
                                // YAW RIGHT
                                Toast.makeText(ChatBoxActivity.this, "RotateRight", Toast.LENGTH_SHORT).show();
                                leftStick(5,0);
                                Thread.sleep(1000);
                                leftStick(0,0);
                            }

                            //

                            else if (message.equals("yawLeft1")){

                                Toast.makeText(ChatBoxActivity.this, "yawLeft", Toast.LENGTH_SHORT).show();
                                leftStick(-5,0);

                            }
                            else if (message.equals("yawRight1")){

                                Toast.makeText(ChatBoxActivity.this, "yawRight", Toast.LENGTH_SHORT).show();
                                leftStick(5,0);

                            }

                            else if (message.equals("stopYaw")){

                                Toast.makeText(ChatBoxActivity.this, "stopYaw", Toast.LENGTH_SHORT).show();
                                leftStick(0,0);

                            }

                            else if (message.equals("up")){

                                Toast.makeText(ChatBoxActivity.this, "up", Toast.LENGTH_SHORT).show();
                                leftStick(0,3);
                                Thread.sleep(100);
                                leftStick(0,0);
                            }
                            else if (message.equals("down")){

                                Toast.makeText(ChatBoxActivity.this, "down", Toast.LENGTH_SHORT).show();
                                leftStick(0,-3);
                                Thread.sleep(100);
                                leftStick(0,0);
                            }

                            else if (message.equals("forward")){

                                Toast.makeText(ChatBoxActivity.this, "forward", Toast.LENGTH_SHORT).show();
                                rightStick(0,5);
                                Thread.sleep(200);
                                rightStick(0,0);
                            }
                            else if (message.equals("forward1")){

                                Toast.makeText(ChatBoxActivity.this, "forward", Toast.LENGTH_SHORT).show();
                                rightStick(0,10);
                                Thread.sleep(200);
                                rightStick(0,0);
                            }
                            else if (message.equals("forward2")){

                                Toast.makeText(ChatBoxActivity.this, "forward", Toast.LENGTH_SHORT).show();
                                rightStick(0,5);
                                Thread.sleep(500);
                                rightStick(0,0);
                            }
                            else if (message.equals("backward")){

                                Toast.makeText(ChatBoxActivity.this, "backward", Toast.LENGTH_SHORT).show();
                                rightStick(0,-5);
                                Thread.sleep(200);
                                rightStick(0,0);
                            }
                            else if (message.equals("backward1")){

                                Toast.makeText(ChatBoxActivity.this, "backward", Toast.LENGTH_SHORT).show();
                                rightStick(0,-10);
                                Thread.sleep(200);
                                rightStick(0,0);
                            }
                            else if (message.equals("backward2")){

                                Toast.makeText(ChatBoxActivity.this, "backward", Toast.LENGTH_SHORT).show();
                                rightStick(0,-10);
                                Thread.sleep(500);
                                rightStick(0,0);
                            }
                            else if (message.equals("right")){

                                Toast.makeText(ChatBoxActivity.this, "right", Toast.LENGTH_SHORT).show();
                                rightStick(3,0);
                                Thread.sleep(100);
                                rightStick(0,0);
                            }
                            else if (message.equals("left")){

                                Toast.makeText(ChatBoxActivity.this, "left", Toast.LENGTH_SHORT).show();
                                rightStick(-3,0);
                                Thread.sleep(100);
                                rightStick(0,0);
                            }
                            else if (message.equals("stopAll")){

                                Toast.makeText(ChatBoxActivity.this, "stopAll", Toast.LENGTH_SHORT).show();
                                rightStick(0,0);
                                leftStick(0,0);
                            }

                            else {
                                Toast.makeText(ChatBoxActivity.this, '_'+message+'_', Toast.LENGTH_SHORT).show();
                                rightStick(0,0);
                                leftStick(0,0);
                            }
/*
                            if (message.equals("atterissage")) {

                                Toast.makeText(ChatBoxActivity.this, "atterissage", Toast.LENGTH_SHORT).show();

                            }
*/
                        } //catch (JSONException e) {
                            //e.printStackTrace();
                       // }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });


    }
    protected void onPause() {
        super.onPause();
        Toast.makeText(ChatBoxActivity.this, "onPause", Toast.LENGTH_SHORT).show();
        //reload();

    }

    protected void onResume() {
        super.onResume();
        Toast.makeText(ChatBoxActivity.this, "onResume", Toast.LENGTH_SHORT).show();
        //reload();

    }

    protected void onRestart() {
        super.onRestart();
        Toast.makeText(ChatBoxActivity.this, "onRestart", Toast.LENGTH_SHORT).show();
        //reload();

    }


    protected void onStop() {
        super.onStop();
        Toast.makeText(ChatBoxActivity.this, "onStop", Toast.LENGTH_SHORT).show();
       // reload();

    }

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        socket.disconnect();
        Toast.makeText(ChatBoxActivity.this, "OnDestroy", Toast.LENGTH_SHORT).show();
        //reload();
    }

}
