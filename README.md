# Graduation Backend

아파트 주차 관리 시스템의 Spring Boot 백엔드입니다.

## 위치

```text
C:\Users\xm300\Desktop\졸업작품\web\graduation
```

## 실행

```powershell
.\gradlew.bat bootRun
```

컴파일 확인:

```powershell
.\gradlew.bat --no-daemon compileJava
```

## 주요 설정

- 서버/DB/JWT/메일 설정: `src\main\resources\application.properties`
- 로컬 비밀 설정: `src\main\resources\application-secret.properties`
- Firebase Admin SDK 서비스 계정 키: `src\main\resources\firebase-key.json`
- AWS RDS 스키마 참고: `src\main\java\aws_db.sql`
- 초기 데이터 참고: `src\main\resources\sql\data.sql`
- API 명세: `docs\backend_api_spec.md`

## 현재 핵심 기능

- 웹 관리자/아파트 관리자/입주민 앱 API 통합 제공
- 세대 기준 입주민 차량 등록 가능 대수 관리
- 입주민별 방문차량 등록 가능 대수 관리
- 관리자 웹 알림: 입주민 가입 신청, 입주민 문의, 이상 주차, OCR 실패 알림
- Python/FastAPI 주차 이벤트 연동 API
- 차단기 정책 API: 방문차량 80%/만차 차단, 관리자 상시개방
- 입차 스냅샷 `image_path` 저장
- 방문차량 조회 API
- FCM 토큰 저장/삭제, 테스트 푸시 API

## 상세 문서

백엔드 구조, 기능 설명, 전체 연동 흐름은 루트 문서를 확인합니다.

```text
C:\Users\xm300\Desktop\졸업작품\웹_백엔드_구조.md
C:\Users\xm300\Desktop\졸업작품\웹_기능_정리_팀원용.md
C:\Users\xm300\Desktop\졸업작품\웹_연동_작업_지침.md
C:\Users\xm300\Desktop\졸업작품\DB_테이블_정리.md
```

## 주의

`application.properties`는 `application-secret.properties`를 import해서 DB/메일/JWT 비밀값을 placeholder로 참조합니다.

`application-secret.properties`와 `firebase-key.json`에는 민감 정보가 포함되어 있으므로 Git에 올리지 않습니다. 이미 원격 저장소에 올라간 키는 삭제 커밋만으로 안전해지지 않으므로 재발급해야 합니다.

현재 설정은 `spring.sql.init.mode=never`라서 서버 시작 시 `data.sql`은 자동 실행되지 않습니다. `spring.jpa.hibernate.ddl-auto=update`는 유지되어 엔티티 변경에 따른 스키마 갱신은 발생할 수 있습니다.
