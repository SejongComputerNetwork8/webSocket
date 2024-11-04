package computerNetwork.webSocket.javafx;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MailTable {

    private TableView<Mail> tableView;

    public MailTable() {
        tableView = new TableView<>();

        // 보낸이 열
        TableColumn<Mail, String> senderColumn = new TableColumn<>("보낸 사람");
        senderColumn.setCellValueFactory(new PropertyValueFactory<>("sender"));
        senderColumn.setPrefWidth(155);

        // 받는 사람 열
        TableColumn<Mail, String> dateColumn = new TableColumn<>("전송 날짜");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setPrefWidth(155);

        // 제목 열
        TableColumn<Mail, String> subjectColumn = new TableColumn<>("제목");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        subjectColumn.setPrefWidth(370);

        // 테이블에 열 추가
        tableView.getColumns().addAll(senderColumn, dateColumn, subjectColumn);
    }

    public void setMailItems(ObservableList<Mail> mailItems) {
        tableView.setItems(mailItems); // 메일 리스트를 테이블에 설정
    }
    public Mail getSelectedMail() {
        return tableView.getSelectionModel().getSelectedItem(); // 선택된 메일 반환
    }

    public TableView<Mail> getTableView() {
        return tableView; // 테이블 뷰 반환
    }
}
