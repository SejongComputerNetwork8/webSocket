package computerNetwork.webSocket.naver;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class NaverFetcher implements AutoCloseable {
    // try-with-resources 문에서 자동으로 자원을 해제 가능하게
    private static final String IMAP_HOST = "imap.naver.com";
    private static final int IMAP_PORT = 993; // 네이버 imap 서버 포트 번호
    private static final int TIMEOUT_MILLISECONDS = 10000; // 타임아웃을 10초로 설정

    private BufferedReader inFromServer; // 서버로부터 데이터를 읽어오는 스트림
    private DataOutputStream outToServer; // 데이터를 보내는 스트림 (바이트 형식)
    private SSLSocket sslSocket;

    public void send(String email, String password) throws IOException {
        try {
            initializeConnection(); // 서버 연결 초기화하고
            login(email, password); // 로그인
            readInbox(); // 이메일 함 읽기
        } finally {
            close();
        }
    }

    private void initializeConnection() throws IOException {
        try {
            // SSL 소켓 팩토리로 보안 소켓 생성
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault(); // 시스템의 기본 SSL 소켓 팩토리 가져옴
            sslSocket = (SSLSocket) factory.createSocket(IMAP_HOST, IMAP_PORT); // 해당 호스트와 포트로 SSL 소켓 생성
            sslSocket.setSoTimeout(TIMEOUT_MILLISECONDS);  // 타임아웃 설정

            // 입출력 스트림 설정
            inFromServer = new BufferedReader(// 버퍼링 추가해서 효율적으로 읽음
                    new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8)); // 바이트 스트림을 문자 스트림으로 변환 (UTF-8 인코딩)
            outToServer = new DataOutputStream(sslSocket.getOutputStream()); // 바이트 단위로 데이터 출력

            // 응답으로 서버 연결 확인
            String response = readResponse();
            if (!response.contains("OK")) { // OK 가 포함될 때만 연결 성공한 것으로 간주할 것임
                throw new IOException("IMAP 서버 연결 실패: " + response);
            }
        } catch (IOException e) {
            throw new IOException("연결 초기화 실패: " + e.getMessage(), e);
        }
    }

    private void login(String email, String password) throws IOException {
        String tag = "A001"; // IMAP 명령어 식별자
        sendCommand(tag + " LOGIN " + email + " " + password); // 로그인 명령어 전송

        //서버 응답 확인
        String response = readResponse();
        while (!response.contains(tag)) {  // tag("A001")를 포함한 응답을 찾을 때까지 읽음
            response = readResponse();
        }
        if (!response.contains(tag + " OK")) { // 로그인 성공 여부 확인하고 실패하면 로그인 실패 출력
            throw new IOException("로그인 실패: " + response);
        }
    }

    private void sendCommand(String command) throws IOException {
        outToServer.writeBytes(command + "\r\n"); // IMAP 프로토콜에서 각 명령어는 Carriage Return(CR)과 Line Feed(LF)로 끝나야해서 \r\n 추가
        System.out.println("Sent: " + command);  // 디버깅용 로그 추가
    }

    private void readInbox() throws IOException {
        String tag = "A002"; // 명령어 식별자
        System.out.println("Sending: " + tag + " SELECT INBOX");
        sendCommand(tag + " SELECT INBOX"); // INBOX 선택 명령어 전송
        String response = readMultilineResponse();

        // 메일 총 개수 파악
        for(String line : response.split("\n")) {
            if(line.matches("\\* \\d+ EXISTS")) { // 메일 개수 정보를 포함한 줄 찾고
                int totalMessages = Integer.parseInt(line.split(" ")[1]); // 메일 개수를 추출함
                if (totalMessages == 0) {
                    System.out.println("메일함이 비어있습니다.");
                    return;
                }

                // 최근 10개의 메일만 가져오기 (역순으로 가져옴, 과거 > 현재)
                int start = Math.max(1, totalMessages - 9); // 시작 위치 계산
                tag = "A003";
                System.out.println("Sending: " + tag + " FETCH command for messages " + totalMessages + " to " + start);

                // FETCH 명령어: totalMessages부터 start까지 역순으로 가져오기
                sendCommand(tag + " FETCH " + totalMessages + ":" + start + " (BODY[HEADER.FIELDS (FROM SUBJECT DATE)])");

                response = readMultilineResponse(); // 메일 정보 받기
                parseAndDisplayEmails(response); // 메일 정보 받아서 파싱하고 표시
                return;
            }
        }

        // EXISTS를 찾지 못했을 경우
        System.out.println("메일함 정보를 가져올 수 없습니다.");
    }

    private String readResponse() throws IOException {
        String response = inFromServer.readLine(); // BufferedReader를 통해 서버로부터 한 줄씩 읽음, 줄 끝의 개행문자(\r\n)는 자동으로 제거
        if (response == null) {
            throw new IOException("서버로부터 응답이 없습니다.");
        }
        System.out.println("Received: " + response);  // 디버깅용 응답 로그 받아서 출력
        return response;
    }

    private String readMultilineResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        int bracketCount = 0;
        boolean inLiteral = false;  // 리터럴 데이터 처리 중인지 확인

        while ((line = inFromServer.readLine()) != null) {
            System.out.println("Received: " + line);
            response.append(line).append("\n");

            if (!inLiteral) {
                // 리터럴 데이터 시작 확인
                if (line.matches(".*\\{\\d+\\}$")) {
                    inLiteral = true;
                    continue;
                }
                // 중괄호 개수 카운팅, 중첩된 데이터 구조를 올바르게 처리하기 위한 용도, 열기랑 닫기 개수 체크
                bracketCount += countCharacter(line, '{') - countCharacter(line, '}');
            } else {
                // 리터럴 데이터 처리 완료
                inLiteral = false;
            }

            // 응답 완료 조건 확인
            // 응답 완료 조건은 태그(A001, A002 등)로 시작하고 OK, NO, BAD 중 하나로 끝나며
            //리터럴 데이터 처리 중이 아니고 중괄호가 모두 닫혀있는 상태임
            if (bracketCount == 0 && !inLiteral &&
                    (line.matches("A\\d{3} OK.*") || // 성공일 때
                            line.matches("A\\d{3} NO.*") || // 실패일 때
                            line.matches("A\\d{3} BAD.*"))) { // 잘못된 명령어일 때
                break;
            }
        }
        return response.toString();
    }

    // 문자 개수 세기
    private int countCharacter(String str, char target) {
        return (int) str.chars().filter(ch -> ch == target).count();
    }

    //이메일 파싱 및 표시
    private void parseAndDisplayEmails(String response) {
        String[] lines = response.split("\n");
        System.out.println("\n=== 최근 메일 목록 ===\n");

        StringBuilder currentEmail = new StringBuilder();
        int emailCount = 0;
        boolean isCollectingEmail = false;

        for (String line : lines) {
            // FETCH 응답의 시작 확인
            if (line.matches("\\* \\d+ FETCH.*")) {
                // 이전 이메일 정보 출력
                if (currentEmail.length() > 0) {
                    printEmail(emailCount, currentEmail.toString());
                }
                currentEmail = new StringBuilder();
                isCollectingEmail = true;
                emailCount++;
                continue;
            }

            if (isCollectingEmail) {
                line = line.trim();
                if (line.startsWith("FROM: ")) {
                    String from = decodeHeader(line.substring(6)); // from 부분 제거하고 발신자 이름 디코딩
                    // 이메일 주소 추출 (<> 안의 내용), 발신자 이름과 이메일 주소 구분
                    String emailAddress = "";
                    int startIndex = from.indexOf('<');
                    int endIndex = from.indexOf('>');
                    if (startIndex >= 0 && endIndex > startIndex) {
                        emailAddress = from.substring(startIndex + 1, endIndex);
                        from = from.substring(0, startIndex).trim();
                    }
                    currentEmail.append("보낸사람: ").append(from);
                    if (!emailAddress.isEmpty()) {
                        currentEmail.append(" <").append(emailAddress).append(">");
                    }
                    currentEmail.append("\n");
                } else if (line.startsWith("SUBJECT: ")) {
                    String subject = decodeHeader(line.substring(9)); // subject 제거하고 제목 디코딩
                    if (!subject.trim().isEmpty()) {
                        currentEmail.append("제목: ").append(subject).append("\n");
                    } else {
                        currentEmail.append("제목: (제목 없음)\n");
                    }
                } else if (line.startsWith("DATE: ")) {
                    // 날짜 형식 정리
                    String date = line.substring(6).trim();// date 제거
                    if (date.endsWith("(KST)")) {
                        date = date.substring(0, date.length() - 5).trim(); /// kst 제거
                    }
                    currentEmail.append("날짜: ").append(date).append("\n");
                }
            }
        }

        // 마지막 이메일 출력
        if (currentEmail.length() > 0) {
            printEmail(emailCount, currentEmail.toString());
        }

        if (emailCount == 0) {
            System.out.println("표시할 메일이 없습니다.");
        } else {
            System.out.println("\n총 " + emailCount + "개의 메일을 불러왔습니다.");
        }
    }

    private void printEmail(int count, String content) {
        System.out.println("─".repeat(60));
        System.out.println("[" + count + "번째 메일]");
        System.out.println(content.trim());
        System.out.println("─".repeat(60));
    }

    private String decodeHeader(String header) {
        try {
            // 이메일 주소 부분 분리
            String emailPart = "";
            int emailStart = header.indexOf('<');
            int emailEnd = header.indexOf('>');
            if (emailStart >= 0 && emailEnd > emailStart) {
                emailPart = header.substring(emailStart, emailEnd + 1);
                header = header.substring(0, emailStart).trim();
            }

            StringBuilder result = new StringBuilder();
            int start = 0;

            while (true) {
                // "=?" 패턴 (MIME 인코딩의 시작) 찾기 (UTF-8뿐만 아니라 다른 인코딩도 처리)
                int beginIndex = header.indexOf("=?", start);
                if (beginIndex == -1) {
                    // 더 이상 인코딩된 부분이 없으면 나머지 텍스트 추가
                    result.append(header.substring(start));
                    break;
                }

                // 인코딩되지 않은 부분 추가
                if (beginIndex > start) {
                    result.append(header.substring(start, beginIndex));
                }

                // "?="(MIME 인코딩의 끝) 찾아서 인코딩된 부분의 끝 확인
                int endIndex = header.indexOf("?=", beginIndex);
                if (endIndex == -1) {
                    result.append(header.substring(beginIndex));
                    break;
                }

                // 인코딩 정보 파싱
                String encodedText = header.substring(beginIndex + 2, endIndex);
                String[] parts = encodedText.split("\\?");
                if (parts.length >= 3) {
                    String charset = parts[0];// 문자셋 (예: UTF-8)
                    String encoding = parts[1];// 인코딩 방식 (B: Base64, Q: Quoted-printable)
                    String encodedContent = parts[2];// 실제 인코딩된 내용

                    try {
                        if (encoding.equalsIgnoreCase("B")) {
                            // Base64 디코딩
                            byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
                            result.append(new String(decodedBytes, charset));
                        } else if (encoding.equalsIgnoreCase("Q")) {
                            // Quoted-printable 디코딩
                            try {
                                StringBuilder decodedContent = new StringBuilder();
                                for (int i = 0; i < encodedContent.length(); i++) {
                                    char c = encodedContent.charAt(i);
                                    if (c == '_') {
                                        // '_'는 공백을 나타냄
                                        decodedContent.append(' ');
                                    } else if (c == '=' && i + 2 < encodedContent.length()) {
                                        // =XX 형식의 16진수 값을 문자로 변환
                                        String hex = encodedContent.substring(i + 1, i + 3);
                                        try {
                                            int value = Integer.parseInt(hex, 16);
                                            decodedContent.append((char)value);
                                            i += 2;  // 16진수 2자리 건너뛰기
                                        } catch (NumberFormatException e) {
                                            // 잘못된 16진수 값이면 원본 그대로 추가
                                            decodedContent.append(c);
                                        }
                                    } else {
                                        // 일반 문자는 그대로 추가
                                        decodedContent.append(c);
                                    }
                                }
                                result.append(new String(decodedContent.toString().getBytes("ASCII"), charset));
                            } catch (Exception e) {
                                // 디코딩 실패시 원본 추가
                                result.append(encodedContent);
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        // Base64 디코딩 실패시 원본 추가
                        result.append(header.substring(beginIndex, endIndex + 2));
                    }
                }

                start = endIndex + 2;
            }

            // 이메일 주소 부분 추가
            String decodedResult = result.toString().trim();
            if (!emailPart.isEmpty()) {
                if (!decodedResult.isEmpty()) {
                    return decodedResult + " " + emailPart;
                } else {
                    return emailPart;
                }
            }

            return decodedResult;
        } catch (Exception e) {
            // 전체 처리 실패시 원본 반환
            return header.trim();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (outToServer != null) {
                try {
                    sendCommand("A004 LOGOUT"); // IMAP 로그아웃 명령
                    readResponse();  // 로그아웃 응답 대기
                } catch (IOException e) {
                    // 로그아웃 실패는 무시 (연결이 이미 끊어졌을 경우 고려, 다른 오류는 아래에서 로그로 표시)
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
}
