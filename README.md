# SMTP,IMAP을 활용한 이메일 통합 시스템 + 네이버 메일 기능 추가 버전(ui 및 애플리케이션은 따로 구현됨)
## 팀원

| [문한결](https://github.com/Munhangyeol)         | 조성준                          | [채승현](https://github.com/cooky122)       | [김선웅](https://github.com/kolom1234)      | [김민재](https://github.com/min1231)       | [김민우](https://github.com/minuda1225)     |
|--------------------------------------------------|---------------------------------|---------------------------------------------|---------------------------------------------|---------------------------------------------|---------------------------------------------|
| <div align="center"><img src="https://github.com/Munhangyeol.png" width="100"></div> | <div align="center">-</div> | <div align="center"><img src="https://github.com/cooky122.png" width="100"></div> | <div align="center"><img src="https://github.com/kolom1234.png" width="100"></div> | <div align="center"><img src="https://github.com/min1231.png" width="100"></div> | <div align="center"><img src="https://github.com/minuda1225.png" width="100"></div> |
| **Team Leader**<br>**Gmail Backend**                 | **Gmail Backend**               | **NaverMail Backend**                       | **NaverMail Backend**                       | **JavaFX Frontend**                        | **JavaFX Frontend**                        |








## 개요
> 이 시스템은 사용자가 이메일을 보내고, 메일함을 열어 상위 메일을 확인할 수 있게 합니다. Google 이나 Naver플랫폼을 통해 **SMTP,IMAP**을 사용하여 통합된 이메일 기능을 제공합니다.

## 목표
```Application layer```의 ```Socket```을 이용해서 programming하면서, SMTP,IMAP의 규격을 익히고, TSL,SSL의 의미에 대해서 생각할 수 있습니다. 

## 사용자 시나리오
1. 사용자는 메일을 보낼지, 메일함을 열지를 선택합니다.
2. 이메일을 보내고 싶은 플랫폼 (Google 혹은 Naver)을 선택합니다.
3. 사용자가 아이디와 비밀번호를 입력합니다.
4. Google의 경우 앱 비밀번호를 이용해야 합니다.
5. 메일 발송 흐름
    - 사용자가 받는 사람의 이메일 주소를 입력합니다.
    - 이메일 내용을 작성하고 발송합니다.
    
6. 메일함 열기 흐름
    -  메일함을 열면 상위 10개의 메일을 확인할 수 있습니다.
  
## 개선하면 좋을 사항
1. 현재는 상위 10개의 메일이라고 했으나, 이를 사용자가 원하는 정보가 담긴 메일을 반환하는 것으로 바꾸면 더 유저친화적일듯

## 메인 로직
뷰는 javafx를 통해서 구성
### 로그인 뷰
<img width="450" alt="image" src="https://github.com/user-attachments/assets/1408d847-0f17-42eb-91b9-3221e2e43b06">

### 메일함 뷰
<img width="602" alt="image" src="https://github.com/user-attachments/assets/1fdf61ca-e4ec-414a-8d18-60429f87215a">

- 로그인 후 메일함의 제목, 보낸 사람, 날짜를 최근 10개를 확인 가능함
- 검색을 통해서 원하는 메일을 서치 가능

### 메일 보내기 뷰
<img width="299" alt="image" src="https://github.com/user-attachments/assets/ad8b1134-227e-4f71-ac3b-19d99d1c82ff">

### 결과
<img width="560" alt="image" src="https://github.com/user-attachments/assets/a09a4ad8-8c0a-4266-96a3-013a1a80339a">




