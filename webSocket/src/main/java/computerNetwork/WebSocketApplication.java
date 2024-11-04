package computerNetwork;


import computerNetwork.webSocket.gmail.GmailFetcher;
import computerNetwork.webSocket.gmail.GmailSender;
import computerNetwork.webSocket.javafx.HomePage;
import computerNetwork.webSocket.ui.UI;

import java.io.IOException;

import static javafx.application.Application.launch;

public class WebSocketApplication {
	public static void main(String[] args) throws IOException {
		launch(HomePage.class, args);
		GmailFetcher gmailFetcher=new GmailFetcher();
		gmailFetcher.fetchMail();
//		GmailSender gmailSender=new GmailSender("mhg10181018@gmail.com","dkgoanypxohpizoi");
//		gmailSender.sendEmail("msw0909@naver.com","!!");

	}
//

}
