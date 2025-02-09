# 📧 SMTP & IMAP을 활용한 이메일 통합 시스템 + 네이버 메일 기능 추가 버전

## 👥 팀원

| [문한결](https://github.com/Munhangyeol)         | 조성준                          | [채승현](https://github.com/cooky122)       | [김선웅](https://github.com/kolom1234)      | [김민재](https://github.com/min1231)       | [김민우](https://github.com/minuda1225)     |
|--------------------------------------------------|---------------------------------|---------------------------------------------|---------------------------------------------|---------------------------------------------|---------------------------------------------|
| <div align="center"><img src="https://github.com/Munhangyeol.png" width="100"></div> | <div align="center">-</div> | <div align="center"><img src="https://github.com/cooky122.png" width="100"></div> | <div align="center"><img src="https://github.com/kolom1234.png" width="100"></div> | <div align="center"><img src="https://github.com/min1231.png" width="100"></div> | <div align="center"><img src="https://github.com/minuda1225.png" width="100"></div> |
| **Team Leader**<br>**Gmail Backend**                 | **Gmail Backend**               | **NaverMail Backend**                       | **NaverMail Backend**                       | **JavaFX Frontend**                        | **JavaFX Frontend**                        |

## 📝 개요
> 이 시스템은 사용자가 이메일을 보내고, 메일함을 열어 상위 메일을 확인할 수 있게 합니다. Google이나 Naver 플랫폼을 통해 **SMTP**와 **IMAP**을 사용하여 통합된 이메일 기능을 제공합니다.

## 🎯 목표
`Application layer`의 `Socket`을 이용해 프로그래밍하며, **SMTP**와 **IMAP**의 규격을 이해하고, **TLS**와 **SSL**의 의미에 대해 학습합니다. 

## 👣 사용자 시나리오
1.  사용자는 메일을 보낼지, 메일함을 열지를 선택합니다.
2.  이메일을 보내고 싶은 플랫폼 (Google 혹은 Naver)을 선택합니다.
3.  사용자가 아이디와 비밀번호를 입력합니다.
4.  Google의 경우 앱 비밀번호를 이용해야 합니다.
5.  메일 발송 흐름
    - 📨 사용자가 받는 사람의 이메일 주소를 입력합니다.
    - 📝 이메일 내용을 작성하고 발송합니다.
6.  메일함 열기 흐름
    - 📑 메일함을 열면 상위 10개의 메일을 확인할 수 있습니다.

## 🚀 개선하면 좋을 사항
1.  현재는 상위 10개의 메일만 보여주지만, 사용자가 원하는 조건에 맞는 메일을 검색하거나 필터링하는 기능을 추가하면 더 유용할 것입니다.

## 🛠 메인 로직
화면은 **JavaFX**를 통해 구성되어 있습니다.

### 🔑 로그인 뷰
<img width="450" alt="로그인 화면" src="https://github.com/user-attachments/assets/1408d847-0f17-42eb-91b9-3221e2e43b06">

### 📬 메일함 뷰
<img width="602" alt="메일함 화면" src="https://github.com/user-attachments/assets/1fdf61ca-e4ec-414a-8d18-60429f87215a">

- 로그인 후 메일함의 제목, 보낸 사람, 날짜를 최근 10개 확인 가능
-  검색 기능을 통해 원하는 메일 검색 가능

### 📨 메일 보내기 뷰
<img width="299" alt="메일 보내기 화면" src="https://github.com/user-attachments/assets/ad8b1134-227e-4f71-ac3b-19d99d1c82ff">

### ✅ 결과
<img width="560" alt="결과 화면" src="https://github.com/user-attachments/assets/a09a4ad8-8c0a-4266-96a3-013a1a80339a">

## 📁 파일 구성

### 📂 Backend Directory Structure
```plaintext
.
└── webSocket
    ├── src
    │   ├── main
    │   │   ├── java
    │   │   │   └── computerNetwork
    │   │   │       └── webSocket
    │   │   │           ├── gmail
    │   │   │           │   ├── GmailFetcher.java
    │   │   │           │   └── GmailSender.java
    │   │   │           └── dto
    │   │   └── resources
    │   │       └── application.properties
    │   └── test
    │       └── java
    │           └── computerNetwork
    │               └── webSocket
```

## 💻 코드 예시

### 📥 Gmail 이메일 가져오기 (GmailFetcher)
```java
GmailFetcher fetcher = new GmailFetcher();
List<FetchingInformation> emails = fetcher.fetch("your-email@gmail.com", "your-app-password");
for (FetchingInformation email : emails) {
    System.out.println("From: " + email.getFrom());
    System.out.println("Date: " + email.getDate());
    System.out.println("Subject: " + email.getSubject());
}
```

### 📤 Gmail 이메일 보내기 (GmailSender)
```java
GmailSender sender = new GmailSender();
boolean isSent = sender.sendEmail("your-email@gmail.com", "your-app-password", "recipient@example.com", "Test Subject", "This is a test email.");
if (isSent) {
    System.out.println("이메일 전송 성공!");
} else {
    System.out.println("이메일 전송 실패!");
}
```

### 📡 IMAP 명령어 전송 및 응답 파싱
```java
private void sendCommand(String command) throws IOException {
    outToServer.writeBytes(command + "\r\n");
    System.out.println("Sent: " + command);
}

private void parseAndDisplayEmails(String response) {
    String[] entries = response.split("\\* \\d+ FETCH");
    for (String entry : entries) {
        if (entry.trim().isEmpty()) continue;
        String from = extractHeaderValue(entry, "From: ");
        String subject = extractHeaderValue(entry, "Subject: ");
        String date = extractHeaderValue(entry, "Date: ");
        System.out.printf("From: %s\nSubject: %s\nDate: %s\n", from, subject, date);
    }
}

private String extractHeaderValue(String entry, String headerName) {
    int startIndex = entry.indexOf(headerName);
    if (startIndex == -1) return "";
    int endIndex = entry.indexOf("\n", startIndex);
    if (endIndex == -1) endIndex = entry.length();
    return entry.substring(startIndex + headerName.length(), endIndex).trim();
}
```

## 🛠 트러블슈팅

### 1️⃣ Gmail 로그인 실패 (IMAP 인증 오류)
- **문제**: 올바른 이메일과 비밀번호를 입력했지만 로그인 실패.
- **해결 방법**: Gmail의 경우 일반 비밀번호 대신 **앱 비밀번호**를 사용해야 하며, IMAP 설정이 Gmail 계정에서 활성화되어 있는지 확인.

### 2️⃣ 메일 수신 시 한글 깨짐 현상
- **문제**: IMAP으로 메일을 수신할 때 한글이 깨지는 문제가 발생.
- **해결 방법**: `InputStreamReader`에서 `StandardCharsets.UTF_8`을 사용하여 인코딩 문제 해결.

### 3️⃣ 메일 발송 시 SMTP 연결 타임아웃
- **문제**: 메일 전송 시 서버 연결 타임아웃 발생.
- **해결 방법**: SMTP 포트(587)와 TLS 설정이 올바르게 적용되었는지 확인하고, 방화벽 설정에서 해당 포트가 차단되지 않았는지 확인.

## 🔧 사용 기술

- **SMTP**: 이메일 발송을 위한 프로토콜.
- **IMAP**: 메일함을 관리하고 메일을 가져오는 프로토콜.
- **WebSocket**: 실시간 통신을 위한 프로토콜.
- **JavaFX**: UI 구성을 위한 Java 기반 프레임워크.

---

이 프로젝트는 **SMTP**, **IMAP**, 그리고 **WebSocket**을 활용한 이메일 통합 시스템입니다.

