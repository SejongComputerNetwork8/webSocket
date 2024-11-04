package computerNetwork.webSocket;






import computerNetwork.webSocket.gmail.GmailFetcher;

import java.io.IOException;

public class WebSocketApplication {
	public static void main(String[] args) throws IOException {
	GmailFetcher gmailFetcher=new GmailFetcher();
	gmailFetcher.checkEmails("mhg10181018@gmail.com","dkgoanypxohpizoi");
	}


}
