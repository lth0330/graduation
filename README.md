# Park-On 프로젝트 포트폴리오

## 1. 프로젝트 소개

### 1.1 프로젝트 이름

- 프로젝트명: **Park-On**
- 프로젝트 유형: 아파트 주차 관리 시스템
- 주요 대상: 입주민, 아파트 관리자, 웹 최고 관리자

### 1.2 기획 의도 및 배경

Park-On은 아파트 주차장에서 발생하는 차량 등록, 방문차량 관리, 주차구역 상태 확인, 차단기 제어, 관리자 문의 처리 등을 하나의 시스템으로 관리하기 위한 졸업작품 프로젝트입니다.

기존 아파트 주차 관리는 입주민 차량, 방문차량, 주차 현황, 관리자 확인 업무가 분리되어 관리되는 경우가 많습니다. 이 프로젝트는 입주민 앱, 관리자 웹, Python 객체인식 모듈을 Spring Boot 백엔드와 MySQL DB를 기준으로 연결하여 주차 관련 데이터를 일관되게 관리하는 것을 목표로 했습니다.

주요 기능은 다음과 같습니다.

- 입주민 회원가입 및 관리자 승인
- 입주민 차량 등록 및 등록 가능 대수 제한
- 방문차량 등록 및 만료 시간 관리
- 관리자 웹 기반 주차장/주차구역 관리
- Python 객체인식 기반 주차 상태 갱신
- 차단기 개방 여부 판단
- OCR 실패 또는 이상 주차 상황에 대한 관리자 알림
- 입주민 문의 작성 및 관리자 답변
- Firebase FCM 기반 앱 알림

## 2. 팀원 / 역할

| 이름 | 역할 | 담당 내용 |
| --- | --- | --- |
| 추후 수정 | Web Frontend | React + Vite 기반 관리자 웹 화면 구현, 주차 상태 모니터링, 관리자 알림, 문의 관리 화면 |
| 추후 수정 | Backend | Spring Boot API 구현, JWT 인증/권한 처리, DB 연동, 웹/앱/Python 공용 API 설계 |
| 추후 수정 | App | Flutter 기반 입주민 앱 구현, 회원가입, 차량 등록, 방문차량 등록, 문의, 알림 화면 |
| 추후 수정 | Python / AI | YOLO, OCR, OpenCV 기반 차량/번호판 인식 및 FastAPI 연동 |

## 3. 기술 스택

### Frontend

- React
- Vite
- React Router DOM
- Axios
- CSS

### Backend

- Java 17
- Spring Boot 4.0.3
- Gradle
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Lombok
- JWT
- Firebase Admin SDK
- Gmail SMTP
- AWS SDK for Java S3

### App

- Flutter
- Dart
- HTTP 통신
- Firebase Cloud Messaging
- Flutter Secure Storage
- SQLite 기반 로컬 저장소

### Python / AI

- Python
- FastAPI
- Uvicorn
- OpenCV
- Ultralytics YOLO
- EasyOCR
- NumPy
- Requests / HTTPX

### Database / Infra

- MySQL
- AWS RDS
- Firebase Cloud Messaging
- Spring Boot 정적 파일 업로드 경로
- Git / GitHub

## 4. 서비스 아키텍처

Park-On은 Spring Boot 백엔드와 MySQL DB를 중심으로 웹, 앱, Python 객체인식 모듈이 연결되는 구조입니다.

```text
입주민 앱 Flutter
        |
        | REST API
        v
Spring Boot Backend  <--- REST API --->  관리자 웹 React
        |
        | JPA
        v
MySQL Database

Python 객체인식 모듈
        |
        | 차량/번호판 인식 이벤트
        v
FastAPI 중계 서버
        |
        | REST API
        v
Spring Boot Backend
```

전체 데이터 변경은 Spring Boot 백엔드를 통해 처리됩니다. 앱, 웹, Python 모듈이 DB를 직접 수정하지 않고 백엔드 API를 호출하도록 구성하여 인증, 권한, 차량 등록 제한, 차단기 정책, 주차 상태 변경 기준을 한 곳에서 관리합니다.

주요 데이터 흐름은 다음과 같습니다.

```text
입주민 앱 회원가입
-> Spring Boot가 승인 대기 상태로 DB 저장
-> 관리자 웹에서 가입 요청 조회
-> 아파트 관리자가 승인
-> 입주민 앱 로그인 가능
```

```text
Python 객체인식
-> 차량/번호판 인식
-> FastAPI로 이벤트 전달
-> Spring Boot 주차 API 호출
-> parking_zone 현재 상태 갱신
-> parking_history 입차/출차 이력 저장
```

```text
입구 카메라 번호판 인식
-> FastAPI가 Spring Boot /api/gate/check 호출
-> 백엔드가 등록 차량 여부와 차단기 정책 확인
-> gate_open true/false 반환
-> 차단기 장비가 개방 여부 결정
```

핵심 테이블은 다음과 같습니다.

| 테이블 | 역할 |
| --- | --- |
| `user` | 입주민 계정, 승인 상태, 차량 등록 제한 |
| `car` | 입주민 차량 |
| `registered_cars` | 방문차량 |
| `parking_lot` | 주차장 |
| `parking_zone` | 주차구역 현재 상태 |
| `parking_history` | 입차/출차 이력 |
| `manager_notification` | 관리자 웹 알림 |
| `notifications` | 입주민 앱 알림 |
| `gate_entry_log` | 차단기 통과 기록 |
| `plate_correction_review` | 번호판 보정 대기/확정 |

## 5. 시연영상 링크

- 시연 영상: 추후 추가

## 6. 참고 링크

- GitHub: 추후 추가
- Figma: 추후 추가
- 발표 자료: 추후 추가
- API 명세: `docs/backend_api_spec.md`
- 백엔드 프로젝트 위치: `web/graduation`
