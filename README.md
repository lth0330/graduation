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
- AWS RDS 스키마 참고: `src\main\java\aws_db.sql`
- 초기 데이터 참고: `src\main\resources\sql\data.sql`
- API 명세: `docs\backend_api_spec.md`

## 상세 문서

백엔드 구조와 전체 연동 흐름은 루트 문서를 확인합니다.

```text
C:\Users\xm300\Desktop\졸업작품\웹_백엔드_구조.md
C:\Users\xm300\Desktop\졸업작품\웹_연동_작업_지침.md
C:\Users\xm300\Desktop\졸업작품\DB_테이블_정리.md
```

## 주의

`application.properties`에는 DB 비밀번호, Gmail 앱 비밀번호, JWT secret이 포함되어 있습니다. 공개 저장소 업로드 전 반드시 환경변수로 분리해야 합니다.
