INSERT INTO apartments (a_no, a_name, a_pwd, a_address, a_detail_address)
VALUES
    (1, '서초 스마트 아파트', '1111', '서울시 서초구 반포대로 37', '관리사무소 1층'),
    (2, '그린파크 아파트', '2222', '서울시 송파구 올림픽로 88', '101동 관리사무소'),
    (3, '중앙 하이츠', '3333', '경기도 성남시 중앙로 45', '커뮤니티센터 2층'),
    (4, '스카이 빌리지', '4444', '서울시 마포구 하늘로 12', '관리동 1층'),
    (5, '미래 포레스트', '5555', '대전시 유성구 미래로 52', '정문 관리사무소');

INSERT INTO web_manager (w_no, w_id, w_pwd)
VALUES
    (1, 'admin', '$2a$10$a1cFmXtlqoCR.ZCiGRvJaOUXr7gHqWM.tBJg37oKsIABeuPkFoXWy');

INSERT INTO apartment_manager (
    m_no,
    a_no,
    m_id,
    m_pwd,
    m_email,
    m_phone,
    m_address,
    m_name,
    picture,
    approval_status,
    reject_reason,
    requested_at,
    approved_at
)
VALUES
    (1, 1, 'qwe123', '$2a$10$Kj1149qiaxyideN8RwApmOXHWrx2ky/hj9ZhTsYVr3qH5Qz3q.VCu', 'manager1@example.com', '01012345678', '서울시 서초구 반포대로 37', '김관리', 'sample-manager-1.png', 'APPROVED', NULL, '2026-04-27 09:00:00', '2026-04-27 11:30:00'),
    (2, 2, 'qwe234', '$2a$10$CsMRs2A4fEsEgS60oiEnD.qz/nkzn4MzD17w5TwmPZMaDQcqzJH7e', 'manager2@example.com', '01023456789', '서울시 송파구 올림픽로 88', '이관리', 'sample-manager-2.png', 'PENDING', NULL, '2026-04-28 10:15:00', NULL),
    (3, 3, 'qwe345', '$2a$10$yFLyresin06dL4/jXnRsn.VeLJKmgAT.5S8nq50gwgbzgvJsbNjLm', 'manager3@example.com', '01034567890', '경기도 성남시 중앙로 45', '박관리', 'sample-manager-3.png', 'REJECTED', '재직 증명 이미지 확인이 필요합니다.', '2026-04-29 14:20:00', NULL),
    (4, 4, 'skyadmin', '$2a$10$Gf98eXniRRu9WUbxRDgk4eX9PCWC521cw1rY73.4Vj/svi2ZC3oz2', 'skyadmin@example.com', '01045678901', '서울시 마포구 하늘로 12', '최하늘', 'sample-manager-4.png', 'APPROVED', NULL, '2026-05-01 09:10:00', '2026-05-01 13:20:00'),
    (5, 5, 'futuremgr', '$2a$10$Kj1149qiaxyideN8RwApmOXHWrx2ky/hj9ZhTsYVr3qH5Qz3q.VCu', 'futuremgr@example.com', '01056789012', '대전시 유성구 미래로 52', '정미래', 'sample-manager-5.png', 'PENDING', NULL, '2026-05-03 08:50:00', NULL);

INSERT INTO `user` (
    u_no,
    u_id,
    u_pwd,
    u_name,
    u_email,
    u_phone,
    p_date,
    u_dong,
    u_ho,
    a_no,
    approval_status,
    reject_reason
)
VALUES
    (1, 'minjun12', 'user1234', '김민준', 'minjun12@example.com', '01011112222', '2026-04-30 09:00:00', '101', '1203', 1, 'PENDING', NULL),
    (2, 'seoyeon', 'user1234', '이서연', 'seoyeon@example.com', '01022223333', '2026-04-29 10:20:00', '102', '804', 1, 'APPROVED', NULL),
    (3, 'doyoon', 'user1234', '박도윤', 'doyoon@example.com', '01033334444', '2026-04-28 14:10:00', '103', '1501', 1, 'REJECTED', '입주민 정보 확인이 필요합니다.'),
    (4, 'jiyoon', 'user1234', '최지윤', 'jiyoon@example.com', '01044445555', '2026-05-01 09:30:00', '201', '502', 4, 'APPROVED', NULL),
    (5, 'sumin', 'user1234', '한수민', 'sumin@example.com', '01055556666', '2026-05-03 09:25:00', '301', '903', 5, 'APPROVED', NULL);

INSERT INTO car (
    c_no,
    c_name,
    c_number,
    c_kind,
    c_note,
    c_date,
    u_no
)
VALUES
    (1, '김민준 차량', '12가 3456', '투싼', '입주 승인 대기 차량', '2026-04-30 09:05:00', 1),
    (2, '이서연 차량', '34나 7890', '카니발', '정기 등록 차량', '2026-04-29 10:25:00', 2),
    (3, '박도윤 차량', '56다 1122', 'K5', '입주 반려 차량', '2026-04-28 14:15:00', 3),
    (4, '최지윤 차량', '78라 3344', 'K7', '방문 주차 확인 예정', '2026-05-01 09:40:00', 4),
    (5, '한수민 차량', '91마 7788', '쏘나타', '장기 등록 차량', '2026-05-03 09:35:00', 5);

INSERT INTO parking_lot (
    pl_no,
    a_no,
    pl_name,
    pl_floor,
    total_spaces,
    used_spaces
)
VALUES
    (1, 1, '서초 스마트 지하주차장', 'B1', 120, 82),
    (2, 1, '서초 스마트 지하주차장', 'B2', 110, 76),
    (3, 1, '방문객 주차장', '1F', 40, 18),
    (4, 4, '스카이 빌리지 지하주차장', 'B1', 95, 41),
    (5, 5, '미래 포레스트 방문객 주차장', '1F', 45, 17);

INSERT INTO parking_zone (
    pz_no,
    pl_no,
    area_number,
    location,
    status,
    layout_row,
    layout_column,
    status_change_reason,
    current_car_number
)
VALUES
    (1, 1, 'A-B1-001', 'B1 입구 앞', 'empty', 1, 1, '초기 등록', NULL),
    (2, 1, 'A-B1-002', 'B1 중앙 구역', 'occupied', 1, 2, '입주민 사용 중', '34나 7890'),
    (3, 1, 'A-B1-003', 'B1 엘리베이터 근처', 'empty', 1, 3, '초기 등록', NULL),
    (4, 1, 'A-B1-004', 'B1 전기차 충전 구역', 'disabled', 1, 4, '충전기 점검', NULL),
    (5, 1, 'A-B1-005', 'B1 오른쪽 구역', 'occupied', 2, 1, '입주민 사용 중', '12가 3456'),
    (6, 2, 'A-B2-001', 'B2 입구 앞', 'empty', 1, 1, '초기 등록', NULL),
    (7, 2, 'A-B2-002', 'B2 중앙 구역', 'occupied', 1, 2, '입주민 사용 중', '56다 1122'),
    (8, 3, 'V-1F-001', '1F 방문객 입구', 'empty', 1, 1, '초기 등록', NULL),
    (9, 4, 'S-B1-001', '스카이 빌리지 B1 입구', 'empty', 1, 1, '초기 등록', NULL),
    (10, 5, 'F-1F-001', '미래 포레스트 1F 방문객 구역', 'occupied', 1, 1, '방문 차량 사용 중', '91마 7788');

INSERT INTO registered_cars (
    v_no,
    u_no,
    c_number,
    reg_time,
    park_time,
    expire_date
)
VALUES
    (1, 2, '22허 2026', '2026-05-08 09:00:00', NULL, NULL),
    (2, 2, '33호 3030', '2026-05-08 11:10:00', '2026-05-08 12:00:00', '2026-05-09 12:00:00'),
    (3, 4, '44무 4040', '2026-05-09 10:30:00', NULL, NULL),
    (4, 5, '55부 5050', '2026-05-10 15:20:00', '2026-05-10 16:00:00', '2026-05-11 16:00:00');

INSERT INTO notifications (
    noti_no,
    u_no,
    noti_type,
    noti_title,
    noti_message,
    is_read,
    created_at
)
VALUES
    (1, 2, 'visitor', '방문 차량 입차 알림', '[33호 3030] 방문 차량이 주차장에 들어왔습니다.', 0, '2026-05-08 12:00:00'),
    (2, 2, 'parking', '주차구역 상태 변경', 'A-B1-002 구역이 사용 중으로 변경되었습니다.', 1, '2026-05-08 13:30:00'),
    (3, 4, 'system', '문의 답변 안내', '등록하신 문의에 답변이 등록되었습니다.', 0, '2026-05-09 09:20:00'),
    (4, 5, 'visitor', '방문 차량 입차 알림', '[55부 5050] 방문 차량이 주차장에 들어왔습니다.', 0, '2026-05-10 16:00:00');

INSERT INTO device_info (
    device_id,
    u_no,
    fcm_token,
    os_type,
    last_login
)
VALUES
    ('device_2', 2, 'sample-fcm-token-2', 'android', '2026-05-08 08:30:00'),
    ('device_4', 4, 'sample-fcm-token-4', 'android', '2026-05-09 08:30:00'),
    ('device_5', 5, 'sample-fcm-token-5', 'android', '2026-05-10 08:30:00');

INSERT INTO settings (
    setting_no,
    device_id,
    alert_push,
    theme_mode
)
VALUES
    (1, 'device_2', 1, 'light'),
    (2, 'device_4', 1, 'dark'),
    (3, 'device_5', 0, 'light');

INSERT INTO waiting_list (
    wait_no,
    u_no,
    target_slot_id,
    is_notified,
    created_at
)
VALUES
    (1, 2, 'A-B1-001', 0, '2026-05-08 14:10:00'),
    (2, 4, 'ALL', 0, '2026-05-09 18:20:00'),
    (3, 5, 'F-1F-001', 1, '2026-05-10 17:30:00');

INSERT INTO manager_inquiry (
    inquiry_no,
    m_no,
    title,
    category,
    content,
    status,
    answer,
    created_at,
    answered_at
)
VALUES
    (1, 1, '주차구역 상태 변경 문의', '주차관리', '전기차 충전 구역을 점검 상태로 바꾸려면 어떤 값을 사용해야 하나요?', 'answered', '주차구역 상태는 empty, occupied, disabled 중 하나를 사용하면 됩니다.', '2026-05-04 09:10:00', '2026-05-04 10:00:00'),
    (2, 4, '입주민 승인 화면 오류 문의', '입주민관리', '입주민 승인 목록에서 일부 데이터가 늦게 표시됩니다.', 'pending', NULL, '2026-05-05 14:30:00', NULL);

INSERT INTO resident_inquiry (
    inquiry_no,
    u_no,
    c_no,
    title,
    content,
    status,
    answer,
    created_at,
    answered_at
)
VALUES
    (1, 2, 2, '차량번호 변경 요청', '차량번호가 변경되어 기존 등록 차량 정보를 수정하고 싶습니다.', 'pending', NULL, '2026-05-06 11:20:00', NULL),
    (2, 2, NULL, '방문객 주차 문의', '주말 방문객 주차 가능 시간을 알고 싶습니다.', 'answered', '방문객 주차는 관리사무소 승인 후 24시간 이용 가능합니다.', '2026-05-06 16:40:00', '2026-05-06 17:10:00'),
    (3, 4, 4, '주차 위치 확인 요청', '최근 등록한 차량의 권장 주차 위치를 확인하고 싶습니다.', 'pending', NULL, '2026-05-07 08:50:00', NULL);
