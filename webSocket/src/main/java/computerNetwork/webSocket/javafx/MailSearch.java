package computerNetwork.webSocket.javafx;

import javafx.collections.ObservableList;
import javafx.scene.control.TextField;

public class MailSearch {

    private TextField searchField; // 검색어 입력 필드
    private MailPage mailPage; // MailPage 인스턴스

    public MailSearch(TextField searchField, MailPage mailPage) {
        this.searchField = searchField;
        this.mailPage = mailPage; // MailPage 인스턴스 저장
    }

    public void performSearch(ObservableList<Mail> mailList, String currentMailbox) {
        String query = searchField.getText().toLowerCase(); // 검색어 소문자로 변환
        ObservableList<Mail> filteredMails;

        // 필터링 로직
        filteredMails = mailList.filtered(mail ->
                mail.getSender().toLowerCase().contains(query) ||
                        mail.getSubject().toLowerCase().contains(query)
        );

        // 필터링된 결과를 테이블에 설정
        mailPage.getMailTable().setMailItems(filteredMails); // 필터링된 메일을 테이블에 설정
    }
}
