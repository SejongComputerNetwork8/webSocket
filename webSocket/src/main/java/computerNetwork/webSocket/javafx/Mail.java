package computerNetwork.webSocket.javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Mail {
    private final StringProperty sender; // 보낸 사람
    private final StringProperty date; // 받는 사람
    private final StringProperty subject; // 제목

    public Mail(String sender, String date, String subject) {
        this.sender = new SimpleStringProperty(sender);
        this.date = new SimpleStringProperty(date);
        this.subject = new SimpleStringProperty(subject);
    }

    public String getSender() {
        return sender.get();
    }

    public StringProperty senderProperty() {
        return sender; // 보낸 사람 프로퍼티 반환
    }

    public String getDate() {
        return date.get();
    }

    public StringProperty dateProperty() {
        return date; // 받는 사람 프로퍼티 반환
    }

    public String getSubject() {
        return subject.get();
    }

    public StringProperty subjectProperty() {
        return subject; // 제목 프로퍼티 반환
    }

    // 30개의 메일 예시를 생성하는 정적 메서드
    public static ObservableList<Mail> generateSampleMails() {
        return FXCollections.observableArrayList(
                new Mail("alice@example.com", "2023-01-01", "안녕하세요1!"),
                new Mail("alice@example.com", "2023-01-02", "안녕하세요2!"),
                new Mail("alice@example.com", "2023-01-03", "안녕하세요3!"),
                new Mail("alice@example.com", "2023-01-04", "안녕하세요4!"),
                new Mail("alice@example.com", "2023-01-05", "안녕하세요5!"),
                new Mail("bob@example.com", "2023-01-06", "회의 일정입니다."),
                new Mail("charlie@example.com", "2023-01-07", "프로젝트 업데이트"),
                new Mail("diana@example.com", "2023-01-08", "할 일 목록"),
                new Mail("eve@example.com", "2023-01-09", "새로운 메시지"),
                new Mail("frank@example.com", "2023-01-10", "업데이트 알림")
        );
    }
}
