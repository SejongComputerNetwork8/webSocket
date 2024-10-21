package computerNetwork.webSocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;



//@SpringBootApplication
public class WebSocketApplication {
	public static WebSocket webSocket;

	public static void main(String[] args) throws IOException {

		webSocket=new WebSocket();
		webSocket.sendEmail();
	}


}
