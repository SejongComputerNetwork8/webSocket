package computerNetwork.webSocket.gmail;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO GMAIL 발신자 이름이 Unkonwn Sender로 나오는 문제 해결
//TODO GMAIL Body중 특정 body가 No Body Content로 나오는 문제 해결
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

        // INBOX 상태 확인 및 메일 확인
        if (checkInboxMessages()) {
            // INBOX 선택
            sendCommand("A003 SELECT INBOX");

            // 최신 10개의 메일에 대해 발신자와 본문 가져오기
            for (int i = 1; i <= 10; i++) {
                fetchMailDetails(i);
            }
        } else {
            System.out.println("INBOX에 메일이 없습니다.");
        }

        // 로그아웃
        sendCommand("A004 LOGOUT");

        // 소켓 닫기
        sslSocket.close();
    }

    private void getInitialResponse() throws IOException {
        String response = inFromServer.readLine();
        System.out.println("Received: " + response);
    }

    private void loginToIMAP(String username, String appPassword) throws IOException {
        // LOGIN 명령어를 통해 Gmail 서버에 로그인 요청
        sendCommand("A001 LOGIN " + username + " " + appPassword);
    }

    private boolean checkInboxMessages() throws IOException {
        // INBOX에 메시지가 있는지 확인
        String statusResponse = sendCommand("A002 STATUS INBOX (MESSAGES)");
        System.out.println("INBOX Status Check:\n" + statusResponse);
        return statusResponse.contains("MESSAGES") && !statusResponse.contains("MESSAGES 0");
    }

    private void fetchMailDetails(int mailId) throws IOException {
        // 발신자와 본문 가져오기
        String fetchResponse = sendCommand("A005 FETCH " + mailId + " (BODY[HEADER.FIELDS (FROM)] BODY[TEXT])", true);

        // 발신자 정보 추출
        String from = extractFrom(fetchResponse);
        // 본문 추출 및 디코딩
        String plainText = decodeBase64(extractBase64Content(fetchResponse));

        System.out.println("\nEmail " + mailId + ":");
        System.out.println("From: " + (decodeBase64(extractBase64Content(from)) != null ? decodeBase64(extractBase64Content(from)) : "Unknown Sender"));
        System.out.println("Body:\n" + (plainText != null ? plainText : "No Body Content"));
    }

    private String extractFrom(String response) {
        // 발신자 이메일 추출을 위한 정규 표현식
        Pattern pattern = Pattern.compile("From: (.+)");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String sendCommand(String command) throws IOException {
        return sendCommand(command, false);
    }

    private String sendCommand(String command, boolean fetchAll) throws IOException {
        System.out.println("Sent: " + command);
        outToServer.writeBytes(command + "\r\n");

        // 서버 응답 읽기
        StringBuilder fullResponse = new StringBuilder();
        String response;
        while ((response = inFromServer.readLine()) != null) {
            if(!fetchAll)
                System.out.println("Received: " + response);
            fullResponse.append(response).append("\n");

            // FETCH 명령이 아닌 경우 OK, NO, BAD에 따라 중단
            if (!fetchAll && (response.contains("OK") || response.contains("NO") || response.contains("BAD"))) {
                break;
            }
            if (fetchAll && response.equals(")")) {
                break;
            }
        }
        return fullResponse.toString();
    }

    private static String extractBase64Content(String input) {
        // "Content-Transfer-Encoding: base64" 뒤에 있는 Base64 데이터 추출
        Pattern pattern = Pattern.compile("Content-Transfer-Encoding: base64\\s+([A-Za-z0-9+/=\\s]+)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // 불필요한 공백과 줄바꿈을 제거하여 반환
            return matcher.group(1).replaceAll("\\s+", "");
        }
        return null;
    }

    private static String decodeBase64(String base64Text) {
        // Base64 디코딩 수행
        if (base64Text == null) return null;
        byte[] decodedBytes = Base64.getDecoder().decode(base64Text);
        return new String(decodedBytes);
    }
}
