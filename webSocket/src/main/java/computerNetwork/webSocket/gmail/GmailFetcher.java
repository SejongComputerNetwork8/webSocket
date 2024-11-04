package computerNetwork.webSocket.gmail;

import computerNetwork.webSocket.dto.FetchingInformation;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class GmailFetcher implements AutoCloseable { // try-with-resources 문을 사용하여 자원을 자동으로 해제하기 위해 AutoCloseable 인터페이스 구현
    private static final String IMAP_HOST = "imap.gmail.com"; // Gmail IMAP 서버 호스트명
    private static final int IMAP_PORT = 993; // IMAP 프로토콜 포트 (SSL/TLS가 적용된 993 포트)
    private static final int TIMEOUT_MILLISECONDS = 10000; // 타임아웃 10초로 설정

    private String from=null;
    private String date=null;
    private String subject=null;
    public static List<FetchingInformation> gmailFetchingInformations;
    private BufferedReader inFromServer; // 서버로부터 데이터를 읽기 위한 스트림 (문자 입력 스트림)
    private DataOutputStream outToServer; // 서버에 데이터를 전송하기 위한 스트림 (바이트 출력 스트림)
    private SSLSocket sslSocket; // SSL을 사용하는 소켓을 설정하여 보안 연결을 수행

    public List<FetchingInformation> fetch(String email, String password) throws IOException {
        // 이메일 확인을 위한 메서드, 전체 과정을 try-finally로 감싸 자원 해제 보장
        try {
            initializeConnection(); // 서버 연결 초기화
            login(email, password); // Gmail 계정에 로그인
            readInbox(); // 받은 편지함 읽기
        } finally {
            close(); // 연결 종료
        }
        return gmailFetchingInformations;
    }

    private void initializeConnection() throws IOException {
        // 서버와 SSL 연결을 설정
        try {
            // 기본 SSL 소켓 팩토리를 사용해 SSL 소켓을 생성
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslSocket = (SSLSocket) factory.createSocket(IMAP_HOST, IMAP_PORT); // IMAP 서버와 연결
            sslSocket.setSoTimeout(TIMEOUT_MILLISECONDS);  // 타임아웃 설정

            // 입력 스트림과 출력 스트림을 설정
            inFromServer = new BufferedReader(
                    new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8)); // UTF-8로 변환하여 읽기
            outToServer = new DataOutputStream(sslSocket.getOutputStream()); // 바이트 단위 출력 스트림 설정

            // 초기 연결 응답 확인
            String response = readResponse();
            if (!response.contains("OK")) { // 응답에 "OK"가 없으면 예외 발생
                throw new IOException("IMAP 서버 연결 실패: " + response);
            }
        } catch (IOException e) {
            throw new IOException("연결 초기화 실패: " + e.getMessage(), e);
        }
    }

    private void login(String email, String password) throws IOException {
        // 이메일 및 비밀번호로 서버에 로그인하는 메서드
        String tag = "A001"; // 각 명령어에는 고유 식별자 태그가 필요
        sendCommand(tag + " LOGIN " + email + " " + password); // 로그인 명령 전송

        // 서버로부터 로그인 응답 확인
        String response = readResponse();
        while (!response.contains(tag)) {  // 태그가 포함된 응답이 올 때까지 읽음
            response = readResponse();
        }
        if (!response.contains(tag + " OK")) { // OK 응답이 없으면 로그인 실패 처리
            throw new IOException("로그인 실패: " + response);
        }
    }

    private void sendCommand(String command) throws IOException {
        // 서버로 명령을 전송하는 메서드
        outToServer.writeBytes(command + "\r\n"); // 명령어는 CRLF(\r\n)으로 종료
        System.out.println("Sent: " + command); // 전송된 명령 로그
    }

    private void readInbox() throws IOException {
        // 받은 편지함을 선택하고 이메일 정보를 가져오는 메서드
        String tag = "A002"; // 명령어 태그
        System.out.println("Sending: " + tag + " SELECT INBOX");
        sendCommand(tag + " SELECT INBOX"); // 받은 편지함 선택
        String response = readMultilineResponse();
        gmailFetchingInformations=new ArrayList<>();

        // 받은 편지함 내 메일 개수 확인
        for (String line : response.split("\n")) {
            if (line.matches("\\* \\d+ EXISTS")) { // 메일 개수 정보를 포함하는 줄을 찾음
                int totalMessages = Integer.parseInt(line.split(" ")[1]); // 메일 개수 추출
                if (totalMessages == 0) {
                    System.out.println("메일함이 비어있습니다.");
                    return;
                }

                // 가장 최근 메일 10개만 가져오기 (총 개수가 10개보다 적으면 모두 가져옴)
                int start = Math.max(1, totalMessages - 9); // 가져올 메일의 시작 위치 계산
                tag = "A003";
                System.out.println("Sending: " + tag + " FETCH command for messages " + totalMessages + " to " + start);

                // 메일 조회 명령어 전송
                sendCommand(tag + " FETCH " + totalMessages + ":" + start + " (BODY[HEADER.FIELDS (FROM SUBJECT DATE)])");
                response = readMultilineResponse(); // 메일 정보를 받음
                parseAndDisplayEmails(response); // 받은 응답을 파싱하고 표시
                return;
            }
        }

        System.out.println("메일함 정보를 가져올 수 없습니다."); // 메일 개수 정보를 찾지 못한 경우
    }

    private String readResponse() throws IOException {
        // 서버에서 단일 응답을 읽는 메서드
        String response = inFromServer.readLine(); // 한 줄 읽기
        if (response == null) {
            throw new IOException("서버로부터 응답이 없습니다.");
        }
        System.out.println("Received: " + response); // 디버깅을 위한 응답 출력
        return response;
    }

    private String readMultilineResponse() throws IOException {
        // 서버로부터 여러 줄의 응답을 읽어들임
        StringBuilder response = new StringBuilder();
        String line;
        int bracketCount = 0;
        boolean inLiteral = false;  // 리터럴 데이터를 처리 중인지 여부

        while ((line = inFromServer.readLine()) != null) {
            System.out.println("Received: " + line); // 받은 각 줄 출력
            response.append(line).append("\n");

            if (!inLiteral) {
                // 리터럴 데이터의 시작을 확인
                if (line.matches(".*\\{\\d+\\}$")) {
                    inLiteral = true;
                    continue;
                }
                // 열고 닫는 중괄호의 개수 계산
                bracketCount += countCharacter(line, '{') - countCharacter(line, '}');
            } else {
                inLiteral = false; // 리터럴 데이터 처리 완료
            }

            // 응답이 완료된 조건: 태그가 있으며 OK, NO, BAD로 끝나는 경우
            if (bracketCount == 0 && !inLiteral &&
                    (line.matches("A\\d{3} OK.*") || line.matches("A\\d{3} NO.*") || line.matches("A\\d{3} BAD.*"))) {
                break;
            }
        }
        return response.toString();
    }

    private int countCharacter(String str, char target) {
        // 문자열에서 특정 문자의 개수를 세는 메서드
        return (int) str.chars().filter(ch -> ch == target).count();
    }

    private void parseAndDisplayEmails(String response) {
        // 응답을 파싱하여 이메일 정보를 출력
        String[] entries = response.split("\\* \\d+ FETCH");
        System.out.println("\n=== 최근 메일 목록 ===\n");

        int emailCount = 0;

        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;

            StringBuilder emailContent = new StringBuilder();
            emailCount++;

            // 각 이메일의 헤더 정보를 추출
          from=extractHeaderValue(entry, "From: ");
          subject = extractHeaderValue(entry, "Subject: ");
          date = extractHeaderValue(entry, "Date: ");

            if (!from.isEmpty()) {
                emailContent.append("보낸사람: ").append(decodeHeader(from)).append("\n");
            }
            if (!subject.isEmpty()) {
                emailContent.append("제목: ").append(decodeHeader(subject)).append("\n");
            }
            if (!date.isEmpty()) {
                emailContent.append("날짜: ").append(date).append("\n");
            }
            gmailFetchingInformations.add(new FetchingInformation(decodeHeader(from),date,decodeHeader(subject)));

            printEmail(emailCount, emailContent.toString());
        }

        if (emailCount == 0) {
            System.out.println("표시할 메일이 없습니다.");
        } else {
            System.out.println("\n총 " + emailCount + "개의 메일을 불러왔습니다.");
        }
    }

    private String extractHeaderValue(String entry, String headerName) {
        // 특정 헤더 값 추출 메서드
        int startIndex = entry.indexOf(headerName);
        if (startIndex == -1) return "";
        int endIndex = entry.indexOf("\n", startIndex);
        if (endIndex == -1) endIndex = entry.length();
        return entry.substring(startIndex + headerName.length(), endIndex).trim();
    }

    private void printEmail(int count, String content) {
        // 이메일 정보 출력
        System.out.println("─".repeat(60));
        System.out.println("[" + count + "번째 메일]");
        System.out.println(content.trim());
        System.out.println("─".repeat(60));
    }

    private String decodeHeader(String header) {
        // 메일 헤더의 인코딩을 해제
        try {
            StringBuilder result = new StringBuilder();
            int start = 0;

            while (true) {
                int beginIndex = header.indexOf("=?", start);
                if (beginIndex == -1) {
                    result.append(header.substring(start));
                    break;
                }

                if (beginIndex > start) {
                    result.append(header.substring(start, beginIndex));
                }

                int endIndex = header.indexOf("?=", beginIndex);
                if (endIndex == -1) {
                    result.append(header.substring(beginIndex));
                    break;
                }

                String encodedText = header.substring(beginIndex + 2, endIndex);
                String[] parts = encodedText.split("\\?");
                if (parts.length >= 3) {
                    String charset = parts[0];
                    String encoding = parts[1];
                    String encodedContent = parts[2];

                    try {
                        if (encoding.equalsIgnoreCase("B")) {
                            byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
                            result.append(new String(decodedBytes, charset));
                        } else if (encoding.equalsIgnoreCase("Q")) {
                            result.append(decodeQuotedPrintable(encodedContent, charset));
                        }
                    } catch (Exception e) {
                        result.append(header.substring(beginIndex, endIndex + 2));
                    }
                }

                start = endIndex + 2;
            }

            return result.toString().trim();
        } catch (Exception e) {
            return header.trim();
        }
    }

    private String decodeQuotedPrintable(String encodedText, String charset) throws UnsupportedEncodingException {
        // Quoted-Printable 인코딩을 해제
        StringBuilder decoded = new StringBuilder();
        for (int i = 0; i < encodedText.length(); i++) {
            char c = encodedText.charAt(i);
            if (c == '_') {
                decoded.append(' ');
            } else if (c == '=' && i + 2 < encodedText.length()) {
                String hex = encodedText.substring(i + 1, i + 3);
                decoded.append((char) Integer.parseInt(hex, 16));
                i += 2;
            } else {
                decoded.append(c);
            }
        }
        return new String(decoded.toString().getBytes(StandardCharsets.ISO_8859_1), charset);
    }

    @Override
    public void close() throws IOException {
        // 모든 자원 정리
        try {
            if (outToServer != null) {
                try {
                    sendCommand("A004 LOGOUT");
                    readResponse();  // LOGOUT 명령 응답 확인
                } catch (IOException ignored) {
                }
                outToServer.close();
            }
            if (inFromServer != null) {
                inFromServer.close();
            }
            if (sslSocket != null && !sslSocket.isClosed()) {
                sslSocket.close();
            }
        } catch (IOException e) {
            System.err.println("연결 종료 중 오류: " + e.getMessage());
            throw e;
        }
    }
    public static List<computerNetwork.webSocket.dto.FetchingInformation> getFetchingInfo(){
        return gmailFetchingInformations;
    }
}
