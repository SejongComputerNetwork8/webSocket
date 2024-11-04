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
    private SSLSocket sslSocket;// 보안 소켓 연결

    private static final int SMTP_PORT = 587; // SMTP 서버 포트
    private static final String SMTP_HOST = "smtp.naver.com";
    private static final int TIMEOUT_MILLISECONDS = 10000; // 10초 타임아웃


    // 네이버 SMTP 서버와 연결, 이메일 전송까지의 과정을 관리
    // 매개변수로 발신 이메일, 비밀번호, 수신 이메일, 메시지 내용을 받음
    public void sendNaverEmail(String email, String password, String recipient, String subject, String message) throws IOException {
        try {
            initializeConnection();// SMTP 서버와 연결 수립
            setupEmailTransfer(email, password, recipient, subject, message);
        } catch (IOException e) {
            throw new IOException("이메일 전송 실패: " + e.getMessage(), e);
        } finally {
            closeConnection();// 연결 종료 보장
        }
    }

    /**
     * SMTP 서버와의 초기 연결을 설정하는 메소드
     */
    private void initializeConnection() throws IOException {
        try {
            // 1단계: 일반 소켓으로 초기 연결
            Socket clientSocket = new Socket(SMTP_HOST, SMTP_PORT);
            clientSocket.setSoTimeout(TIMEOUT_MILLISECONDS);

            // 2단계: TLS 보안 소켓 설정
            sslSocket = createTLSSocket(clientSocket);
            setupInitialStreams(clientSocket);// 초기 스트림 설정

            // 3단계: 서버 응답 확인 및 TLS 업그레이드
            verifyServerResponse();// 서버가 준비되었는지 확인
            upgradeToPulse();// TLS 연결로 업그레이드

            // TLS 스트림 설정
            setupTLSStreams(sslSocket);

        } catch (IOException e) {
            throw new IOException("연결 초기화 실패: " + e.getMessage(), e);
        }
    }
    /**
     * TLS 보안 소켓을 생성하는 메소드
     */
    private SSLSocket createTLSSocket(Socket clientSocket) throws IOException {
        // SSL 소켓 팩토리 생성 (보안 연결을 위한 소켓 생성기)
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        // 일반 소켓을 SSL/TLS 소켓으로 업그레이드
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(
                clientSocket,     // 기존 소켓
                SMTP_HOST,       // 서버 주소
                SMTP_PORT,       // 포트 번호
                true            // autoClose 설정
        );

        // TLS 1.2 이상 사용 강제
        socket.setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});
        return socket;
    }

    /**
     * TLS 연결을 위한 입출력 스트림 설정
     */

    private void setupTLSStreams(SSLSocket sslSocket) throws IOException {
        // 입력 스트림 설정 (서버로부터 데이터 읽기)
        inFromServer = new BufferedReader(
                new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8));
        // 출력 스트림 설정 (서버로 데이터 쓰기)
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

    /**
     * 이메일 전송 전체 프로세스를 관리하는 메소드
     */
    private void setupEmailTransfer(String email, String password, String recipient, String subject, String message) throws IOException {
        // 1단계: SMTP 인증
        authenticateUser(email, password);// 사용자 로그인


        // 2단계: 이메일 전송 준비
        setupEmailTransaction(email, recipient);// 발신자, 수신자 설정

        // 3단계: 이메일 내용 전송
        sendEmailContent(email, recipient, subject, message);// 실제 이메일 데이터 전송
    }
    /**
     * SMTP 인증을 처리하는 메소드
     */

    private void authenticateUser(String email, String password) throws IOException {
        sendCommand("AUTH LOGIN");// 로그인 명령 전송
        verifyResponse("334");// 서버가 로그인 준비됨

        // 이메일과 비밀번호를 Base64로 인코딩하여 전송
        sendCommand(Base64.getEncoder().encodeToString(email.getBytes()));
        verifyResponse("334");// 이메일 확인됨

        sendCommand(Base64.getEncoder().encodeToString(password.getBytes()));
        verifyResponse("235");// 인증 성공
    }

    private void setupEmailTransaction(String sender, String recipient) throws IOException {
        sendCommand("MAIL FROM:<" + sender + ">");// 발신자 설정
        verifyResponse("250");// 발신자 승인됨

        sendCommand("RCPT TO:<" + recipient + ">"); //수신자 설정
        verifyResponse("250"); // 수신자 승인

        sendCommand("DATA"); // 데이터 전송 시작
        verifyResponse("354"); // 데이터 전송 준비됨
    }

    private void sendEmailContent(String sender, String recipient, String subject, String message) throws IOException {
        StringBuilder emailContent = new StringBuilder();

        // 기본 MIME 헤더
        emailContent.append("MIME-Version: 1.0\r\n");
        emailContent.append("Content-Type: text/plain; charset=UTF-8\r\n");
        emailContent.append("Content-Transfer-Encoding: base64\r\n");

        // 제목 설정 (UTF-8 Base64 인코딩)
        emailContent.append("Subject: =?UTF-8?B?")
                .append(Base64.getEncoder().encodeToString(subject.getBytes(StandardCharsets.UTF_8)))
                .append("?=\r\n");

        // 발신자와 수신자
        emailContent.append("From: ").append(sender).append("\r\n");
        emailContent.append("To: ").append(recipient).append("\r\n");
        emailContent.append("\r\n");  // 헤더와 본문 구분을 위한 빈 줄

        // 메시지 본문 UTF-8로 인코딩 후 Base64 변환
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        String encodedBody = Base64.getEncoder().encodeToString(messageBytes);
        emailContent.append(encodedBody).append("\r\n");
        emailContent.append(".\r\n");// 이메일 끝 표시
        // 5. 전송 및 확인
        sendCommand(emailContent.toString());
        verifyResponse("250");// 전송 성공 확인
    }

    /**
     * 서버에 명령을 전송하는 메소드
     * @param command SMTP 명령어
     */

    private void sendCommand(String command) throws IOException {
        outToServer.writeBytes(command + "\r\n"); // 명령어 끝에 CRLF 추가
    }

    /**
     * 서버로부터 응답을 받는 메소드
     * @return 서버 응답 메시지
     */
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
    /**
     * 연결을 안전하게 종료하는 메소드
     */

    private void closeConnection() {
        try {
            // 1. QUIT 명령 전송
            if (outToServer != null) {
                sendCommand("QUIT");// SMTP 연결 종료 요청
                outToServer.close();// 출력 스트림 닫기
            }
            // 2. 입력 스트림 종료
            if (inFromServer != null) {
                inFromServer.close();
            }
            // 3. 소켓 연결 종료
            if (sslSocket != null && !sslSocket.isClosed()) {
                sslSocket.close();
            }
        } catch (IOException e) {
            // 종료 중 오류는 로그만 남기고 진행
            System.err.println("연결 종료 중 오류 발생: " + e.getMessage());
        }
    }

}
