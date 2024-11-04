package computerNetwork.webSocket.javafx;

import computerNetwork.webSocket.dto.FetchingInformation;
import computerNetwork.webSocket.dto.UserInfo;
import computerNetwork.webSocket.gmail.GmailFetcher;
import computerNetwork.webSocket.naver.NaverFetcher;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class HomePage extends Application {
    private UserData userData; // UserData 인스턴스
    private static UserInfo userInfo;
    private GmailFetcher gmailFetcher;
    private NaverFetcher naverFetcher;


    public void start(Stage primaryStage) {
        // 홈 화면 생성
        VBox homeBox = createHomeScreen(primaryStage);

        // Scene 설정
        Scene scene = new Scene(homeBox, 600, 400);
        primaryStage.setTitle("홈 화면");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    //홈 화면 생성 각종 UI틀 제공
    private VBox createHomeScreen(Stage primaryStage) {
        //홈 화면 바탕 설정
        VBox homeBox = new VBox();
        homeBox.setPadding(new Insets(20));
        homeBox.setSpacing(10);
        homeBox.setStyle("-fx-background-color: #f0f0f0;");

        // 네이버 구글 라디오 버튼 생성
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton googleButton = new RadioButton("Google");
        googleButton.setToggleGroup(toggleGroup);
        googleButton.setSelected(true); // 기본적으로 Google 버튼 선택
        RadioButton naverButton = new RadioButton("Naver");
        naverButton.setToggleGroup(toggleGroup);

        // 아이디 입력 필드
        TextField usernameField = new TextField();
        usernameField.setPromptText("아이디");

        // 비밀번호 입력 필드
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("비밀번호");

        // 로그인 회원가입 버튼
        Button loginButton = new Button("로그인");
        Button signupButton = new Button("회원가입");


        // 로그인 버튼 이벤트
        loginButton.setOnAction(event -> {
            RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
            // 로그인 로직 추가
            System.out.println("로그인 버튼 클릭됨");
            //여기서 로그인 아이디 비밀번호를 입력받은 것과 웹사이트 답변 비교 후 MailPage로 넘어가게 하기
            try {
                String selectedSite = selectedRadioButton.getText();
                String email = usernameField.getText();
                String password = passwordField.getText();
                userData = new UserData(email, password); // 예시 로그인 정보
                gmailFetcher=new GmailFetcher();
                naverFetcher=new NaverFetcher();
                userInfo = new UserInfo(userData.getEmail(), userData.getLoginInfo(), selectedSite); //user 정보 넘기기

                if (selectedSite.equals("Google")) {
                    List<FetchingInformation> fetchingInformations= naverFetcher.fetch(userData.getEmail(),userData.getLoginInfo());

                    //로그인 데이터
                    MailPage mailPage = new MailPage(userData);
                    mailPage.start(fetchingInformations,primaryStage); // MailPage를 시작 구글과 통신하는 함수 호출 인자 email, password

                } else if (selectedSite.equals("Naver")) {
                    List<FetchingInformation> fetchingInformations= naverFetcher.fetch(userData.getEmail(),userData.getLoginInfo());
                    //로그인 데이터
                    MailPage mailPage = new MailPage(userData);

                    mailPage.start(fetchingInformations,primaryStage); //  MailPage를 시작 구글과 통신하는 함수 호출 인자 email, password

                }
            } catch (Exception e) {
                System.out.println("로그인 오류가 발생함");
                e.printStackTrace();
            }
        });
        
        //회원가입 버튼 이벤트
        signupButton.setOnAction(event -> {
            RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
            if (selectedRadioButton != null) {
                String selectedSite = selectedRadioButton.getText();
                try {
                    if (selectedSite.equals("Google")) {
                        Desktop.getDesktop().browse(new URI("http://www.google.co.kr"));
                    } else if (selectedSite.equals("Naver")) {
                        Desktop.getDesktop().browse(new URI("http://www.naver.com"));
                    }
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        // 홈 화면에 요소 추가
        homeBox.getChildren().addAll(usernameField, passwordField, googleButton, naverButton, loginButton, signupButton);
        return homeBox;
    }
    public static Record getUserInfo(){
        return userInfo;
    }
}
