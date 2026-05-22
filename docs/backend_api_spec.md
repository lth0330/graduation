# 백엔드 API 명세

기준일: 2026-05-22

Base URL:

```text
http://localhost:8080
```

현재 백엔드는 웹 관리자/아파트 관리자 API와 Flutter 앱 API를 함께 제공한다.

## 1. 웹 관리자

| 기능 | Method | URL |
|---|---:|---|
| 웹 관리자 로그인 | POST | `/api/web-admin/login` |
| 웹 관리자 대시보드 통계 | GET | `/api/web-admin/dashboard/summary` |

## 2. 아파트 관리자 회원가입/로그인

| 기능 | Method | URL |
|---|---:|---|
| 회원가입 신청 | POST | `/api/apartment-managers` |
| 전체/아파트별 조회 | GET | `/api/apartment-managers?apartmentNo=1` |
| 상세 조회 | GET | `/api/apartment-managers/{managerNo}` |
| 수정 | PUT | `/api/apartment-managers/{managerNo}` |
| 삭제 | DELETE | `/api/apartment-managers/{managerNo}` |
| 로그인 | POST | `/api/apartment-managers/login` |
| 마이페이지 | GET | `/api/apartment-managers/{managerNo}/my-page` |
| 아파트 관리자 대시보드 통계 | GET | `/api/apartment-managers/dashboard/summary` |

## 3. 웹 관리자 - 아파트 관리자 승인

| 기능 | Method | URL |
|---|---:|---|
| 가입 신청 목록 | GET | `/api/web-admin/signup-requests` |
| 가입 신청 상세 | GET | `/api/web-admin/signup-requests/{managerNo}` |
| 가입 승인 | PATCH | `/api/web-admin/signup-requests/{managerNo}/approve` |
| 가입 거절 | PATCH | `/api/web-admin/signup-requests/{managerNo}/reject` |

## 4. 입주민 승인

| 기능 | Method | URL |
|---|---:|---|
| 입주민 가입 신청 목록 | GET | `/api/resident-signup-requests?apartmentNo=1` |
| 입주민 가입 신청 상세 | GET | `/api/resident-signup-requests/{residentNo}` |
| 입주민 승인 | PATCH | `/api/resident-signup-requests/{residentNo}/approve` |
| 입주민 거절 | PATCH | `/api/resident-signup-requests/{residentNo}/reject` |

## 5. 입주민 관리

| 기능 | Method | URL |
|---|---:|---|
| 입주민 목록 | GET | `/api/residents?apartmentNo=1` |
| 입주민 상세 | GET | `/api/residents/{residentNo}` |
| 입주민 등록 | POST | `/api/residents` |
| 입주민 수정 | PUT | `/api/residents/{residentNo}` |
| 입주민 삭제 | DELETE | `/api/residents/{residentNo}` |

## 6. 아파트 관리자 차량 관리

아파트 관리자 화면의 입주민 차량 관리 API다.

| 기능 | Method | URL |
|---|---:|---|
| 차량 목록 | GET | `/api/vehicles?apartmentNo=1` |
| 차량 상세 | GET | `/api/vehicles/{vehicleNo}` |
| 차량 등록 | POST | `/api/vehicles` |
| 차량 수정 | PUT | `/api/vehicles/{vehicleNo}` |
| 차량 삭제 | DELETE | `/api/vehicles/{vehicleNo}` |

## 7. 주차장 관리

| 기능 | Method | URL |
|---|---:|---|
| 주차장 목록 | GET | `/api/parking-lots?apartmentNo=1` |
| 주차장 생성 | POST | `/api/parking-lots` |
| 주차장 삭제 | DELETE | `/api/parking-lots/{parkingLotNo}` |

## 8. 주차칸 관리

| 기능 | Method | URL |
|---|---:|---|
| 주차칸 목록 | GET | `/api/parking-zones?parkingLotNo=1` |
| 주차칸 생성 | POST | `/api/parking-zones` |
| 주차칸 상태 변경 | PATCH | `/api/parking-zones/{parkingZoneNo}/status` |
| 주차칸 배치 변경 | PATCH | `/api/parking-zones/{parkingZoneNo}/layout` |
| 주차칸 삭제 | DELETE | `/api/parking-zones/{parkingZoneNo}` |

## 9. 문의

| 기능 | Method | URL |
|---|---:|---|
| 아파트 관리자 문의 작성 | POST | `/api/manager-inquiries` |
| 내 관리자 문의 조회 | GET | `/api/manager-inquiries/my` |
| 웹 관리자 문의 목록 | GET | `/api/web-admin/inquiries` |
| 웹 관리자 문의 상세 | GET | `/api/web-admin/inquiries/{inquiryNo}` |
| 웹 관리자 문의 답변 | PATCH | `/api/web-admin/inquiries/{inquiryNo}/answer` |
| 입주민 문의 작성 | POST | `/api/resident-inquiries` |
| 입주민 문의 목록 | GET | `/api/resident-inquiries` |
| 입주민 문의 상세 | GET | `/api/resident-inquiries/{inquiryNo}` |
| 입주민 문의 답변 | PATCH | `/api/resident-inquiries/{inquiryNo}/answer` |

## 10. 앱 인증/계정

Flutter 앱에서 호출하는 API다.

| 기능 | Method | URL |
|---|---:|---|
| 아파트 목록 조회 | GET | `/api/apartments` |
| 앱 로그인 | POST | `/api/login` |
| 앱 회원가입 | POST | `/api/signup` |
| 아이디 찾기 | POST | `/api/find-id` |
| 비밀번호 재설정 | POST | `/api/reset-pw` |
| 내 정보 조회 | GET | `/api/user-info` |

## 11. 앱 차량/주차

| 기능 | Method | URL |
|---|---:|---|
| 내 차량/방문차량 조회 | GET | `/api/cars` |
| 차량/방문차량 등록 | POST | `/api/cars` |
| 차량 삭제 | DELETE | `/api/cars/{carNumber}` |
| 앱 주차구역 조회 | GET | `/api/app/parking-zones` |
| 주차 대기 신청 | POST | `/api/waitlist` |
| 방문차량 입차 처리 | POST | `/api/visitor-entry` |
| 주차 상태 업데이트 | POST | `/api/parking-update` |

## 12. 앱 문의/알림/설정

| 기능 | Method | URL |
|---|---:|---|
| 내 문의 목록 | GET | `/api/inquiries` |
| 문의 작성 | POST | `/api/inquiries` |
| 알림 목록 | GET | `/api/notifications` |
| 알림 읽음 처리 | PATCH | `/api/notifications/{notificationNo}/read` |
| FCM 토큰 저장 | POST | `/api/device-token` |
| FCM 토큰 삭제 | DELETE | `/api/device-token` |
| 푸시 설정 변경 | PATCH | `/api/settings/push` |
| 테마 설정 변경 | PATCH | `/api/settings/theme` |

## 13. 상태값

### 승인 상태

| 값 | 의미 |
|---|---|
| `PENDING` | 승인 대기 |
| `APPROVED` | 승인 완료 |
| `REJECTED` | 승인 거절 |

### 주차구역 상태

| 값 | 의미 |
|---|---|
| `empty` | 비어 있음 |
| `occupied` | 사용 중 |
| `disabled` | 사용 불가 |

## 14. Python 연동 주의

Python FastAPI 서버는 현재 다음 Spring Boot API가 있다고 가정한다.

```text
/api/parking/zone
/api/parking/entry
/api/parking/exit
/api/parking/update-plate
/api/gate/check
/api/gate/log
/api/gate/unmatched
/api/gate/assign-plate
/api/gate/alert
```

하지만 현재 Spring Boot Controller에는 위 API가 아직 모두 구현되어 있지 않다.

현재 Spring Boot에 있는 외부 연동용 API는 다음에 가깝다.

```text
POST /api/visitor-entry
POST /api/parking-update
GET /api/app/parking-zones
GET /api/cars
```

따라서 Python 연동을 완성하려면 FastAPI의 기대 URL, 요청 JSON, 응답 JSON을 Spring Boot 실제 API와 맞춰야 한다.

## 15. 프론트/앱 호출 주의사항

- 웹 관리자 로그인은 웹 전용 API인 `/api/web-admin/login`을 사용한다.
- 아파트 관리자 로그인은 `/api/apartment-managers/login`을 사용한다.
- Flutter 앱 로그인은 `/api/login`을 사용한다.
- 웹 프론트는 `web\front\src\api\axiosInstance.js`의 baseURL을 사용한다.
- Flutter 앱은 `lib\main.dart`의 `baseUrl`을 사용한다.
- Android 에뮬레이터에서는 `localhost` 대신 `10.0.2.2`를 사용한다.
