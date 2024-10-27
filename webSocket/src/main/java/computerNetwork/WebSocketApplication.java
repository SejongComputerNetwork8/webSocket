package computerNetwork;


import computerNetwork.webSocket.gmail.GmailFetcher;
import computerNetwork.webSocket.gmail.GmailSender;

import java.io.IOException;

public class WebSocketApplication {
	public static GmailSender gmailSender;
	public static GmailFetcher gmailFetcher;

	public static void main(String[] args) throws IOException {
		gmailSender.sendEmail();

	}


}
