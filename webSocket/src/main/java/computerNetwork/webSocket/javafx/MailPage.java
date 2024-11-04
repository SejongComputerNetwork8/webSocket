package computerNetwork.webSocket.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MailPage {
    private UserData userData; // UserData 인스턴스
    private Label currentMailboxLabel; // 현재 메일함을 표시하는 레이블
    private MailTable mailTable; // 메일 리스트를 표시하는 테이블
    private ObservableList<Mail> allMails; // 전체 메일 리스트
    private MailSearch mailSearch; // 검색 기능 클래스
    private TextField searchField; // 검색어 입력 필드
    private String currentMailbox; // 현재 메일함 상태
    private String userEmail; // 사용자 메일 주소
    private int currentPage; // 현재 페이지
    public static final int PAGE_SIZE = 15; // 한 페이지에 표시할 메일 수
    private List<Mail> currentMails; // 현재 페이지에 표시할 메일 리스트
    private MailDetail mailDetail; // 메일 세부 정보 창
    private WriteMail writeMail; // 메일 쓰기 인스턴스

    public MailPage(UserData userData) {
        // 사용자 메일 주소 초기화
        this.userData = userData; //메일 주소 설정 받아야 함.
        this.currentPage = 0; // 초기 페이지 설정
        this.currentMails = new ArrayList<>(); // 현재 페이지 메일 리스트 초기화
        this.allMails = FXCollections.observableArrayList(); // allMails 초기화
        this.writeMail = new WriteMail(); // WriteMail 인스턴스 초기화
        this.mailDetail = new MailDetail(writeMail); // WriteMail 인스턴스를 전달하여 MailDetail 초기화
        this.userEmail = userData.getEmail();
    }

    public void start(Stage primaryStage) {
        // 메일박스의 루트 레이아웃
        BorderPane mailboxRoot = new BorderPane();

        // 좌측 사이드바 생성
        MailSidebar sidebar = new MailSidebar(primaryStage, this);
        mailboxRoot.setLeft(sidebar);

        // 메일 내용 표시 영역 (테이블)
        mailTable = new MailTable();

        // 더블 클릭 이벤트 처리
        mailTable.getTableView().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Mail selectedMail = mailTable.getSelectedMail(); // 선택된 메일 가져오기
                if (selectedMail != null) {
                    showMailDetails(selectedMail); // 세부 정보 표시
                }
            }
        });

        // 현재 메일함 레이블을 포함하는 상단 레이아웃
        VBox mailContentLayout = new VBox();
        currentMailboxLabel = new Label("현재 메일함: 전체 메일함");
        currentMailboxLabel.setPadding(new Insets(10));
        mailContentLayout.getChildren().addAll(currentMailboxLabel, mailTable.getTableView());

        mailboxRoot.setCenter(mailContentLayout); // 메일 내용 레이아웃을 중앙에 배치


        // 우측 상단에 검색어 입력 박스와 검색 버튼 추가
        searchField = new TextField();
        searchField.setPromptText("검색어 입력");
        mailSearch = new MailSearch(searchField, this); // MailSearch 인스턴스 생성 시 MailPage 전달

        Button searchButton = new Button("검색");
        searchButton.setOnAction(event -> {
            mailSearch.performSearch(allMails, currentMailbox); // 전체 메일 리스트를 검색
        });

        HBox searchBox = new HBox(searchField, searchButton);
        searchBox.setPadding(new Insets(10));
        searchBox.setSpacing(10);
        searchBox.setAlignment(Pos.CENTER_RIGHT); // 우측 정렬
        mailboxRoot.setTop(searchBox); // 검색 박스를 상단에 배치

        // 새로운 Scene 설정
        Scene mailboxScene = new Scene(mailboxRoot, 800, 600);
        primaryStage.setTitle("메일함");
        primaryStage.setScene(mailboxScene);

        // 기본적으로 전체 메일함 선택 후 메일 리스트 로드
        loadAllMails();
    }

    public void updateMailTable() {
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allMails.size());
        currentMails = allMails.subList(start, end); // 현재 페이지 메일 리스트 설정
        mailTable.setMailItems(FXCollections.observableArrayList(currentMails)); // 테이블에 현재 페이지 메일 리스트 설정


        System.out.println("페이지 " + (currentPage + 1) + " 로드");
    }

    public void loadAllMails() {
        currentMailboxLabel.setText("현재 메일함: 전체 메일함");
        currentMailbox = "전체"; // 현재 메일함 상태 설정

        // Mail 클래스에서 생성한 30개의 메일 예시를 가져옵니다.
        allMails = Mail.generateSampleMails(); // ObservableList로 설정됨

        // 페이지 초기화
        currentPage = 0;
        updateMailTable(); // 테이블 업데이트
    }



    private void showMailDetails(Mail mail) {
        mailDetail.showMailDetails(
                (Stage) mailTable.getTableView().getScene().getWindow(),
                mail.getSender(),
                mail.getDate(),
                mail.getSubject()
        );
    }

    public int getCurrentPage() {
        return currentPage; // 현재 페이지 반환
    }

    public void setCurrentPage(int page) {
        this.currentPage = page; // 현재 페이지 설정
        updateMailTable(); // 테이블 업데이트
    }

    public String getCurrentMailbox() {
        return currentMailbox;
    }

    public MailTable getMailTable() {
        return mailTable; // MailTable 객체를 반환하는 메서드 추가
    }

    public ObservableList<Mail> getAllMails() {
        return allMails; // 전체 메일 리스트 반환
    }
}
