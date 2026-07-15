package mx.utng.lmrr.wear.mqtt

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mx.utng.lmrr.smarthealthmonitor.mqtt.MqttConfig
import mx.utng.lmrr.smarthealthmonitor.mqtt.FcMessage
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttWearPublisher(private val context: Context) {

    private var client: MqttAsyncClient? = null

    fun connect() {
        // Usamos un ID único agregando el timestamp para evitar conflictos de "Identifier rejected"
        val clientId = "${MqttConfig.CLIENT_WEAR}-${System.currentTimeMillis()}"
        
        client = MqttAsyncClient(
            MqttConfig.BROKER_URL,
            clientId,
            MemoryPersistence()
        )

        val options = MqttConnectOptions().apply {
            userName          = MqttConfig.USERNAME
            password          = MqttConfig.PASSWORD.toCharArray()
            isCleanSession    = true
            connectionTimeout = 30
            keepAliveInterval = 60
            
            // Si la URL empieza con ssl://, configuramos el socket factory
            if (MqttConfig.BROKER_URL.startsWith("ssl://")) {
                socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
            }
        }

        client?.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                android.util.Log.d("MQTT_WEAR", "✅ Conectado a HiveMQ Cloud con ID: $clientId")
            }
            override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                val cause = (ex as? MqttException)?.cause
                android.util.Log.e("MQTT_WEAR", "❌ Error de conexión: ${ex?.message} (Causa: ${cause?.message})")
                ex?.printStackTrace()
            }
        })
    }

    /** Publicar FC al topic MQTT */
    fun publishFC(bpm: Int, estado: String) {
        if (client?.isConnected != true) return

        val message = FcMessage(bpm = bpm, estado = estado)
        val payload = Json.encodeToString(message).toByteArray()

        val mqttMessage = MqttMessage(payload).apply {
            qos        = MqttConfig.QOS
            isRetained = true // el TV verá el último valor al conectarse
        }

        client?.publish(MqttConfig.TOPIC_FC, mqttMessage)
        android.util.Log.d("MQTT_WEAR", "📤 Publicado: ${bpm} bpm -> ${MqttConfig.TOPIC_FC}")
    }
}
