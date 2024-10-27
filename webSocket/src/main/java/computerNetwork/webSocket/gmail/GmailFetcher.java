
package computerNetwork.webSocket.gmail;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;

public class GmailFetcher {
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;

    public void fetchMail() throws IOException {
        // Gmail IMAP 서버에 SSL 소켓으로 연결 (포트 993)
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket("imap.gmail.com", 993);

        // 서버와 데이터를 주고받기 위한 입출력 스트림 설정
        inFromServer = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
        outToServer = new DataOutputStream(sslSocket.getOutputStream());

        // 초기 응답 확인
        getInitialResponse();

        // IMAP 로그인
        loginToIMAP("mhg10181018@gmail.com", "dkgoanypxohpizoi");

        // INBOX 선택 (IMAP 명령어)
        sendCommand("A002 SELECT INBOX");

        // 메일 목록 가져오기
        sendCommand("A003 FETCH 1:* (BODY[HEADER.FIELDS (SUBJECT FROM)])");

        // 연결 종료
        sendCommand("A004 LOGOUT");

        // 소켓 닫기
        sslSocket.close();
    }

    private void getInitialResponse() throws IOException {
        String response = inFromServer.readLine();
        System.out.println("Received: " + response);
    }

    private void loginToIMAP(String username, String password) throws IOException {
        // Base64 인코딩된 사용자명과 비밀번호 전송
        String base64Username = Base64.getEncoder().encodeToString(username.getBytes());
        String base64Password = Base64.getEncoder().encodeToString(password.getBytes());

        sendCommand("A001 LOGIN " + base64Username + " " + base64Password);
    }

    private void sendCommand(String command) throws IOException {
        System.out.println("Sent: " + command);
        outToServer.writeBytes(command + "\r\n");

        // 서버 응답 읽기
        String response;
        while ((response = inFromServer.readLine()) != null) {
            System.out.println("Received: " + response);
            if (response.contains("OK") || response.contains("NO") || response.contains("BAD")) {
                break;
            }
        }
    }


}

