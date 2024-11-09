package computerNetwork.webSocket.ui;

import computerNetwork.webSocket.gmail.GmailSender;

import java.io.IOException;
import java.util.Scanner;

public class UI {
    private Scanner scanner;

    public UI() {
        scanner = new Scanner(System.in);
    }

    public void run() throws IOException {
        // 플랫폼 선택
        System.out.println("사용할 플랫폼을 선택하세요 (Google 또는 Naver): ");
        String platform = scanner.nextLine().trim().toLowerCase();

        // 이메일 계정 정보 입력
        System.out.print("아이디 (이메일 주소): ");
        String email = scanner.nextLine().trim();

        System.out.print("비밀번호 (구글의 경우 앱 비밀번호): ");
        String password = scanner.nextLine().trim();
        // 받는 사람 이메일과 메시지 입력
        System.out.print("받는 사람 이메일: ");
        String recipient = scanner.nextLine().trim();
        System.out.print("보낼 메시지 내용을 입력하세요: ");
        String message = scanner.nextLine().trim();

        //TODO: 받은 정보를 통해서 websocket program 구현
        GmailSender gmailSender=new GmailSender(email,password);
        gmailSender.sendEmail("msw0909@naver.com","this is subject","mailmail");

    }
}
