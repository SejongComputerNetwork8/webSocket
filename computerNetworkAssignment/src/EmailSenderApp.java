//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.GridPane;
//import javafx.stage.Stage;

public class EmailSenderApp  {
    public static void main(String[] args) {
//        launch(args);
    }

//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Email Sender");
//
//        // 레이아웃 설정
//        GridPane grid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setPadding(new Insets(25, 25, 25, 25));
//
//        // 플랫폼 선택
//        Label platformLabel = new Label("Platform:");
//        ComboBox<String> platformComboBox = new ComboBox<>();
//        platformComboBox.getItems().addAll("Naver");
//        platformComboBox.setValue("Naver");
//
//        // Email, Password, Recipient, Subject, Body
//        Label emailLabel = new Label("Email:");
//        TextField emailField = new TextField();
//
//        Label passwordLabel = new Label("Password:");
//        PasswordField passwordField = new PasswordField(); // 비밀번호 입력 시 자동으로 *로 표시
//
//        Label recipientLabel = new Label("Recipient:");
//        TextField recipientField = new TextField();
//
//        Label messageLabel = new Label("Message:");
//        TextArea messageArea = new TextArea();
//
//        // Send Button
//        Button sendButton = new Button("Send Email");
//        Label statusLabel = new Label();
//
//        // Send 버튼 클릭 이벤트
//        sendButton.setOnAction(e -> {
//            String platform = platformComboBox.getValue().toLowerCase();
//            String email = emailField.getText();
//            String password = passwordField.getText(); // 비밀번호 입력 값
//            String recipient = recipientField.getText();
//            String message = messageArea.getText();
//
//            // 여기서 WebSocket 클래스의 메서드를 호출하여 이메일을 보냅니다.
//            WebSocket webSocket = new WebSocket();
//            try {
//                webSocket.sendNaverEmail(email, password, recipient, message);
//                statusLabel.setText("이메일 전송 완료!");
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                statusLabel.setText("이메일 전송 실패!");
//            }
//        });
//
//        // 레이아웃에 요소 추가
//        grid.add(platformLabel, 0, 0);
//        grid.add(platformComboBox, 1, 0);
//
//        grid.add(emailLabel, 0, 1);
//        grid.add(emailField, 1, 1);
//
//        grid.add(passwordLabel, 0, 2);
//        grid.add(passwordField, 1, 2); // 비밀번호 필드 추가
//
//        grid.add(recipientLabel, 0, 3);
//        grid.add(recipientField, 1, 3);
//
//        grid.add(messageLabel, 0, 4);
//        grid.add(messageArea, 1, 4);
//
//        grid.add(sendButton, 1, 5);
//        grid.add(statusLabel, 1, 6);
//
//        // Scene 설정 및 표시
//        Scene scene = new Scene(grid, 400, 500);
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
}

