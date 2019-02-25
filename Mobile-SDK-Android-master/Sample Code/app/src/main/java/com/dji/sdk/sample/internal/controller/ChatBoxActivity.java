package com.dji.sdk.sample.internal.controller;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.SeekBarValueChangeListener;
import com.dji.sdk.sample.internal.utils.DialogUtils;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.dji.sdk.sample.internal.utils.VideoFeedView;
import com.dji.sdk.sample.internal.view.PopupSeekBar;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.keysdk.CameraKey;
import dji.keysdk.KeyManager;
import dji.keysdk.callback.SetCallback;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mobilerc.MobileRemoteController;
import dji.sdk.products.Aircraft;

import static dji.midware.data.manager.P3.ServiceManager.getContext;


public class ChatBoxActivity extends AppCompatActivity {

    private Socket socket;
    private MobileRemoteController mobileRemoteController;
    private static final String TAG = "ChatBoxActivity";
    FlightController flightController = ModuleVerificationUtil.getFlightController();
    TextView mEdit;

    private final int VISUAL_CAMERA_INDEX = 0;
    private final int THERMAL_CAMERA_INDEX = 2;
    private PopupSeekBar popupSeekBar;
    private TextView primaryVideoFeedTitle;
    private VideoFeedView primaryVideoFeed;
    private VideoFeeder.PhysicalSourceListener sourceListener;

    private void setCameraMSXLevel(int val1) {
        KeyManager.getInstance().setValue(CameraKey.create(CameraKey.MSX_LEVEL, THERMAL_CAMERA_INDEX), val1, new SetCallback() {
            @Override
            public void onSuccess() {
                ToastUtils.setResultToToast("Success");
            }

            @Override
            public void onFailure(@NonNull DJIError error) {
                ToastUtils.setResultToToast("Failed. " + error.toString());
            }
        });
    }

    private void confirmTakingOff() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Taking Off");
        builder.setMessage("You are about to takeoff the aircraft. Do you really want to proceed ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (flightController == null) {
                    Toast.makeText(ChatBoxActivity.this, "Device not connected", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(ChatBoxActivity.this, "takeOff", Toast.LENGTH_SHORT).show();
                //
                // Fonction très sensible du code
                //
                flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        DialogUtils.showDialogBasedOnError(ChatBoxActivity.this, djiError);
                    }

                });
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "canceled landing", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }


    private void initUI() {

        primaryVideoFeedTitle = (TextView) findViewById(R.id.video_feed_title);
        primaryVideoFeed = (VideoFeedView) findViewById(R.id.primary_video_feed);
        popupSeekBar = new PopupSeekBar(getContext(), 0, 100, "MSX Level", new SeekBarValueChangeListener() {
            @Override
            public void onValueChange(int val1, int val2) {
                setCameraMSXLevel(val1);
                popupSeekBar.dismiss();
            }
        }, 300, 150, 0);

    }

    private void setUpListeners() {
        sourceListener = new VideoFeeder.PhysicalSourceListener() {
            @Override
            public void onChange(VideoFeeder.VideoFeed videoFeed, VideoFeeder.PhysicalSource newPhysicalSource) {
                if (videoFeed == VideoFeeder.getInstance().getPrimaryVideoFeed()) {
                    String newText = "Primary Source: " + newPhysicalSource.toString();
                    ToastUtils.setResultToText(primaryVideoFeedTitle, newText);
                }
            }
        };
        primaryVideoFeed.registerLiveVideo(VideoFeeder.getInstance().getPrimaryVideoFeed(), true);
        String newText = "Primary Source: " + VideoFeeder.getInstance().getPrimaryVideoFeed().getVideoSource().name();
        ToastUtils.setResultToText(primaryVideoFeedTitle, newText);
        VideoFeeder.getInstance().addPhysicalSourceListener(sourceListener);
    }

    private void init(Context context) {
        initUI();
        setUpListeners();
    }

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void alwaysPreferNetworksWith(@NonNull int[] capabilities, @NonNull int[] transportTypes) {

        NetworkRequest.Builder request = new NetworkRequest.Builder();

        // add capabilities
        for (int cap: capabilities) {
            request.addCapability(cap);
        }

        // add transport types
        for (int trans: transportTypes) {
            request.addTransportType(trans);
        }

        final ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);

        connectivityManager.registerNetworkCallback(request.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        ConnectivityManager.setProcessDefaultNetwork(network);
                    } else {
                        connectivityManager.bindProcessToNetwork(network);
                    }
                } catch (IllegalStateException e) {
                    Log.e(TAG, "ConnectivityManager.NetworkCallback.onAvailable: ", e);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);
        mEdit   = (TextView) findViewById(R.id.textView);
        mEdit.setMovementMethod(new ScrollingMovementMethod());

        int[] capabilities = new int[]{ NetworkCapabilities.NET_CAPABILITY_INTERNET };
        int[] transportTypes = new int[]{ NetworkCapabilities.TRANSPORT_CELLULAR };

        alwaysPreferNetworksWith(capabilities, transportTypes);
        init(getApplicationContext());
        try {

            //socket = IO.socket("http://kamino-google-home.francecentral.cloudapp.azure.com:8002");
            socket = IO.socket("http://testserverbmi.westeurope.cloudapp.azure.com:3000");
            socket.connect();
            socket.emit("join", "connecté");
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
        Button Take_Off = (Button) findViewById(R.id.Take_off);
        Take_Off.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confirmTakingOff();
            }
        });

        Button Button_Camera_View = (Button) findViewById(R.id.Camera_view);

        final com.dji.sdk.sample.internal.utils.VideoFeedView camera = (com.dji.sdk.sample.internal.utils.VideoFeedView) findViewById(R.id.primary_video_feed);
        final ImageView Image_View = (ImageView)  findViewById(R.id.imageView4);

        Image_View.setVisibility(View.VISIBLE);
        camera.setVisibility(View.GONE);
        Button_Camera_View.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (camera.getVisibility()==View.GONE) {
                    Image_View.setVisibility(View.GONE);
                    camera.setVisibility(View.VISIBLE);
                }

                else if (camera.getVisibility()==View.VISIBLE ) {
                    camera.setVisibility(View.GONE);
                    Image_View.setVisibility(View.VISIBLE); }
            }
        });

        Button Landing = (Button) findViewById(R.id.Landing);
        Landing.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (flightController == null) {

                    Toast.makeText(ChatBoxActivity.this, "Device not connected", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(ChatBoxActivity.this, "landing", Toast.LENGTH_SHORT).show();

                flightController.startLanding(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        DialogUtils.showDialogBasedOnError(ChatBoxActivity.this, djiError);
                    }
                });            }
        });

        socket.on("newmessage", new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //String message = (String)args[0];
                        JSONObject data = (JSONObject) args[0];
                        try {
                            //extract data from fired event
                            String message = data.getString("message");
                            String[] splited = message.split(" ");
                            message = splited[0];

                            mEdit.append("\n"+message);
                            if (message.equals("takeOff")) {
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

                            } else if (message.equals("landing")) {

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

                            } else if (message.equals("forceLand")) {
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
                            } else if (message.equals("yawLeft")) {

                                Toast.makeText(ChatBoxActivity.this, "yawLeft", Toast.LENGTH_SHORT).show();
                                leftStick(-5, 0);
                                Thread.sleep(1000);
                                leftStick(0, 0);
                            } else if (message.equals("yawRight")) {

                                Toast.makeText(ChatBoxActivity.this, "yawRight", Toast.LENGTH_SHORT).show();
                                leftStick(5, 0);
                                Thread.sleep(1000);
                                leftStick(0, 0);
                            }

                            //TEST CASQUE
                            else if (message.equals("RotateLeft")) {
                                // YAW LEFT
                                Toast.makeText(ChatBoxActivity.this, "RotateLeft", Toast.LENGTH_SHORT).show();
                                leftStick(-5, 0);
                                Thread.sleep(1000);
                                leftStick(0, 0);
                            } else if (message.equals("RotateRight")) {
                                // YAW RIGHT
                                Toast.makeText(ChatBoxActivity.this, "RotateRight", Toast.LENGTH_SHORT).show();
                                leftStick(5, 0);
                                Thread.sleep(1000);
                                leftStick(0, 0);
                            }

                            else if (message.equals("yawLeft1")) {

                                Toast.makeText(ChatBoxActivity.this, "yawLeft", Toast.LENGTH_SHORT).show();
                                leftStick(-5, 0);

                            } else if (message.equals("yawRight1")) {

                                Toast.makeText(ChatBoxActivity.this, "yawRight", Toast.LENGTH_SHORT).show();
                                leftStick(5, 0);

                            } else if (message.equals("stopYaw")) {

                                Toast.makeText(ChatBoxActivity.this, "stopYaw", Toast.LENGTH_SHORT).show();
                                leftStick(0, 0);

                            } else if (message.equals("up")) {

                                Toast.makeText(ChatBoxActivity.this, "up", Toast.LENGTH_SHORT).show();
                                leftStick(0, 3);
                                Thread.sleep(100);
                                leftStick(0, 0);
                            } else if (message.equals("down")) {

                                Toast.makeText(ChatBoxActivity.this, "down", Toast.LENGTH_SHORT).show();
                                leftStick(0, -3);
                                Thread.sleep(100);
                                leftStick(0, 0);
                            } else if (message.equals("forward1")) {

                                Toast.makeText(ChatBoxActivity.this, "forward", Toast.LENGTH_SHORT).show();
                                rightStick(0, 10);
                                Thread.sleep(200);
                                rightStick(0, 0);
                            } else if (message.equals("forward")) {

                                Toast.makeText(ChatBoxActivity.this, "forward", Toast.LENGTH_SHORT).show();
                                rightStick(0, 5);
                                Thread.sleep(500);
                                rightStick(0, 0);
                            } else if (message.equals("backward")) {

                                Toast.makeText(ChatBoxActivity.this, "backward", Toast.LENGTH_SHORT).show();
                                rightStick(0, -10);
                                Thread.sleep(500);
                                rightStick(0, 0);
                            } else if (message.equals("backward1")) {

                                Toast.makeText(ChatBoxActivity.this, "backward", Toast.LENGTH_SHORT).show();
                                rightStick(0, -10);
                                Thread.sleep(200);
                                rightStick(0, 0);
                            }  else if (message.equals("right")) {

                                Toast.makeText(ChatBoxActivity.this, "right", Toast.LENGTH_SHORT).show();
                                rightStick(3, 0);
                                Thread.sleep(100);
                                rightStick(0, 0);
                            } else if (message.equals("left")) {

                                Toast.makeText(ChatBoxActivity.this, "left", Toast.LENGTH_SHORT).show();
                                rightStick(-3, 0);
                                Thread.sleep(100);
                                rightStick(0, 0);
                            } else if (message.equals("stopAll")) {

                                Toast.makeText(ChatBoxActivity.this, "stopAll", Toast.LENGTH_SHORT).show();
                                rightStick(0, 0);
                                leftStick(0, 0);
                            } else {

                                Toast.makeText(ChatBoxActivity.this, '_' + message + '_', Toast.LENGTH_SHORT).show();
                                rightStick(0, 0);
                                leftStick(0, 0);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
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

    }

    protected void onResume() {
        super.onResume();
        Toast.makeText(ChatBoxActivity.this, "onResume", Toast.LENGTH_SHORT).show();

    }

    protected void onRestart() {
        super.onRestart();
        Toast.makeText(ChatBoxActivity.this, "onRestart", Toast.LENGTH_SHORT).show();
    }


    protected void onStop() {
        super.onStop();
        Toast.makeText(ChatBoxActivity.this, "onStop", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        socket.disconnect();

        Toast.makeText(ChatBoxActivity.this, "OnDestroy", Toast.LENGTH_SHORT).show();
    }

}
