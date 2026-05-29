# 백엔드 API 명세

기준일: 2026-05-29

Base URL:

```text
http://localhost:8080
```

현재 백엔드는 웹 관리자/아파트 관리자 API, Flutter 앱 API, Python/FastAPI 연동 API를 함께 제공합니다.

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

입주민 등록/수정 시 차량 등록 제한 값을 함께 저장할 수 있습니다.

```json
{
  "residentCarLimit": 1,
  "visitorCarLimit": 2
}
```

기본값:

```text
residentCarLimit = 1
visitorCarLimit = 2
```

## 6. 아파트 관리자 차량 관리

아파트 관리자 화면의 입주민 차량 관리 API입니다.

| 기능 | Method | URL |
|---|---:|---|
| 차량 목록 | GET | `/api/vehicles?apartmentNo=1` |
| 차량 상세 | GET | `/api/vehicles/{vehicleNo}` |
| 차량 등록 | POST | `/api/vehicles` |
| 차량 수정 | PUT | `/api/vehicles/{vehicleNo}` |
| 차량 삭제 | DELETE | `/api/vehicles/{vehicleNo}` |

입주민 차량 등록은 해당 입주민의 `residentCarLimit`을 초과하면 실패합니다.

## 7. 방문차량 관리

아파트 관리자가 앱에서 등록된 방문차량을 조회하는 API입니다.

| 기능 | Method | URL |
|---|---:|---|
| 방문차량 목록 | GET | `/api/visitor-cars?apartmentNo=1` |

조회 기준:

```text
registered_cars.u_no -> user.u_no -> user.a_no
```

## 8. 주차장 관리

| 기능 | Method | URL |
|---|---:|---|
| 주차장 목록 | GET | `/api/parking-lots?apartmentNo=1` |
| 주차장 생성 | POST | `/api/parking-lots` |
| 주차장 삭제 | DELETE | `/api/parking-lots/{parkingLotNo}` |

## 9. 주차칸 관리

| 기능 | Method | URL |
|---|---:|---|
| 주차칸 목록 | GET | `/api/parking-zones?parkingLotNo=1` |
| 주차칸 생성 | POST | `/api/parking-zones` |
| 주차칸 상태 변경 | PATCH | `/api/parking-zones/{parkingZoneNo}/status` |
| 주차칸 배치 변경 | PATCH | `/api/parking-zones/{parkingZoneNo}/layout` |
| 주차칸 삭제 | DELETE | `/api/parking-zones/{parkingZoneNo}` |

주차칸 생성/상태 변경 시 구역 종류를 함께 저장할 수 있습니다.

```json
{
  "zoneType": "normal"
}
```

구역 종류:

```text
normal       일반 주차칸
double_lane  통로 주차칸
```

## 10. 관리자 알림

| 기능 | Method | URL |
|---|---:|---|
| 관리자 알림 목록 | GET | `/api/manager-notifications` |
| 관리자 알림 읽음 처리 | PATCH | `/api/manager-notifications/{notificationNo}/read` |

주요 생성 조건:

- 앱에서 입주민 문의 작성
- Python 주차 이벤트에서 이상 주차 감지

## 11. 문의

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

관리자 화면 입주민 문의 상세는 문의에 연결된 차량을 표시합니다. 문의에 차량이 직접 연결되지 않은 경우 해당 입주민의 첫 입주민 차량을 보조로 사용합니다.

## 12. 앱 인증/계정

Flutter 앱에서 호출하는 API입니다.

| 기능 | Method | URL |
|---|---:|---|
| 아파트 목록 조회 | GET | `/api/apartments` |
| 앱 로그인 | POST | `/api/login` |
| 앱 회원가입 | POST | `/api/signup` |
| 아이디 찾기 | POST | `/api/find-id` |
| 비밀번호 재설정 | POST | `/api/reset-pw` |
| 내 정보 조회 | GET | `/api/user-info` |

## 13. 앱 차량/주차

| 기능 | Method | URL |
|---|---:|---|
| 내 차량/방문차량 조회 | GET | `/api/cars` |
| 차량/방문차량 등록 | POST | `/api/cars` |
| 차량 삭제 | DELETE | `/api/cars/{carNumber}` |
| 앱 주차구역 조회 | GET | `/api/app/parking-zones` |
| 주차 대기 신청 | POST | `/api/waitlist` |
| 방문차량 입차 처리 | POST | `/api/visitor-entry` |
| 주차 상태 업데이트 | POST | `/api/parking-update` |

앱 차량 등록 제한:

```text
입주민 차량: residentCarLimit 기준
방문차량: visitorCarLimit 기준
```

제한 초과 시 `409 CONFLICT`로 응답합니다.

## 14. 앱 문의/알림/설정

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

앱에서 `/api/inquiries`로 문의를 작성하면 `resident_inquiry` 저장 후 아파트 관리자용 `manager_notification`도 함께 생성됩니다.

## 15. Python 연동

FastAPI가 Spring Boot로 전달하는 주차/차단기 API입니다.

| 기능 | Method | URL |
|---|---:|---|
| 등록 차량 목록 조회 | GET | `/api/parking/cars` |
| 주차구역 상태 조회 | GET | `/api/parking/zone/{zoneName}` |
| 입차 저장 | POST | `/api/parking/entry` |
| 출차 저장 | POST | `/api/parking/exit` |
| 번호판 수정 | POST | `/api/parking/update-plate` |
| 차단기 차량 확인 | POST | `/api/gate/check` |
| 차단기 차량 확인 호환 URL | POST | `/api/check-plate` |
| 차단기 통과 로그 저장 | POST | `/api/gate/log` |
| 차단기 통과 로그 호환 URL | POST | `/api/entry-log` |
| 번호판 미확인 기록 조회 | GET | `/api/gate/unmatched` |
| 번호판 자동 부여 | POST | `/api/gate/assign-plate` |
| 이상 주차 알림 요청 | POST | `/api/gate/alert` |

현재 FastAPI 쪽 주의:

- FastAPI `config.py`의 차량 목록 URL이 `/api/cars`를 바라보는 부분이 있습니다.
- Spring Boot의 Python용 전체 차량 목록 API는 `/api/parking/cars`입니다.
- FastAPI의 번호판 자동 부여 요청 형식과 Spring Boot의 `/api/gate/assign-plate` 요청 형식이 다릅니다.

## 16. 상태값

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

### 주차구역 종류

| 값 | 의미 |
|---|---|
| `normal` | 일반 주차칸 |
| `double_lane` | 통로 주차칸 |

### 주차 유형

| 값 | 의미 |
|---|---|
| `normal` | 정상 주차 |
| `multi_zone` | 두 칸 이상 걸침 |
| `double_lane` | 통로 주차 |
| `aisle_block` | 통로 방해 |

## 17. 프론트/앱 호출 주의사항

- 웹 관리자 로그인은 웹 전용 API인 `/api/web-admin/login`을 사용합니다.
- 아파트 관리자 로그인은 `/api/apartment-managers/login`을 사용합니다.
- Flutter 앱 로그인은 `/api/login`을 사용합니다.
- 웹 프론트는 `web\front\src\api\axiosInstance.js`의 baseURL을 사용합니다.
- Flutter 앱은 `lib\main.dart`의 `baseUrl`을 사용합니다.
- Android 에뮬레이터에서는 `localhost` 대신 `10.0.2.2`를 사용합니다.
