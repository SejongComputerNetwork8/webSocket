import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import java.util.Properties;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NaverMailbox implements AutoCloseable {
    private static final String IMAP_HOST = "imap.naver.com";
    private static final int IMAP_PORT = 993;
    private Store store;
    private Folder inbox;

    public void checkEmails(String email, String password) throws MessagingException {
        try {
            connect(email, password);
            displayRecentEmails();
        } catch (MessagingException e) {
            throw new MessagingException("메일함 접근 실패: " + e.getMessage(), e);
        } finally {
            close();
        }
    }

    private void connect(String email, String password) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", IMAP_HOST);
        props.put("mail.imaps.port", IMAP_PORT);
        props.put("mail.imaps.ssl.enable", "true");
        props.put("mail.imaps.timeout", "10000");

        Session session = Session.getInstance(props);
        store = session.getStore("imaps");
        store.connect(IMAP_HOST, email, password);

        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
    }

    private void displayRecentEmails() throws MessagingException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int messageCount = inbox.getMessageCount();
        int start = Math.max(1, messageCount - 9);

        System.out.println("\n=== 받은 메일함 (" + messageCount + "개 중 최근 10개) ===");

        if (messageCount == 0) {
            System.out.println("메일함이 비어있습니다.");
            return;
        }

        Message[] messages = inbox.getMessages(start, messageCount);
        for (int i = messages.length - 1; i >= 0; i--) {
            Message msg = messages[i];
            // 발신자 정보 디코딩
            String from = null;
            try {
                if (msg.getFrom()[0] instanceof InternetAddress) {
                    InternetAddress address = (InternetAddress) msg.getFrom()[0];
                    String personal = address.getPersonal();
                    if (personal != null) {
                        // Base64로 인코딩된 발신자 이름을 디코딩
                        from = MimeUtility.decodeText(personal) + " <" + address.getAddress() + ">";
                    } else {
                        from = address.getAddress();
                    }
                } else {
                    from = MimeUtility.decodeText(msg.getFrom()[0].toString());
                }
            } catch (Exception e) {
                from = "알 수 없는 발신자";
            }

            String subject = null;
            try {
                subject = MimeUtility.decodeText(msg.getSubject());
            } catch (Exception e) {
                subject = "(제목을 읽을 수 없음)";
            }

            Date sentDate = msg.getSentDate();

            System.out.println("\n" + (messages.length - i) + "번째 메일");
            System.out.println("─".repeat(30));
            System.out.println("발신자: " + from);
            System.out.println("제목: " + (subject != null ? subject : "(제목 없음)"));
            System.out.println("날짜: " + (sentDate != null ? dateFormat.format(sentDate) : "알 수 없음"));
            System.out.println("─".repeat(30));
        }
    }

    @Override
    public void close() {
        try {
            if (inbox != null && inbox.isOpen()) {
                inbox.close(false);
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (MessagingException e) {
            System.err.println("메일함 닫기 실패: " + e.getMessage());
        }
    }
}
