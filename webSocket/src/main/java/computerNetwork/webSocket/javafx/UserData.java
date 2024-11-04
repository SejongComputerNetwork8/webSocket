package computerNetwork.webSocket.javafx;

public class UserData {
    private String email; // 사용자 이메일 주소
    private String loginInfo; // 로그인 정보 (예: 비밀번호 등)

    public UserData(String email, String loginInfo) {
        this.email = email;
        this.loginInfo = loginInfo;
    }

    public String getEmail() {
        return email; // 이메일 주소 반환
    }

    public String getLoginInfo() {
        return loginInfo; // 로그인 정보 반환
    }
}
