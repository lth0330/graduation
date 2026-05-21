# Graduation Web Backend

아파트 웹 관리 기능을 위한 Spring Boot 백엔드 프로젝트입니다.  
현재 구현 범위는 구글 시트의 `웹DB` 내용을 기준으로 한 아파트, 웹 관리자, 아파트 관리자 데이터 구조와 아파트 관리자 회원가입/정보 CRUD입니다.

## 개발 환경

- Java 17
- Spring Boot 4.0.3
- Gradle
- MySQL
- Spring Data JPA
- Lombok

## Java 설정

현재 로컬에서는 Java 17 경로를 아래 값으로 사용합니다.

```powershell
JAVA_HOME=C:\Users\xm300\.jdks\corretto-17.0.19
```

새 터미널에서 확인:

```powershell
java -version
```

## DB 기준

구글 시트 `웹DB` 기준으로 다음 테이블을 사용합니다.

### apartments

아파트 기본 정보입니다.

| 컬럼 | 설명 |
| --- | --- |
| `a_no` | 아파트 번호, PK, AUTO_INCREMENT |
| `a_name` | 아파트명, UNIQUE, NOT NULL |
| `a_pwd` | 아파트 비밀번호, UNIQUE, NOT NULL |

### web_manager

웹 관리자 계정 정보입니다.

| 컬럼 | 설명 |
| --- | --- |
| `w_no` | 웹 관리자 번호, PK, AUTO_INCREMENT |
| `id` | 웹 관리자 아이디, UNIQUE |
| `pwd` | 웹 관리자 비밀번호, UNIQUE, NOT NULL |

### apartment_manager

아파트 관리자 회원가입 정보입니다.

| 컬럼 | 설명 |
| --- | --- |
| `m_no` | 아파트 관리자 번호, PK, AUTO_INCREMENT |
| `a_no` | 아파트 번호, `apartments.a_no` 참조 |
| `m_id` | 아이디, UNIQUE, NOT NULL |
| `m_pwd` | 비밀번호, NOT NULL |
| `m_email` | 이메일, NOT NULL |
| `m_phone` | 연락처 |
| `m_address` | 주소 |
| `m_name` | 관리자명 |
| `picture` | 재직증명서 사진 경로/파일명, NOT NULL |

## 샘플 데이터

샘플 데이터는 아래 파일에 있습니다.

```text
src/main/resources/sql/data.sql
```

포함된 샘플:

- 아파트 3개: 명학 아파트, 성결 아파트, 안양 아파트
- 웹 관리자 1개: `admin / admin1234`
- 아파트 관리자 3명

`application.properties`에서 SQL 초기화를 사용할 경우 아래 설정을 확인합니다.

```properties
spring.sql.init.data-locations=classpath:/sql/data.sql
spring.jpa.defer-datasource-initialization=true
```

현재 AWS DB 기준 설정은 `spring.jpa.hibernate.ddl-auto=update`입니다. 서버 시작 시 기존 테이블을 삭제하지 않고 Entity 변경 사항을 반영합니다.

## 구현된 주요 파일

### Entity

- `web.aptManager.entity.ApartmentEntity`
- `web.aptManager.entity.ApartmentManagerEntity`
- `web.webAdmin.entity.WebManagerEntity`

각 Entity는 이전 프로젝트 스타일에 맞춰 Lombok과 JPA를 사용하고, DTO 변환용 `toDTO()` 메서드를 포함합니다.

### DTO

- `web.aptManager.dto.ApartmentInfoDto`
- `web.aptManager.dto.SignDto`
- `web.webAdmin.dto.WebManagerDto`
- `web.webAdmin.dto.ApartmentManagerSignupListDto`
- `web.webAdmin.dto.ApartmentManagerApprovalDto`

DTO는 필요한 경우 Entity 변환용 `toEntity()` 메서드를 포함합니다.

### Repository

- `web.aptManager.repository.ApartmentRepository`
- `web.aptManager.repository.ApartmentManagerRepository`
- `web.webAdmin.repository.WebManagerRepository`

Spring Data JPA의 `JpaRepository`를 사용합니다.

### 아파트 관리자 CRUD

- Controller: `web.aptManager.controller.SignController`
- Service: `web.aptManager.service.SignService`

## 아파트 관리자 API

기본 URL:

```http
/api/apartment-managers
```

### 회원가입

```http
POST /api/apartment-managers
```

요청 예시:

```json
{
  "apartmentNo": 1,
  "loginId": "qwe123",
  "password": "qwer1234",
  "email": "sample1@naver.com",
  "phone": "1012345678",
  "address": "방울내로37(j-sky 아파트)",
  "name": "아무개",
  "picture": "sample"
}
```

처리 내용:

- 아파트 번호 존재 여부 확인
- 아이디 중복 확인
- 이메일 중복 확인
- 필수값 확인
- 아파트 관리자 저장

### 전체 조회

```http
GET /api/apartment-managers
```

### 아파트별 조회

```http
GET /api/apartment-managers?apartmentNo=1
```

### 단건 조회

```http
GET /api/apartment-managers/{managerNo}
```

예시:

```http
GET /api/apartment-managers/1
```

### 수정

```http
PUT /api/apartment-managers/{managerNo}
```

요청 예시:

```json
{
  "email": "new-email@naver.com",
  "phone": "01012345678",
  "address": "수정된 주소",
  "name": "수정된 이름",
  "picture": "new-sample"
}
```

수정 요청은 전달된 값만 변경합니다.

### 삭제

```http
DELETE /api/apartment-managers/{managerNo}
```

예시:

```http
DELETE /api/apartment-managers/1
```

## 실행 및 컴파일 확인

컴파일:

```powershell
.\gradlew.bat --no-daemon compileJava
```

현재까지 확인 결과:

```text
BUILD SUCCESSFUL
```

서버 실행:

```powershell
.\gradlew.bat bootRun
```

## 현재 구현 범위

완료:

- 구글 시트 웹DB 기준 Entity 작성
- Repository 작성
- DTO 작성
- 아파트 관리자 회원가입 API
- 아파트 관리자 전체 조회 API
- 아파트 관리자 단건 조회 API
- 아파트별 관리자 조회 API
- 아파트 관리자 수정 API
- 아파트 관리자 삭제 API
- `data.sql` 샘플 데이터 작성

아직 미구현:

- 로그인/JWT 인증 연동
- 웹 관리자 승인/거절 기능
- 실제 파일 업로드 방식의 재직증명서 처리
- 주민/차량/주차장 관련 CRUD
