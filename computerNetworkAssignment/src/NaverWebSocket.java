import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class NaverWebSocket implements AutoCloseable {
    // inFromServer : 네이버 SMTP 서버에서 보내온 응답 메시지를 읽기 위한 입력 스트림
    // outToServer : 네이버 SMTP 서버로 데이터를 전송하기 위한 출력 스트림
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private SSLSocket sslSocket;

    private static final int SMTP_PORT = 587;
    private static final String SMTP_HOST = "smtp.naver.com";
    private static final int TIMEOUT_MILLISECONDS = 10000; // 10초 타임아웃


    // 네이버 SMTP 서버와 연결, 이메일 전송까지의 과정을 관리
    // 매개변수로 발신 이메일, 비밀번호, 수신 이메일, 메시지 내용을 받음
    public void sendNaverEmail(String email, String password, String recipient, String subject, String message) throws IOException {
        try {
            initializeConnection();
            setupEmailTransfer(email, password, recipient, subject, message);
        } catch (IOException e) {
            throw new IOException("이메일 전송 실패: " + e.getMessage(), e);
        } finally {
            closeConnection();
        }
    }

    private void initializeConnection() throws IOException {
        try {
            // 소켓 생성 및 타임아웃 설정
            Socket clientSocket = new Socket(SMTP_HOST, SMTP_PORT);
            clientSocket.setSoTimeout(TIMEOUT_MILLISECONDS);

            // TLS 소켓 설정
            sslSocket = createTLSSocket(clientSocket);
            setupInitialStreams(clientSocket);

            // 서버 응답 확인 및 TLS 업그레이드
            verifyServerResponse();
            upgradeToPulse();

            // TLS 스트림 설정
            setupTLSStreams(sslSocket);

        } catch (IOException e) {
            throw new IOException("연결 초기화 실패: " + e.getMessage(), e);
        }
    }

    private SSLSocket createTLSSocket(Socket clientSocket) throws IOException {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(
                clientSocket, SMTP_HOST, SMTP_PORT, true);

        // TLS 1.2 이상 사용 강제
        socket.setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});
        return socket;
    }

    private void setupTLSStreams(SSLSocket sslSocket) throws IOException {
        inFromServer = new BufferedReader(
                new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8));
        outToServer = new DataOutputStream(sslSocket.getOutputStream());
    }

    private void setupInitialStreams(Socket clientSocket) throws IOException {
        inFromServer = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
    }

    private void verifyServerResponse() throws IOException {
        String response = readResponse();
        if (!response.startsWith("220")) {
            throw new IOException("서버 준비 상태가 아님: " + response);
        }
    }

    private void upgradeToPulse() throws IOException {
        sendCommand("STARTTLS");
        String response = readResponse();
        if (!response.startsWith("220")) {
            throw new IOException("TLS 업그레이드 실패: " + response);
        }
    }

    private void setupEmailTransfer(String email, String password, String recipient, String subject, String message) throws IOException {
        // 로그인
        authenticateUser(email, password);

        // 이메일 전송 준비
        setupEmailTransaction(email, recipient);

        // 이메일 데이터 전송
        sendEmailContent(email, recipient, subject, message);
    }

    private void authenticateUser(String email, String password) throws IOException {
        sendCommand("AUTH LOGIN");
        verifyResponse("334");

        sendCommand(Base64.getEncoder().encodeToString(email.getBytes()));
        verifyResponse("334");

        sendCommand(Base64.getEncoder().encodeToString(password.getBytes()));
        verifyResponse("235");
    }

    private void setupEmailTransaction(String sender, String recipient) throws IOException {
        sendCommand("MAIL FROM:<" + sender + ">");
        verifyResponse("250");

        sendCommand("RCPT TO:<" + recipient + ">");
        verifyResponse("250");

        sendCommand("DATA");
        verifyResponse("354");
    }

    private void sendEmailContent(String sender, String recipient, String subject, String message) throws IOException {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("MIME-Version: 1.0\r\n");
        emailContent.append("Content-Type: text/plain; charset=UTF-8\r\n");
        emailContent.append("Content-Transfer-Encoding: base64\r\n");
        emailContent.append("Subject: =?UTF-8?B?")
                .append(Base64.getEncoder().encodeToString(subject.getBytes(StandardCharsets.UTF_8)))
                .append("?=\r\n");
        emailContent.append("From: ").append(sender).append("\r\n");
        emailContent.append("To: ").append(recipient).append("\r\n");
        emailContent.append("\r\n");

        // 메시지 본문을 Base64로 인코딩
        String encodedBody = Base64.getEncoder().encodeToString(
                message.getBytes(StandardCharsets.UTF_8));
        emailContent.append(encodedBody).append("\r\n");
        emailContent.append(".\r\n");

        sendCommand(emailContent.toString());
        verifyResponse("250");
    }

    private void sendCommand(String command) throws IOException {
        outToServer.writeBytes(command + "\r\n");
    }

    private String readResponse() throws IOException {
        String response = inFromServer.readLine();
        if (response == null) {
            throw new IOException("서버로부터 응답이 없습니다.");
        }
        return response;
    }

    private void verifyResponse(String expectedCode) throws IOException {
        String response = readResponse();
        if (!response.startsWith(expectedCode)) {
            throw new IOException("예상치 못한 서버 응답: " + response);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (outToServer != null && sslSocket != null && !sslSocket.isClosed()) {
                outToServer.writeBytes("QUIT\r\n");
                outToServer.close();
            }
            if (inFromServer != null) {
                inFromServer.close();
            }
            if (sslSocket != null && !sslSocket.isClosed()) {
                sslSocket.close();
            }
        } catch (IOException e) {
            // 연결 종료 오류는 로그만 남기고 무시
            System.err.println("연결 종료 중 오류 발생: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            if (outToServer != null) {
                sendCommand("QUIT");
                outToServer.close();
            }
            if (inFromServer != null) {
                inFromServer.close();
            }
            if (sslSocket != null && !sslSocket.isClosed()) {
                sslSocket.close();
            }
        } catch (IOException e) {
            System.err.println("연결 종료 중 오류 발생: " + e.getMessage());
        }
    }

}
