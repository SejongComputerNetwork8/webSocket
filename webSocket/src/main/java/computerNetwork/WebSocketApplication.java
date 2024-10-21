package computerNetwork;


import computerNetwork.webSocket.WebSocket;

import java.io.IOException;

public class WebSocketApplication {
	public static WebSocket webSocket;

	public static void main(String[] args) throws IOException {
		webSocket=new WebSocket();
		webSocket.sendEmail();
	}


}
