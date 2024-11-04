package computerNetwork.webSocket.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WriteMail {

    public void openComposeMailWindow(Stage owner, String recipient) {
        Stage composeStage = new Stage();
        composeStage.initModality(Modality.WINDOW_MODAL); // 부모 창이 활성화된 상태에서만 작동
        composeStage.initOwner(owner); // 부모 창 설정
        composeStage.setTitle("메일 쓰기");

        // 레이아웃 설정
        VBox layout = new VBox();
        layout.setPadding(new Insets(10));
        layout.setSpacing(10);

        // 보낼 메일 주소 입력 필드
        Label emailLabel = new Label("보낼 주소:");
        TextField emailField = new TextField();
        emailField.setPromptText("이메일 주소 입력");
        emailField.setText(recipient); // 답장할 때 받는 사람으로 설정

        // 제목 입력 필드
        Label subjectLabel = new Label("제목:");
        TextField subjectField = new TextField();
        subjectField.setPromptText("메일 제목 입력");

        // 메일 내용 입력 필드
        Label contentLabel = new Label("내용:");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("메일 내용을 입력하세요.");

        // 파일 첨부 버튼
        Button attachButton = new Button("파일 첨부");
        Label attachedFilesLabel = new Label("첨부된 파일: 없음");
        ArrayList<File> attachedFiles = new ArrayList<>(); // 첨부된 파일 목록

        attachButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("파일 선택");
            // 여러 파일 선택 가능하도록 설정
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("모든 파일", "*.*"),
                    new FileChooser.ExtensionFilter("텍스트 파일", "*.txt"),
                    new FileChooser.ExtensionFilter("이미지 파일", "*.png", "*.jpg", "*.gif")
            );
            List<File> files = fileChooser.showOpenMultipleDialog(composeStage);
            if (files != null) {
                attachedFiles.addAll(files);
                // 첨부된 파일 목록 업데이트
                StringBuilder fileNames = new StringBuilder("첨부된 파일: ");
                for (File file : attachedFiles) {
                    fileNames.append(file.getName()).append(", ");
                }
                // 마지막 쉼표 제거
                if (fileNames.length() > 2) {
                    fileNames.setLength(fileNames.length() - 2);
                }
                attachedFilesLabel.setText(fileNames.toString());
            }
        });

        // 전송 버튼
        Button sendButton = new Button("보내기");
        sendButton.setOnAction(e -> {
            String email = emailField.getText();
            String subject = subjectField.getText();
            String content = contentArea.getText();
            // 여기서 전송 로직을 추가하면 됩니다.
            System.out.println("보낼 메일 주소: " + email);
            System.out.println("제목: " + subject);
            System.out.println("메일 내용: " + content);
            System.out.println("첨부된 파일 수: " + attachedFiles.size());
            composeStage.close(); // 창 닫기
        });

        // 하단 버튼 영역을 우측 하단에 고정
        HBox buttonLayout = new HBox(attachButton, sendButton);
        buttonLayout.setSpacing(10);
        buttonLayout.setAlignment(Pos.CENTER_RIGHT); // 버튼을 우측 정렬

        // 레이아웃에 요소 추가
        layout.getChildren().addAll(emailLabel, emailField, subjectLabel, subjectField, contentLabel, contentArea, attachedFilesLabel, buttonLayout);

        // VBox의 마지막에 버튼 레이아웃을 추가하여 버튼이 아래쪽에 고정되도록 설정
        VBox.setVgrow(contentArea, Priority.ALWAYS); // 내용 입력 영역이 가능한 공간을 차지하도록 설정

        // 새로운 Scene 설정
        Scene composeScene = new Scene(layout, 400, 400);
        composeStage.setScene(composeScene);
        composeStage.show(); // 창 열기
    }
}
