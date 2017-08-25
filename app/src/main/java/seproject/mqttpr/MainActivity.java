package seproject.mqttpr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    private static final String LOGTAG = "HANSOOOOOOOOOOOOL MQTT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editContents = (EditText) findViewById(R.id.editText_contents);
        final EditText editTopic = (EditText) findViewById(R.id.editText_topic);
        Button pubButton = (Button) findViewById(R.id.button_Pub);
        Button subButton = (Button) findViewById(R.id.button_Sub);
        Button unsubButton = (Button) findViewById(R.id.button_Unsub);
        Button connectBuctton = (Button) findViewById(R.id.button_Connect);
        Button disconnectButton = (Button) findViewById(R.id.button_Disconnect);
        final TextView textview = (TextView) findViewById(R.id.textview_Test);
        final String Testtopic = "/sol";
        final String Testpayload = "Message from SolApp";
        //"UTF-8" encoding may be needed
        /******************This IP of device running mosquitto server) if for testing ********************/
        //String broker = "tcp://192.168.0.119:1883";
        String broker="tcp://basserd2.iptime.org:3335";
        /******************This clinetID is afor testing***************/
        String clientId = "SolApp";
            MemoryPersistence persistence = new MemoryPersistence();
            //Persistence that uses memory In cases where reliability is not required across client or device restarts memory this memory peristence can be used.
            // In cases where reliability is required like when clean session is set to false then a non-volatile form of persistence should be used.
            /******************************************************Setting Call back to MqttAndroidClient*****************************************************************************************************/

            //setCallback methos sets a callback listener to use for events that happen asyncrhnously.
            //event is connection lost & messageArrived & deliveryComplete
            final MqttAndroidClient mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(), broker, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.i(LOGTAG, "Connection Lost");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.i(LOGTAG, "Message Arrived!: " + topic + ": " + new String(message.getPayload()));
                    Toast.makeText(getApplicationContext(), "Message Arrived!: " + topic + ": " + new String(message.getPayload()), Toast.LENGTH_LONG).show();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i(LOGTAG, "DeliveryComplete" + token.getTopics());
                }
        });
        final MqttConnectOptions connOpts = new MqttConnectOptions();
        //MqttConnectOptions holds the set of options that control how the clinet connects to a server.
        connOpts.setCleanSession(true);
        //Sets whheter the client and server should remember state acrros restrats and reconnects
        //true --> not maintain state across restarts of the client, the server or the connections.(not gurantee certain lv of QoS)

        //String clientId= MqttClient.generateClientId();//genearate Random ID
        /*******************************************************Button Setting********************************************************************************************/
        connectBuctton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                textview.setText("Connecting");
                try {
                    //IMqttActionListenr will be notified when an asynchronous action completes.
                    // connect() calls an asynchronous method and you need to implement an ActionListener. In case of success you could send messages
                    IMqttToken token = mqttAndroidClient.connect(connOpts, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.i(LOGTAG, "Client connected");
                            Log.i(LOGTAG, "Topics=" + asyncActionToken.getTopics());
                            textview.setText("Connected");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.i(LOGTAG, "Client connection failed: " + exception.getMessage());
                            textview.setText("Connection Failed ");
                        }
                    });
                } catch (MqttException e) {
                    // TODO Auto-generated catch block
                    Log.i(LOGTAG, "Mqtt Exception occur");
                }

            }

        });
        /**********************************************************publish*************************************************************/
        pubButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                textview.setText(editContents.getText());
                String payload = editContents.getText().toString();
                String topic = editTopic.getText().toString();
                try {
                    int qos = 1;
                    byte[] encodePayload = new byte[0];
                    encodePayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodePayload);
                    message.setQos(2);
                    message.setRetained(false);
                    mqttAndroidClient.publish(topic, message, qos, null);
                    Log.i(LOGTAG, "Message published");
                    textview.setText("Message published");


                } catch (UnsupportedEncodingException e) {
                    Log.i(LOGTAG, "UnsupportedEncodingException Exception occur");
                } catch (MqttPersistenceException e) {
                    // TODO Auto-generated catch block

                    Log.i(LOGTAG, "MqttPer Exception occur");

                } catch (MqttException e) {
                    // TODO Auto-generated catch block
                    Log.i(LOGTAG, "Mqtt Exception occur");
                }
            }
        });
        /**********************************************************subscribe*************************************************************/
        subButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String subtopic = editTopic.getText().toString();

                try {
                    int qos = 1;
                    IMqttToken subToken = mqttAndroidClient.subscribe(subtopic, qos, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            // The message was published
                            Log.i(LOGTAG, "Subscription " + subtopic + " successes");
                            textview.setText("Subscription " + subtopic + " successes");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken,
                                              Throwable exception) {
                            // The subscription could not be performed, maybe the user was not
                            // authorized to subscribe on the specified topic e.g. using wildcards
                            Log.i(LOGTAG, "Subscription failed");
                            textview.setText("Subscription failed");
                        }
                    });

                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
        /**********************************************************Unsubscribe*************************************************************/
        unsubButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                textview.setText("Implmentation is needed");
                final String subtopic = editTopic.getText().toString();
                try {
                    int qos = 1;
                    IMqttToken unsubToken = mqttAndroidClient.unsubscribe(subtopic, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            // The message was published
                            Log.i(LOGTAG, "Unsubscription " + subtopic + " successes");
                            textview.setText("Unsubscription " + subtopic + " successes");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken,
                                              Throwable exception) {
                            // The subscription could not be performed, maybe the user was not
                            // authorized to subscribe on the specified topic e.g. using wildcards
                            Log.i(LOGTAG, "Unsubscription failed");
                            textview.setText("Unsubscription failed");
                        }
                    });

                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
        disconnectButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i(LOGTAG, "disconnect");
                try {
                    IMqttToken disconToken = mqttAndroidClient.disconnect();
                    disconToken.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            // we are now successfully disconnected
                            Log.i(LOGTAG, "Disconnect success");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken,
                                              Throwable exception) {
                            Log.i(LOGTAG, "Disconnect failed");
                            // something went wrong, but probably we are disconnected anyway
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
