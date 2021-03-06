package in.wglabz.alberto.main.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Date;

public class MqttHelper {
    public static MqttAndroidClient mqttAndroidClient;
    final String clientId = "alberto_smart_android_app"+new Date();
    String username = "";
    String password = "";

    public MqttHelper(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        username=prefs.getString("mqtt_username","no_user");
        password=prefs.getString("mqtt_password","no_pasword");

        mqttAndroidClient = new MqttAndroidClient(context,prefs.getString("protocol","tcp")+"://"+prefs.getString("server_address","")+":"+prefs.getString("port","1883"), clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " +  exception.toString());
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }
    public static void publishMessage(String topic, final String payload){
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(0);
        message.setRetained(false);
        try {
            if(mqttAndroidClient.isConnected())
                    mqttAndroidClient.publish(topic, message);
            else {
                Log.w("ALBERTO", "Unable to publish");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(!mqttAndroidClient.isConnected()){
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        MqttMessage message = new MqttMessage(payload.getBytes());
                        message.setQos(0);
                        message.setRetained(false);
                        try {
                            mqttAndroidClient.publish("bikash/message", message);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void subscribeToTopic() {
//        try {
//            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.w("Mqtt","Subscribed!");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.w("Mqtt", "Subscribed fail!");
//                }
//            });
//
//        } catch (MqttException ex) {
//            System.err.println("Exceptionst subscribing");
//            ex.printStackTrace();
//        }
    }
}