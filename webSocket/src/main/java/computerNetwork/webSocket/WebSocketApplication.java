package computerNetwork.webSocket;





//import computerNetwork.webSocket.gmail.GmailFetcher;

import computerNetwork.webSocket.gmail.GmailMailbox;

import java.io.IOException;

public class WebSocketApplication {
	public static void main(String[] args) throws IOException {
	GmailMailbox gmailFetcher=new GmailMailbox();
	gmailFetcher.checkEmails("mhg10181018@gmail.com","dkgoanypxohpizoi");
	}


}
