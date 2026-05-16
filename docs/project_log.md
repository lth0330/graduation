# Project Log

이 파일은 지금까지 Codex가 확인하고 수정한 내용을 한 번에 이어볼 수 있도록 정리한 기록이다.

## 프로젝트 기본 정보

- Frontend: `C:\Users\xm300\Desktop\졸업작품\front`
- Backend: `C:\Users\xm300\Desktop\졸업작품\graduation`
- Frontend 실행: `npm run dev`
- Frontend 주소: `http://localhost:5173`
- Backend 실행: `.\gradlew.bat bootRun`
- Backend 주소: `http://localhost:8080`

## 완료한 작업

### 1. 검색 및 페이지네이션 정리

- 검색 버튼이 실제로 동작하도록 연결했다.
- 목록 화면들에 페이지네이션을 적용했다.
- 관련 파일:
  - `front/src/components/common/SearchBar.jsx`
  - `front/src/components/common/Pagination.jsx`
  - `front/src/components/common/DataTable.jsx`
  - `front/src/utils/pagination.js`

### 2. 카카오 주소 검색 API 연결

- 아파트 관리자 회원가입 화면에서 주소 검색 버튼이 카카오 우편번호 API를 열도록 구현했다.
- 상세 주소 입력은 제거했다.
- 주소는 직접 입력하지 못하게 읽기 전용으로 바꿨다.
- 관련 파일:
  - `front/src/pages/auth/SignupPage.jsx`
  - `front/src/api/apartmentManagerApi.js`
  - `src/main/java/web/aptManager/service/SignService.java`

### 3. 주민 등록 기능 추가

- 아파트 관리자 화면에서 주민을 새로 등록하는 기능을 추가했다.
- 주민 등록은 앱 쪽과 연동될 예정이지만, 현재는 웹 관리자/아파트 관리자 흐름에서 먼저 등록 가능하게 만들었다.
- 관련 파일:
  - `src/main/java/web/aptManager/controller/ResidentManagementController.java`
  - `src/main/java/web/aptManager/service/ResidentManagementService.java`
  - `front/src/pages/aptManager/ResidentEdit.jsx`
  - `front/src/api/apartmentManagerApi.js`

### 4. 아파트 관리자 수정/삭제 연결

- 아파트 관리자 목록에서 수정과 삭제가 동작하도록 프론트 API를 연결했다.
- 관련 파일:
  - `front/src/api/webAdminApi.js`
  - `front/src/pages/webAdmin/ApartmentManagerManagement.jsx`

### 5. 회원가입 신청 화면 수정

- 아파트 주소는 카카오 주소 검색만 사용하게 바꿨다.
- 상세 주소 입력 필드를 제거했다.
- 회원가입 신청 오류 메시지를 정리했다.
- 관련 파일:
  - `front/src/pages/auth/SignupPage.jsx`
  - `front/src/api/apartmentManagerApi.js`
  - `src/main/java/web/aptManager/service/SignService.java`

### 6. 백엔드 의존성 정리

- 현재 사용하지 않는 의존성은 주석 처리했다.
- 주석 처리한 항목:
  - `spring-boot-starter-webflux`
  - `jackson-dataformat-xml`
  - 직접 추가한 `spring-security-crypto`
- `jjwt-api`는 실제 사용 중이므로 주석이 말리지 않도록 분리했다.
- 관련 파일:
  - `build.gradle`

## 현재 확인된 문제

### 회원가입 신청 403

- 화면에서 보이는 오류는 로그인 오류가 아니라 아파트 관리자 회원가입 신청 API 오류였다.
- 실제 요청:
  - `POST http://localhost:8080/api/apartment-managers`
- 콘솔 결과:
  - `403 (Forbidden)`
- 확인한 점:
  - `OPTIONS` 사전 요청은 정상 응답
  - 실제 `POST`만 403
  - 프론트 문제라기보다 백엔드 Security 설정 반영 문제 가능성이 높았다

### Security 설정 수정

- 공개 API 매칭을 더 명확한 방식으로 바꿨다.
- `PathPatternRequestMatcher`를 사용해서 POST 공개 경로가 확실히 매칭되도록 조정했다.
- 관련 파일:
  - `src/main/java/web/common/config/SecurityConfig.java`

## 현재 상태

- `compileJava`는 성공했다.
- 현재 백엔드 서버는 내가 띄운 테스트용 프로세스는 종료했다.
- 사용자가 직접 실행한 기존 백엔드 프로세스가 있다면 그 서버는 별도로 다시 시작해야 수정 내용이 반영된다.

## 다음에 할 일

1. 백엔드 서버를 수정된 코드로 다시 실행한다.
2. `http://localhost:5173/signup-request`에서 아파트 관리자 회원가입 신청을 다시 시도한다.
3. 만약 여전히 403이면, 실행 중인 서버가 정말 최신 빌드인지 다시 확인한다.
4. 회원가입이 정상화되면 다음 순서로 넘어간다.
   - 웹 관리자 로그인
   - 아파트 관리자 가입 승인/거절
   - 아파트 관리자 로그인
   - 주민 등록
   - 차량 등록/수정/삭제
   - 주차장 등록/삭제
   - 주차구역 등록/상태변경/배치수정/삭제
   - 문의 작성
   - 웹 관리자 문의 답변

## 메모

- 사용자가 나중에 새 기능이나 수정 요청을 주면 이 파일에 이어서 기록하면 된다.
- 파일 위치를 따로 지정하지 않으면, 이 프로젝트 안에서는 `docs/project_log.md`를 기준 메모로 사용한다.
