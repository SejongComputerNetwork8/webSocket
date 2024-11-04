package computerNetwork.webSocket.javafx;

import computerNetwork.webSocket.dto.FetchingInformation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MailSidebar extends VBox {

    public MailSidebar(Stage primaryStage, MailPage mailPage, List<FetchingInformation> fetchingInformations) {



        setPadding(new Insets(10));
        setSpacing(10);
        setStyle("-fx-background-color: #f0f0f0;");

        // 각 버튼 생성
        Button composeButton = new Button("메일 쓰기");
        Button allMailsButton = new Button("전체 메일함");

        // 버튼 클릭 이벤트 설정
        composeButton.setOnAction(event -> {
            WriteMail writeMail = new WriteMail(); // WriteMail 인스턴스 생성
            writeMail.openComposeMailWindow(primaryStage, ""); // 메일 쓰기 창 열기, 기본적으로 받는 사람은 빈 문자열
        });

        allMailsButton.setOnAction(event -> mailPage.loadAllMails(fetchingInformations));

        // 버튼을 사이드바에 추가
        getChildren().addAll(composeButton, allMailsButton);
    }
}
