package computerNetwork.webSocket.javafx;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class MailPageMove {
    private MailPage mailPage; // MailPage 인스턴스
    private int totalPages; // 총 페이지 수
    private HBox paginationBox; // 페이지 버튼을 담을 레이아웃

    public MailPageMove(MailPage mailPage) {
        this.mailPage = mailPage;
        this.paginationBox = new HBox(10); // 페이지 버튼 간격 설정
    }

    public HBox createPagination() {
        updatePagination(); // 초기 페이지 버튼 업데이트
        return paginationBox;
    }

    public void updatePagination() {
        paginationBox.getChildren().clear(); // 기존 버튼 제거

        // "처음" 버튼
        Button firstButton = new Button("처음");
        firstButton.setOnAction(event -> {
            mailPage.setCurrentPage(0);
            mailPage.updateMailTable();
        });
        paginationBox.getChildren().add(firstButton);

        // "이전" 버튼
        Button prevButton = new Button("이전");
        prevButton.setOnAction(event -> {
            if (mailPage.getCurrentPage() > 0) {
                mailPage.setCurrentPage(mailPage.getCurrentPage() - 1);
                mailPage.updateMailTable();
            }
        });
        paginationBox.getChildren().add(prevButton);

        // 페이지 번호 버튼 생성
        int currentPage = mailPage.getCurrentPage();
        int totalPages = (int) Math.ceil((double) mailPage.getAllMails().size() / MailPage.PAGE_SIZE);

        // 버튼 생성 로직
        for (int i = -2; i <= 2; i++) {
            int pageIndex = currentPage + i;
            if (pageIndex >= 0 && pageIndex < totalPages) {
                Button pageButton = new Button(String.valueOf(pageIndex + 1)); // 페이지 번호는 1부터 시작
                final int index = pageIndex; // 페이지 인덱스 저장
                pageButton.setOnAction(event -> {
                    mailPage.setCurrentPage(index);
                    mailPage.updateMailTable();
                });
                paginationBox.getChildren().add(pageButton);
            }
        }

        // "다음" 버튼
        Button nextButton = new Button("다음");
        nextButton.setOnAction(event -> {
            if (mailPage.getCurrentPage() < totalPages - 1) {
                mailPage.setCurrentPage(mailPage.getCurrentPage() + 1);
                mailPage.updateMailTable();
            }
        });
        paginationBox.getChildren().add(nextButton);

        // "끝" 버튼
        Button lastButton = new Button("끝");
        lastButton.setOnAction(event -> {
            mailPage.setCurrentPage(totalPages - 1);
            mailPage.updateMailTable();
        });
        paginationBox.getChildren().add(lastButton);
    }

    public void setTotalPages(int total) {
        this.totalPages = total; // 총 페이지 수 설정
        updatePagination(); // 페이지 버튼 업데이트
    }
}
