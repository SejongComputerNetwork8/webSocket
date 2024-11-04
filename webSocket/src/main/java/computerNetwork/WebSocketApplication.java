package computerNetwork;
import javafx.application.Application;
import computerNetwork.webSocket.javafx.HomePage;

import computerNetwork.webSocket.gmail.GmailFetcher;
import computerNetwork.webSocket.gmail.GmailSender;
import computerNetwork.webSocket.javafx.HomePage;
import computerNetwork.webSocket.naver.NaverFetcher;
import computerNetwork.webSocket.ui.UI;

import java.io.IOException;

public class WebSocketApplication {
	public static void main(String[] args) throws IOException {
	    Application.launch(HomePage.class, args);


	}


}
