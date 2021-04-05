package com.zukalover.amqpaho;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button btn_send;
    EditText edt_message;

    MqttAndroidClient client;
    //MqttClient client;
    String clientId="Tablet-1";
   // String serverURI= "tcp://192.168.1.185:1883";
   String serverURI= "tcp://192.168.8.100:1883";
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        edt_message = findViewById(R.id.edt_amq_message);
        btn_send = findViewById(R.id.btn_amq_send);
        connect();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage(edt_message.getText().toString());
            }
        });
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    private void connect()
    {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setAutomaticReconnect(true);



        client = new MqttAndroidClient(MainActivity.this, serverURI, clientId);
        try {
            client.connect(connectOptions, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                    e.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void subscribe()
    {
        String subscribeTopic = "THEtopic";
        try {
            client.subscribe(subscribeTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, message.toString(), Toast.LENGTH_SHORT).show();
                           // new ProcessMessageAsyncTask().execute(message.toString());
                            try {
                            String fileName = "csv.csv";

                                FileOutputStream fileOutputStream = openFileOutput(fileName,Context.MODE_PRIVATE);
                                fileOutputStream.write( Base64.decode(message.toString(),Base64.DEFAULT));
                                fileOutputStream.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publishMessage(String message)
    {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes());

        try {
            client.publish("outputTopic",mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static class ProcessMessageAsyncTask extends AsyncTask<String,Void,Void>
    {
        public ProcessMessageAsyncTask()
        {

        }
        @Override
        protected Void doInBackground(String... strings) {

            /**
             * WORKS
            byte[] bytes = Base64.decode(strings[0],Base64.DEFAULT);
            FileOutputStream fileOutputStream = null;
            String fileName =  + "/airtravel.csv";
            try {
                fileOutputStream = new FileOutputStream(fileName);
                fileOutputStream.write(bytes);
                fileOutputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
             **/

            try {


                String fileName = "csv.csv";
                FileOutputStream fileOutputStream = new FileOutputStream(new File(fileName, String.valueOf(Context.MODE_PRIVATE)));

            }catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }
}