package monitoring.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class IoTMQTTConnectionUtils implements MqttCallback {
	
	static MqttClient client;

	public static String connect() {
		String serverHost = "xyzkfq.messaging.internetofthings.ibmcloud.com";
		String serverPort = "8883";
		String clientId = "a:xyzkfq:mobihealthmoni";
		
		String connectionUri = "ssl://" + serverHost + ":" + serverPort;
		try {
			client = new MqttClient(connectionUri, clientId);
		} catch (MqttException e1) {
			e1.printStackTrace();
		}

		MqttConnectOptions options = new MqttConnectOptions();
		options.setConnectionTimeout(1000000);
		options.setCleanSession(true);
		options.setUserName("a-xyzkfq-d5rvpn3y6f"); //a-xyzkfq-bgriq7bd5w
		options.setPassword("Zxj90-f_Yku@LfMpMx".toCharArray()); //CDyeTEcMADlnEBO6vx
		
		try {
			client.connect(options);
			
			//client.subscribe(" iot-2/type/iOSDeviceMT/id/a88e24348a82/evt/status/fmt/json");
			//client.subscribe("iot-2/type/iOSDeviceMT/id/a88e24348a82/mon");
			
			//publish("iot-2/evt/status/fmt/json", "message", false, 0);
			
			//client.subscribe("iot-2/cmd/+/fmt/+", 2);
			return "ok";
		} catch (MqttException e) {
			e.printStackTrace();
			return "not ok";
		}
	}

//	public static String subscribe(String topic, int qos) {
//		try {
//			client.subscribe(topic, qos);
//			return "merge!";
//		} catch (MqttException e) {
//			e.printStackTrace();
//		}
//		return "nu merge!";
//	}
//
//	public static void publish(String topic, String message, boolean retained,
//			int qos) {
//		MqttMessage mqttMsg = new MqttMessage(message.getBytes());
//		mqttMsg.setRetained(retained);
//		mqttMsg.setQos(qos);
//		try {
//			client.publish(topic, mqttMsg);
//			
//		} catch (MqttPersistenceException e) {
//		} catch (MqttException e) {
//		}
//	}
//	
	public void messageArrived(String topic, MqttMessage message) {
		
		System.out.println("Primeste");
	}

	@Override
	public void connectionLost(Throwable arg0) {
		System.out.println("mort");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		System.out.println("trimite");
	}
}