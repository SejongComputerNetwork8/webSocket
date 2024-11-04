package computerNetwork.webSocket.javafx;

import computerNetwork.webSocket.dto.UserInfo;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;

public class MailDetail {

    private WriteMail writeMail; // WriteMail 인스턴스

    public MailDetail(WriteMail writeMail) {
        this.writeMail = writeMail; // WriteMail 인스턴스 초기화
    }

    public void showMailDetails(UserInfo userInfo,Stage owner, String sender, String recipient, String subject) {
        Stage detailStage = new Stage();
        detailStage.initOwner(owner);
        detailStage.setTitle("메일 세부 정보");

        // 레이아웃 설정
        VBox layout = new VBox();
        layout.setPadding(new Insets(10));
        layout.setSpacing(10);

        // 메일 세부 정보 표시
        layout.getChildren().addAll(
                new Label("보낸 사람: " + sender),
                new Label("받는 사람: " + recipient),
                new Label("제목: " + subject)
        );

        // 답장하기 버튼
        Button replyButton = new Button("답장하기");
        replyButton.setOnAction(event -> {
            writeMail.openComposeMailWindow(userInfo,owner, sender); // 답장할 때 보낸이를 받는 사람으로 설정
            // detailStage.close(); // 이 줄을 제거하여 창이 닫히지 않도록 합니다.
        });

        // 버튼을 포함할 HBox 생성
        HBox buttonLayout = new HBox(replyButton);
        buttonLayout.setAlignment(Pos.CENTER_RIGHT); // 버튼을 우측 정렬
        buttonLayout.setSpacing(10); // 버튼 간격 설정

        // 레이아웃에 버튼 추가
        layout.getChildren().add(buttonLayout);

        // Scene 설정
        Scene detailScene = new Scene(layout, 400, 300);
        detailStage.setScene(detailScene);
        detailStage.show(); // 창 열기
    }
}
