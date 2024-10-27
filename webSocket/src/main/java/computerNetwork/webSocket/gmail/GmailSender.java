package computerNetwork.webSocket.gmail;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class GmailSender {
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private String gmailId;
    private String gmailAppPassword;
    private String receivedEmail;
    private String message;
    public GmailSender(String id,String password){
        this.gmailId=id;
        this.gmailAppPassword=password;
    }

    public void sendEmail(String receivedEmail,String message) throws IOException {
        setReceivendEmailAndMessage(receivedEmail, message);
        // 일반 소켓을 사용해 Gmail SMTP 서버에 연결 (포트 587)
        Socket clientSocket = new Socket("smtp.gmail.com", 587);
        SSLSocket sslSocket = getTLSSocket(clientSocket);
        getInitialResponse(clientSocket);
        isConnectionWithServer();
        // STARTTLS 명령어 전송 (암호화된 통신 시작)
        startTLSConnection();
        // 서버와 데이터를 주고받기 위한 새로운 TLS 스트림 설정
        setTLSStream(sslSocket);
        //서버와 연결 상태 확인
        isConnectionWithServer();
        loginGmail();
        //email을 하기위한 준비
        setReadyToEmail();
        //이메일 내용 전송
        sendEmailData();
        quitWebSocket(sslSocket);
    }


    private void setReceivendEmailAndMessage(String receivedEmail, String message) {
        this.receivedEmail= receivedEmail;
        this.message= message;
    }


    private SSLSocket getTLSSocket(Socket clientSocket) throws IOException {
        // TLS로 연결 업그레이드
        // TLS를 써야 gmail과의 연동이 가능함
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();
        return (SSLSocket) sslSocketFactory.createSocket(clientSocket, "smtp.gmail.com", 587, true);
    }

    private void getInitialResponse(Socket clientSocket) throws IOException {
        // 서버와 데이터를 주고받기 위한 입출력 스트림 설정
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        // 서버 초기 응답 읽기
        String response = inFromServer.readLine();
        System.out.println("Received: " + response);
    }
    private void isConnectionWithServer() throws IOException {
        String response;
        // SMTP 명령어 재전송
        outToServer.writeBytes("HELO example.com\r\n");
        response = inFromServer.readLine();
        System.out.println("Sent: HELO example.com");
        System.out.println("Received: " + response);
    }
    private void startTLSConnection() throws IOException {
        outToServer.writeBytes("STARTTLS\r\n");
        String response = inFromServer.readLine();
        System.out.println("Sent: STARTTLS");
        System.out.println("Received: " + response);
    }
    private void setTLSStream(SSLSocket sslSocket) throws IOException {
        inFromServer = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
        outToServer = new DataOutputStream(sslSocket.getOutputStream());
    }
    private void loginGmail() throws IOException {
        String response;
        // AUTH LOGIN 명령어 전송
        outToServer.writeBytes("AUTH LOGIN\r\n");
        response = inFromServer.readLine();
        System.out.println("Sent: AUTH LOGIN");
        System.out.println("Received: " + response);

        // Base64로 인코딩된 사용자 이름 전송
        String base64EncodedUsername = java.util.Base64.getEncoder().encodeToString(gmailId.getBytes());
        outToServer.writeBytes(base64EncodedUsername + "\r\n");
        response = inFromServer.readLine();
        System.out.println("Sent: " + base64EncodedUsername);
        System.out.println("Received: " + response);

        // Gmail 서버에서 334 응답으로 비밀번호를 요청할 때 Base64로 인코딩된 비밀번호 전송
        String base64EncodedPassword = java.util.Base64.getEncoder().encodeToString(gmailAppPassword.getBytes());
        outToServer.writeBytes(base64EncodedPassword + "\r\n");
        response = inFromServer.readLine();
        System.out.println("Sent: " + base64EncodedPassword);
        System.out.println("Received: " + response);
    }
    private void setReadyToEmail() throws IOException {
        String response;
        // 이메일 전송 준비
        //MAIL FROM을 통해서 어디서 보내는 지를 나타냄
        outToServer.writeBytes("MAIL FROM:<"+gmailId+">\r\n");
        response = inFromServer.readLine();
        System.out.println("Sent: MAIL FROM:<mhg10181018@gmail.com>");
        System.out.println("Received: " + response);
        //RCPT TO를 통해서 어디서 보내는 지를 나타냄
        outToServer.writeBytes("RCPT TO:<"+receivedEmail+">\r\n");
        response = inFromServer.readLine();
        System.out.println("Sent: RCPT TO:<"+receivedEmail+">");
        System.out.println("Received: " + response);
        //이제 data가 간다는 것을 알리는 부분
        outToServer.writeBytes("DATA\r\n");
        response = inFromServer.readLine();
        System.out.println("Sent: DATA");
        System.out.println("Received: " + response);
    }

    private void sendEmailData() throws IOException {
        String response;
        outToServer.writeBytes("Subject: Test Email\r\n");
        outToServer.writeBytes("To: "+receivedEmail+"\r\n");
        outToServer.writeBytes("From: "+gmailId+"\r\n");
        outToServer.writeBytes("\r\n");  // 헤더와 본문을 구분하는 빈 줄
        outToServer.writeBytes(message+".\r\n.\r\n");
        response = inFromServer.readLine();
        System.out.println("Sent: Email Body");
        System.out.println("Received: " + response);
    }
    private void quitWebSocket(SSLSocket sslSocket) throws IOException {
        String response;
        // QUIT 명령어 전송 및 연결 종료
        outToServer.writeBytes("QUIT\r\n");
        response = inFromServer.readLine();
        System.out.println("Sent: QUIT");
        System.out.println("Received: " + response);
        // 소켓 닫기
        sslSocket.close();
    }
}
