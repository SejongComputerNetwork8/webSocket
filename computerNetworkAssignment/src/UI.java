import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UI implements AutoCloseable {
    private final Scanner scanner;
    private static final String NAVER = "naver";
    private static final String GOOGLE = "google";

    public UI() {

        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
    }

    public void run() {
        try {
            showMainMenu();
        } catch (Exception e) {
            System.out.println("프로그램 실행 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            close();
        }
    }
    private void showMainMenu() throws IOException {
        while (true) {
            // 1. 플랫폼 선택
            String platform = selectPlatform();
            if (platform == null) {
                return;  // 프로그램 종료
            }

            // 2. 이메일 계정 정보 입력
            EmailCredentials credentials = getEmailCredentials();
            if (credentials == null) {
                continue;  // 메인 메뉴로 돌아가기
            }

            // 3. 작업 선택 (메일 보내기 또는 메일함 확인)
            while (true) {
                System.out.println("\n=== 작업 선택 ===");
                System.out.println("1. 메일 보내기");
                System.out.println("2. 메일함 확인");
                System.out.println("0. 이전 메뉴로");
                System.out.print("선택하세요: ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        handleEmailSending(platform, credentials);
                        break;
                    case "2":
                        handleEmailChecking(platform, credentials);
                        break;
                    case "0":
                        break;
                    default:
                        System.out.println("잘못된 선택입니다.");
                        continue;
                }

                if (choice.equals("0")) break;
            }
        }
    }

    private void handleEmailChecking(String platform, EmailCredentials credentials) {
        try {
            NaverMailbox mailbox = new NaverMailbox();
            System.out.println("\n메일함 확인 중...");
            mailbox.checkEmails(credentials.email(), credentials.password());
        } catch (Exception e) {
            System.out.println("메일함 확인 실패: " + e.getMessage());
        }
    }

    private void handleEmailSending(String platform, EmailCredentials credentials) {
        EmailContent content = getEmailContent();
        if (content == null) {
            return;
        }

        sendEmail(platform, credentials, content);
    }

    private String selectPlatform() {
        while (true) {
            System.out.println("\n사용할 플랫폼을 선택하세요:");
            System.out.println("1. Naver");
            System.out.println("2. Google (준비 중)");
            System.out.println("0. 이전 메뉴로");
            System.out.print("선택: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    return NAVER;
                case "2":
                    System.out.println("Gmail은 현재 구현되지 않았습니다.");
                    return null;
                case "0":
                    return null;
                default:
                    System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
            }
        }
    }

    private EmailCredentials getEmailCredentials() {
        System.out.print("\n이메일 주소: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            System.out.println("이메일 주소를 입력해야 합니다.");
            return null;
        }

        String password = getPassword();
        if (password == null) {
            return null;
        }

        return new EmailCredentials(email, password);
    }

    private String getPassword() {
        Console console = System.console();
        String password;

        if (console != null) {
            char[] passwordArray = console.readPassword("비밀번호: ");
            if (passwordArray == null || passwordArray.length == 0) {
                return null;
            }
            password = new String(passwordArray);
            // 보안을 위해 비밀번호 배열을 즉시 삭제
            java.util.Arrays.fill(passwordArray, ' ');
        } else {
            System.out.print("비밀번호: ");
            password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                return null;
            }
        }

        return password;
    }

    private EmailContent getEmailContent() {
        System.out.print("\n받는 사람 이메일: ");
        String recipient = scanner.nextLine().trim();
        if (recipient.isEmpty()) {
            System.out.println("받는 사람 이메일을 입력해야 합니다.");
            return null;
        }

        System.out.print("제목: ");
        String subject = scanner.nextLine().trim();
        if (subject.isEmpty()) {
            System.out.println("제목을 입력해야 합니다.");
            return null;
        }

        System.out.println("내용을 입력하세요 (입력 완료 시 엔터 후 '/send' 입력):");
        StringBuilder messageBuilder = new StringBuilder();
        String line;

        while (!(line = scanner.nextLine()).equals("/send")) {
            messageBuilder.append(line).append("\n");
        }

        String message = messageBuilder.toString().trim();
        if (message.isEmpty()) {
            System.out.println("메시지 내용을 입력해야 합니다.");
            return null;
        }

        return new EmailContent(recipient, subject, message);
    }

    private void sendEmail(String platform, EmailCredentials credentials, EmailContent content) {
        try (NaverWebSocket webSocket = new NaverWebSocket()) {
            System.out.println("\n이메일 전송 중...");
            webSocket.sendNaverEmail(
                    credentials.email(),
                    credentials.password(),
                    content.recipient(),
                    content.subject(),
                    content.message()
            );
            System.out.println("이메일 전송이 완료되었습니다!");
        } catch (IOException e) {
            System.out.println("이메일 전송 실패: " + e.getMessage());
            System.out.println("자세한 오류: " + e.getCause());
        }
    }

    @Override
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}

// 이메일 자격 증명을 위한 레코드
record EmailCredentials(String email, String password) {}

// 이메일 내용을 위한 레코드
record EmailContent(String recipient, String subject, String message) {}

// 이메일 자격 증명을 위한 레코드
record EmailCredentials(String email, String password) {}

// 이메일 내용을 위한 레코드
record EmailContent(String recipient, String subject, String message) {}
