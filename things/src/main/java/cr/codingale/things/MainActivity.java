package cr.codingale.things;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;

public class MainActivity extends Activity implements MqttCallback {

    private static final String TAG = "Things";
    private static final String topic_gestion = "aulate/gestion";
    private static final String topic_led = "aulate/led";
    private static final String topic_boton = "aulate/boton";
    private static final String hello = "Hello world! Android Things conectada.";
    private static final int qos = 1;
    private static final String broker = "tcp://iot.eclipse.org:1883";
    private static final String clientId = "Test134568789";

    private MqttClient client;
    private static final String BTN_PIN = "BCM23";
    private Gpio btnGpio;

    private final String PIN_LED = "BCM18";
    public Gpio mLedGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectMQTT();
        subscribeToLED();
        setupButton();
        setupLED();
    }

    @Override
    protected void onDestroy() {
        disposeButton();
        disposeLED();
        disconnectMQTT();
        super.onDestroy();
    }

    private GpioCallback callback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                Log.i(TAG, "Triggered: Button status - " + gpio.getValue());
                if (gpio.getValue()) {
                    Log.i(TAG, "Triggered: Button pressed ");
                    sendMQTTMessage(topic_boton, "click!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    };

    private void setupButton() {
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            btnGpio = manager.openGpio(BTN_PIN);
            btnGpio.setDirection(Gpio.DIRECTION_IN);
            btnGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            btnGpio.registerGpioCallback(callback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disposeButton() {
        if (btnGpio != null) {
            try {
                btnGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error en el API PeripheralIO", e);
            } finally {
                btnGpio = null;
            }
        }
    }

    private void setupLED() {
        PeripheralManager service = PeripheralManager.getInstance();
        try {
            mLedGpio = service.openGpio(PIN_LED);
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, "Error en el API PeripheralIO", e);
        }
    }

    private void disposeLED() {
        if (mLedGpio != null) {
            try {
                mLedGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error en el API PeripheralIO", e);
            } finally {
                mLedGpio = null;
            }
        }
    }

    private void connectMQTT() {
        try {
            String welcome = "Hello world! Android Things conectada.";
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            client.setCallback(this);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(topic_gestion, "Android Things desconectada!"
                    .getBytes(), qos, false);
            Log.i(TAG, "Conectando al broker " + broker);
            client.connect(connOpts);
            Log.i(TAG, "Conectado");
            Log.i(TAG, "Publicando mensaje de bienvenida.");
            sendMQTTMessage(topic_gestion, welcome);
            Log.i(TAG, "Mensaje de bienvenida publicado");
        } catch (MqttException e) {
            Log.e(TAG, "Error en MQTT.", e);
        }
    }

    private void subscribeToLED() {
        try {
            client.subscribe(topic_led, qos);
            Log.i(TAG, "Suscrito a " + topic_led);
        } catch (MqttException e) {
            Log.e(TAG, "Error en MQTT.", e);
        }
    }

    private void disconnectMQTT() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                Log.i(TAG, "MQTT Desconectado");
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error en MQTT.", e);
        }
    }

    private void sendMQTTMessage(String topic, String message) {
        try {
            Log.i(TAG, "Publicando mensaje: " + message);
            MqttMessage mqqtMessage = new MqttMessage(message.getBytes());
            mqqtMessage.setQos(qos);
            client.publish(topic, mqqtMessage);
            Log.i(TAG, "Mensaje publicado");
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
        String payload = new String(message.getPayload());
        Log.d(TAG, payload);
        switch (payload) {
            case "ON":
                mLedGpio.setValue(true);
                Log.d(TAG, "LED ON!");
                break;
            case "OFF":
                mLedGpio.setValue(false);
                Log.d(TAG, "LED OFF!");
                break;
            case "Shake!":
                Log.d(TAG, "Parpadeo!");
                for (int i = 0; i < 4; i++) {
                    mLedGpio.setValue(true);
                    Thread.sleep(500);
                    mLedGpio.setValue(false);
                    Thread.sleep(500);
                }
                break;
            default:
                Log.d(TAG, "Comando no soportado");
                break;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "Entrega completa!");
    }
}
