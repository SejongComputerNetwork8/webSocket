package computerNetwork.webSocket.javafx;

import computerNetwork.webSocket.dto.FetchingInformation;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

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
    public static ObservableList<Mail> generateSampleMails(List<FetchingInformation> fetchingInformations) {
        // fetchingInformations 리스트를 순회하며 Mail 객체 생성
        ObservableList<Mail> mails = FXCollections.observableArrayList();
        System.out.println("!!!!"+fetchingInformations);

        for (FetchingInformation info : fetchingInformations) {
            // FetchingInformation 객체에서 이메일 발신자, 날짜, 내용을 추출
            String sender = info.sendingPerson(); // 예: 발신자
            String date = info.date();     // 예: 날짜
            String content = info.title(); // 예: 내용

            // Mail 객체 생성 후 리스트에 추가
            mails.add(new Mail(sender, date, content));
        }

        return mails;
    }
}
