# 백엔드 API 명세

기준일: 2026-05-10  
Base URL: `http://localhost:8080`  
응답 형태: 기본적으로 `ResponseEntity.ok(data)` 또는 `201 Created + data`, 삭제는 `204 No Content`

## 1. 웹 관리자

| 기능 | Method | URL | Request | Response |
|---|---:|---|---|---|
| 웹 관리자 로그인 | POST | `/api/web-admin/login` | `{ "wId": "admin", "wPwd": "1234" }` | `{ "managerNo": 1, "wId": "admin" }` |

## 2. 아파트 관리자 회원가입/로그인

| 기능 | Method | URL | Request | Response |
|---|---:|---|---|---|
| 회원가입 신청 | POST | `/api/apartment-managers` | `{ "loginId", "password", "email", "phone", "name", "apartmentName", "address", "detailAddress", "careerImage" }` | `SignDto` |
| 전체/아파트별 조회 | GET | `/api/apartment-managers?apartmentNo=1` | 없음 | `SignDto[]` |
| 상세 조회 | GET | `/api/apartment-managers/{managerNo}` | 없음 | `SignDto` |
| 수정 | PUT | `/api/apartment-managers/{managerNo}` | `SignDto` | `SignDto` |
| 삭제 | DELETE | `/api/apartment-managers/{managerNo}` | 없음 | 없음 |
| 로그인 | POST | `/api/apartment-managers/login` | `{ "loginId": "qwe123", "password": "1234" }` | `{ "managerNo", "apartmentNo", "apartmentName", "loginId", "name", "approvalStatus" }` |
| 마이페이지 | GET | `/api/apartment-managers/{managerNo}/my-page` | 없음 | `{ "managerNo", "loginId", "email", "phone", "name", "apartmentNo", "apartmentName", "address", "detailAddress", "apartmentPassword" }` |

## 3. 웹 관리자 - 아파트 관리자 승인

| 기능 | Method | URL | Request | Response |
|---|---:|---|---|---|
| 가입 신청 목록 | GET | `/api/web-admin/signup-requests` | 없음 | `ApartmentManagerSignupListDto[]` |
| 가입 신청 상세 | GET | `/api/web-admin/signup-requests/{managerNo}` | 없음 | `ApartmentManagerSignupListDto` |
| 가입 승인 | PATCH | `/api/web-admin/signup-requests/{managerNo}/approve` | 없음 | `{ "managerNo", "apartmentNo", "apartmentPassword" }` |
| 가입 거절 | PATCH | `/api/web-admin/signup-requests/{managerNo}/reject` | `{ "rejectReason": "거절 사유" }` | `ApartmentManagerSignupListDto` |

## 4. 입주민 승인

| 기능 | Method | URL | Request | Response |
|---|---:|---|---|---|
| 입주민 가입 신청 목록 | GET | `/api/resident-signup-requests?apartmentNo=1` | 없음 | `ResidentApprovalDto[]` |
| 입주민 가입 신청 상세 | GET | `/api/resident-signup-requests/{residentNo}` | 없음 | `ResidentApprovalDto` |
| 입주민 승인 | PATCH | `/api/resident-signup-requests/{residentNo}/approve` | 없음 | `ResidentApprovalDto` |
| 입주민 거절 | PATCH | `/api/resident-signup-requests/{residentNo}/reject` | `{ "rejectReason": "거절 사유" }` | `ResidentApprovalDto` |

## 5. 입주민 관리

| 기능 | Method | URL | Request | Response |
|---|---:|---|---|---|
| 입주민 목록 | GET | `/api/residents?apartmentNo=1` | 없음 | `ResidentManagementDto[]` |
| 입주민 상세 | GET | `/api/residents/{residentNo}` | 없음 | `ResidentManagementDto` |
| 입주민 등록 | POST | `/api/residents` | `{ "apartmentNo", "loginId", "password", "name", "email", "building", "unit", "phone" }` | `ResidentManagementDto` |
| 입주민 수정 | PUT | `/api/residents/{residentNo}` | `{ "name", "email", "building", "unit", "phone" }` | `ResidentManagementDto` |
| 입주민 삭제 | DELETE | `/api/residents/{residentNo}` | 없음 | 없음 |

## 6. 차량 관리

| 기능 | Method | URL | Request | Response |
|---|---:|---|---|---|
| 차량 목록 | GET | `/api/vehicles?apartmentNo=1` | 없음 | `VehicleManagementDto[]` |
| 차량 상세 | GET | `/api/vehicles/{vehicleNo}` | 없음 | `VehicleManagementDto` |
| 차량 등록 | POST | `/api/vehicles` | `{ "carNumber", "carType", "ownerId", "note" }` | `VehicleManagementDto` |
| 차량 수정 | PUT | `/api/vehicles/{vehicleNo}` | `{ "carNumber", "carType", "ownerId", "note" }` | `VehicleManagementDto` |
| 차량 삭제 | DELETE | `/api/vehicles/{vehicleNo}` | 없음 | 없음 |

## 7. 주차장 관리

| 기능 | Method | URL | Request | Response |
|---|---:|---|---|---|
| 주차장 목록 | GET | `/api/parking-lots?apartmentNo=1` | 없음 | `ParkingLotDto[]` |
| 주차장 생성 | POST | `/api/parking-lots` | `{ "apartmentNo", "name", "floor", "totalSpaces", "usedSpaces" }` | `ParkingLotDto` |
| 주차장 삭제 | DELETE | `/api/parking-lots/{parkingLotNo}` | 없음 | 없음 |

## 8. 주차칸 관리

| 기능 | Method | URL | Request | Response |
|---|---:|---|---|---|
| 주차칸 목록 | GET | `/api/parking-zones?parkingLotNo=1` | 없음 | `ParkingZoneDto[]` |
| 주차칸 생성 | POST | `/api/parking-zones` | `{ "parkingLotNo", "areaNumber", "location", "status", "layoutRow", "layoutColumn", "statusChangeReason" }` | `ParkingZoneDto` |
| 주차칸 상태 변경 | PATCH | `/api/parking-zones/{parkingZoneNo}/status` | `{ "status", "statusChangeReason" }` | `ParkingZoneDto` |
| 주차칸 배치 변경 | PATCH | `/api/parking-zones/{parkingZoneNo}/layout` | `{ "layoutRow", "layoutColumn" }` | `ParkingZoneDto` |
| 주차칸 삭제 | DELETE | `/api/parking-zones/{parkingZoneNo}` | 없음 | 없음 |

## 9. 상태값

| 값 | 의미 |
|---|---|
| `PENDING` | 승인 대기 |
| `APPROVED` | 승인 완료 |
| `REJECTED` | 승인 거절 |

## 10. 프론트 axios 주의사항

- 웹 관리자 로그인만 `wId`, `wPwd`를 사용한다.
- 아파트 관리자/입주민 쪽 로그인 아이디는 아직 `loginId`, 비밀번호는 `password`를 사용한다.
- `DELETE` API는 응답 body가 없으므로 axios에서는 성공 status만 확인하면 된다.
- 관리자 승인/거절 시 Gmail 발송이 실행되지만, 메일 발송 실패가 DB 승인 상태 저장을 막지는 않도록 처리되어 있다.
