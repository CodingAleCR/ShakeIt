package cr.codingale.shakeit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MainActivity extends AppCompatActivity implements
        MqttCallback, ShakeListener.OnShakeListener {
    private static final String TAG = "Mobile";
    private static final String topic_gestion = "aulate/gestion";
    private static final String topic_led = "aulate/led";
    static final String hello = "Hello world! Android Mobile conectado.";
    private static final int qos = 1;
    private static final String broker = "tcp://iot.eclipse.org:1883";
    MqttClient client;
    MqttConnectOptions connOpts;
    Button botonConnect, botonDisconnect, botonON, botonOFF;
    TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShakeListener test = new ShakeListener(this);
        test.setOnShakeListener(this);
        textview = (TextView) findViewById(R.id.textview);
        botonConnect = (Button) findViewById(R.id.buttonConnect);
        botonDisconnect = (Button) findViewById(R.id.buttonDisconnect);
        botonON = (Button) findViewById(R.id.buttonON);
        botonOFF = (Button) findViewById(R.id.buttonOFF);
        try {
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            client.setCallback(this);
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(topic_gestion, ("Android Mobile " +
                    "desconectado!").getBytes(), qos, false);
        } catch (MqttException e) {
            Log.e(TAG, "Error en MQTT.", e);
        }
        botonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Boton presionado");
                textview.setText("Conectando...");
                try {
                    Log.i(TAG, "Conectando al broker " + broker);
                    client.connect(connOpts);
                    Log.i(TAG, "Conectado");
                    Log.i(TAG, "Publicando mensaje: " + hello);
                    MqttMessage message = new MqttMessage(hello.getBytes());
                    message.setQos(qos);
                    client.publish(topic_gestion, message);
                    Log.i(TAG, "Mensaje publicado");
                    textview.setText("Conectado");
                } catch (MqttException e) {
                    Log.e(TAG, "Error en MQTT.", e);
                    textview.setText("Error al conectar");
                }
            }
        });
        botonDisconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Boton presionado");
                textview.setText("Desconectando...");
                try {
                    if (client != null && client.isConnected()) {
                        client.disconnect();
                    }
                    textview.setText("Desconectado");
                } catch (MqttException e) {
                    Log.e(TAG, "Error en MQTT.", e);
                    textview.setText("Error al desconectar");
                }
            }
        });
        botonON.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Boton presionado");
                try {
                    String mensaje = "ON";
                    Log.i(TAG, "Publicando mensaje: " + mensaje);
                    MqttMessage message = new MqttMessage(mensaje.getBytes());
                    message.setQos(qos);
                    client.publish(topic_led, message);
                    Log.i(TAG, "Mensaje publicado");
                    textview.setText("Publicado ON");
                } catch (MqttException e) {
                    Log.e(TAG, "Error en MQTT.", e);
                    textview.setText("Error al publicar");
                }
            }
        });
        botonOFF.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Boton presionado");
                try {
                    String mensaje = "OFF";
                    Log.i(TAG, "Publicando mensaje: " + mensaje);
                    MqttMessage message = new MqttMessage(mensaje.getBytes());
                    message.setQos(qos);
                    client.publish(topic_led, message);
                    Log.i(TAG, "Mensaje publicado");
                    textview.setText("Publicado OFF");
                } catch (MqttException e) {
                    Log.e(TAG, "Error en MQTT.", e);
                    textview.setText("Error al publicar");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error en MQTT.", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "Conexión perdida...");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
            throws Exception {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "Entrega completa!");
    }

    @Override
    public void onShake() {
        Log.i(TAG, "Shake!");
        try {
            String mensaje = "Shake!";
            Log.i(TAG, "Publicando mensaje: " + mensaje);
            MqttMessage message = new MqttMessage(mensaje.getBytes());
            message.setQos(qos);
            client.publish(topic_led, message);
            Log.i(TAG, "Mensaje publicado");
            textview.setText("Publicado Shake!");
        } catch (MqttException e) {
            Log.e(TAG, "Error en MQTT.", e);
            textview.setText("Error al publicar");
        }
    }
}
