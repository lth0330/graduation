# 백엔드 API 명세

기준일: 2026-06-03

Base URL:

```text
http://localhost:8080
```

현재 백엔드는 웹 관리자/아파트 관리자 API, Flutter 앱 API, Python/FastAPI 연동 API를 함께 제공합니다.

## 공통 에러 응답

서비스에서 `ResponseStatusException`으로 던진 오류는 다음 형식으로 반환됩니다.
프론트에서는 `message` 값을 사용자 안내 문구로 사용할 수 있습니다.

```json
{
  "timestamp": "2026-06-04T12:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "이미 등록된 차량번호입니다.",
  "path": "/api/vehicles"
}
```

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

승인/거절은 `PENDING` 상태의 신청만 처리할 수 있습니다. 이미 승인 또는 거절된 신청을 다시 처리하면 `409 Conflict`와 안내 메시지를 반환합니다.
거절 요청의 `rejectReason`이 비어 있으면 `400 Bad Request`를 반환합니다.

## 4. 입주민 승인

| 기능 | Method | URL |
|---|---:|---|
| 입주민 가입 신청 목록 | GET | `/api/resident-signup-requests?apartmentNo=1` |
| 입주민 가입 신청 상세 | GET | `/api/resident-signup-requests/{residentNo}` |
| 입주민 승인 | PATCH | `/api/resident-signup-requests/{residentNo}/approve` |
| 입주민 거절 | PATCH | `/api/resident-signup-requests/{residentNo}/reject` |

승인/거절은 `PENDING` 상태의 신청만 처리할 수 있습니다. 이미 승인 또는 거절된 신청을 다시 처리하면 `409 Conflict`와 안내 메시지를 반환합니다.
거절 요청의 `rejectReason`이 비어 있으면 `400 Bad Request`를 반환합니다.

## 5. 입주민 관리

| 기능 | Method | URL |
|---|---:|---|
| 입주민 목록 | GET | `/api/residents?apartmentNo=1` |
| 입주민 상세 | GET | `/api/residents/{residentNo}` |
| 입주민 등록 | POST | `/api/residents` |
| 입주민 수정 | PUT | `/api/residents/{residentNo}` |
| 입주민 삭제 | DELETE | `/api/residents/{residentNo}` |

입주민 등록/수정 시 세대 입주민 차량 등록 제한과 방문차량 등록 제한 값을 함께 저장할 수 있습니다.
입주민 차량은 개인별 카운트가 아니라 같은 아파트의 같은 동/호수 세대 기준으로 카운트합니다.
`residentCarLimit`는 `user` 테이블에 저장되지만 해당 주민이 속한 세대의 차량 제한값으로 해석합니다.
관리자가 값을 등록/수정하거나 입주민 가입 신청을 승인하면 같은 아파트/동/호 주민의 `residentCarLimit`도 같은 값으로 맞춥니다.

```json
{
  "residentCarLimit": 1,
  "visitorCarLimit": 2
}
```

기본값:

```text
residentCarLimit = 1    // 기본 1대이며, 관리자가 세대 차량 제한값으로 변경할 수 있습니다.
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

입주민 차량 등록은 같은 아파트의 같은 동/호수 기준으로 현재 차량 수가 `residentCarLimit` 이상이면 실패합니다.

차량 등록 시 `ownerId`로 소유 입주민을 지정합니다. 차량 수정 시에는 기존 소유 입주민을 유지하고 차량번호, 차종, 비고만 수정합니다. 소유 입주민을 잘못 선택해 등록한 경우에는 차량을 삭제한 뒤 올바른 입주민에게 다시 등록합니다.

차량 삭제 시 해당 차량을 참조하던 입주민 문의의 `c_no`는 null로 변경됩니다. 문의 작성자 `u_no`는 유지되므로 문의 이력은 삭제되지 않습니다.

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

`GET /api/user-info` 응답의 `user` 객체에는 아래 값이 포함됩니다.

```json
{
  "u_name": "홍길동",
  "u_dong": "101",
  "u_ho": "1001",
  "a_name": "테스트 아파트"
}
```

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
입주민 차량: 세대 기준 residentCarLimit
방문차량: visitorCarLimit 기준
```

제한 초과 시 `409 CONFLICT`로 응답합니다.

앱 주차구역 조회 응답의 `zones` 항목은 현재 아래 필드를 내려줍니다.

```json
{
  "floor": "B1",
  "type": "slot",
  "slot": "A-1",
  "status": "empty",
  "isOccupied": false,
  "current_car_number": null
}
```

`type` 값:

```text
slot   일반 주차칸
aisle  통로 주차칸
```

## 14. 앱 문의/알림/설정

| 기능 | Method | URL |
|---|---:|---|
| 내 문의 목록 | GET | `/api/inquiries` |
| 문의 작성 | POST | `/api/inquiries` |
| 알림 목록 | GET | `/api/notifications` |
| 알림 읽음 처리 | PATCH | `/api/notifications/{notificationNo}/read` |
| FCM 토큰 저장 | POST | `/api/device-token` |
| FCM 토큰 삭제 | DELETE | `/api/device-token` |
| 테스트 푸시 발송 | POST | `/api/test-push` |
| 푸시 설정 변경 | PATCH | `/api/settings/push` |
| 테마 설정 변경 | PATCH | `/api/settings/theme` |

앱에서 `/api/inquiries`로 문의를 작성하면 `resident_inquiry` 저장 후 아파트 관리자용 `manager_notification`도 함께 생성됩니다.

FCM 동작:

- 앱 로그인 성공 후 `POST /api/device-token`으로 FCM 토큰을 저장하거나 갱신합니다.
- 앱 로그아웃 시 `DELETE /api/device-token`으로 저장된 토큰을 제거합니다.
- `POST /api/test-push`는 로그인한 입주민의 기기 토큰으로 테스트 푸시를 전송합니다.
- FCM 발송 중 `UNREGISTERED`, `INVALID_ARGUMENT` 오류가 나면 해당 토큰은 `device_info`에서 삭제됩니다.

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

현재 FastAPI 연동 상태:

- FastAPI `config.py`의 차량 목록 URL은 `/api/parking/cars`를 사용합니다.
- 번호판 자동 부여는 FastAPI가 `/api/gate/unmatched`로 `history_id`를 찾은 뒤 `/api/gate/assign-plate`에 `history_id`, `plate`를 전달합니다.

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
- Flutter 앱 최신 참고본은 `app\parking2-main`이며, API 기본 주소는 `lib\main.dart`의 `baseUrl`을 사용합니다.
- Android 에뮬레이터에서는 `localhost` 대신 `10.0.2.2`를 사용합니다.

## 18. 현재 확인된 주의사항

- 최신 앱 참고본 `app\parking2-main` 기준 `parking_screen.dart`는 백엔드 앱 주차구역 응답의 `type == "aisle"` 기준으로 통로 주차칸을 구분합니다.
- 최신 앱 참고본 `app\parking2-main` 기준 `settings_screen.dart`는 `/api/user-info`의 `a_name`을 화면에 반영하고 `POST /api/test-push`를 호출합니다.
- 최신 앱 참고본 `app\parking2-main` 기준 `main.dart`에는 FCM background handler가 추가되어 있습니다.
- `POST /api/test-push`는 `RESIDENT` 권한이 필요합니다.
- 앱 입주민 차량/방문차량 등록 API는 등록 가능 대수 제한과 차량번호 중복을 함께 검사합니다.
- 최신 앱 참고본 `app\parking2-main\pubspec.yaml`에서 `http`는 런타임 의존성이므로 `dependencies` 아래에 있습니다.
- `src/main/resources/application-secret.properties`에는 DB/메일/JWT 비밀값이 들어 있고 Git에 올리지 않습니다.
- `src/main/resources/firebase-key.json`은 현재 존재합니다. 서비스 계정 키이므로 Git에 올리지 않습니다.
- 이미 원격 저장소에 올라간 키는 삭제 커밋만으로 안전해지지 않으므로 재발급해야 합니다.
