-- Spring Boot startup seed data.
-- 목적:
-- 1. 서버 시작 시 필요한 기본 샘플 데이터를 넣는다.
-- 2. spring.sql.init.mode=always 상태에서도 중복 PK 오류가 나지 않게 한다.
-- 3. 모든 아파트 FK(a_no)는 1번 "서초 스마트 아파트"만 참조한다.

INSERT INTO apartments (a_no, a_name, a_pwd, a_address, a_detail_address)
VALUES
    (1, '서초 스마트 아파트', '1111', '서울시 서초구 반포대로 37', '관리사무소 1층')
ON DUPLICATE KEY UPDATE
    a_name = VALUES(a_name),
    a_pwd = VALUES(a_pwd),
    a_address = VALUES(a_address),
    a_detail_address = VALUES(a_detail_address);

INSERT INTO web_manager (w_no, w_id, w_pwd)
VALUES
    (1, 'admin', '$2a$10$a1cFmXtlqoCR.ZCiGRvJaOUXr7gHqWM.tBJg37oKsIABeuPkFoXWy')
ON DUPLICATE KEY UPDATE
    w_id = VALUES(w_id),
    w_pwd = VALUES(w_pwd);

INSERT INTO apartment_manager (
    m_no, a_no, m_id, m_pwd, m_email, m_phone, m_address, m_name,
    picture, approval_status, reject_reason, requested_at, approved_at
)
VALUES
    (1, 1, 'qwe123', '$2a$10$Kj1149qiaxyideN8RwApmOXHWrx2ky/hj9ZhTsYVr3qH5Qz3q.VCu',
     'manager1@example.com', '01012345678', '서울시 서초구 반포대로 37',
     '김관리', 'sample-manager-1.png', 'APPROVED', NULL,
     '2026-04-27 09:00:00', '2026-04-27 11:30:00')
ON DUPLICATE KEY UPDATE
    a_no = VALUES(a_no),
    m_id = VALUES(m_id),
    m_pwd = VALUES(m_pwd),
    m_email = VALUES(m_email),
    m_phone = VALUES(m_phone),
    m_address = VALUES(m_address),
    m_name = VALUES(m_name),
    picture = VALUES(picture),
    approval_status = VALUES(approval_status),
    reject_reason = VALUES(reject_reason),
    requested_at = VALUES(requested_at),
    approved_at = VALUES(approved_at);

INSERT INTO `user` (
    u_no, u_id, u_pwd, u_name, u_email, u_phone, p_date,
    u_dong, u_ho, a_no, approval_status, reject_reason
)
VALUES
    (1, 'minjun12', 'user1234', '김민준', 'minjun12@example.com', '01011112222',
     '2026-04-30 09:00:00', '101', '1203', 1, 'APPROVED', NULL),
    (2, 'seoyeon', 'user1234', '이서연', 'seoyeon@example.com', '01022223333',
     '2026-04-29 10:20:00', '102', '804', 1, 'APPROVED', NULL),
    (3, 'doyoon', 'user1234', '박도윤', 'doyoon@example.com', '01033334444',
     '2026-04-28 14:10:00', '103', '1501', 1, 'PENDING', NULL)
ON DUPLICATE KEY UPDATE
    u_id = VALUES(u_id),
    u_pwd = VALUES(u_pwd),
    u_name = VALUES(u_name),
    u_email = VALUES(u_email),
    u_phone = VALUES(u_phone),
    p_date = VALUES(p_date),
    u_dong = VALUES(u_dong),
    u_ho = VALUES(u_ho),
    a_no = VALUES(a_no),
    approval_status = VALUES(approval_status),
    reject_reason = VALUES(reject_reason);

INSERT INTO car (
    c_no, c_name, c_number, c_kind, c_note, c_date, u_no
)
VALUES
    -- 차단기 테스트용 입주민 차량: POST /api/gate/check 에서 true 반환
    (1, '김민준 차량', '12가3456', '쏘나타', '기존 AWS DB에 있던 테스트 차량', '2026-04-30 09:05:00', 1),
    (2, '김민준 차량2', '789호 1234', '아반떼', '차단기 OCR 테스트 차량', '2026-05-26 09:00:00', 1),
    (3, '이서연 차량', '34나 7890', '카니발', '정기 등록 차량', '2026-04-29 10:25:00', 2)
ON DUPLICATE KEY UPDATE
    c_name = VALUES(c_name),
    c_number = VALUES(c_number),
    c_kind = VALUES(c_kind),
    c_note = VALUES(c_note),
    c_date = VALUES(c_date),
    u_no = VALUES(u_no);

INSERT INTO parking_lot (
    pl_no, a_no, pl_name, pl_floor, total_spaces, used_spaces
)
VALUES
    (1, 1, '서초 스마트 지하주차장', 'B1', 120, 82),
    (2, 1, '서초 스마트 지하주차장', 'B2', 110, 76),
    (3, 1, '서초 스마트 방문객 주차장', '1F', 40, 18)
ON DUPLICATE KEY UPDATE
    a_no = VALUES(a_no),
    pl_name = VALUES(pl_name),
    pl_floor = VALUES(pl_floor),
    total_spaces = VALUES(total_spaces),
    used_spaces = VALUES(used_spaces);

INSERT INTO parking_zone (
    pz_no, pl_no, area_number, location, status, layout_row, layout_column,
    status_change_reason, current_car_number
)
VALUES
    (1, 1, 'A-B1-001', 'B1 입구 옆', 'empty', 1, 1, '초기 등록', NULL),
    (2, 1, 'A-B1-002', 'B1 중앙 구역', 'occupied', 1, 2, '입주민 사용 중', '12가3456'),
    (3, 1, 'A-B1-003', 'B1 엘리베이터 근처', 'empty', 1, 3, '초기 등록', NULL),
    (4, 1, 'A-B1-004', 'B1 전기차 충전 구역', 'disabled', 1, 4, '충전기 점검', NULL),
    (5, 2, 'A-B2-001', 'B2 입구 옆', 'occupied', 1, 1, '입주민 사용 중', '789호 1234'),
    (6, 3, 'V-1F-001', '1F 방문객 입구', 'empty', 1, 1, '초기 등록', NULL)
ON DUPLICATE KEY UPDATE
    pl_no = VALUES(pl_no),
    area_number = VALUES(area_number),
    location = VALUES(location),
    status = VALUES(status),
    layout_row = VALUES(layout_row),
    layout_column = VALUES(layout_column),
    status_change_reason = VALUES(status_change_reason),
    current_car_number = VALUES(current_car_number);

INSERT INTO registered_cars (
    v_no, u_no, c_number, reg_time, park_time, expire_date
)
VALUES
    -- 차단기 테스트용 방문 차량: POST /api/gate/check 에서 true 반환
    (1, 2, '22허 2026', '2026-05-08 09:00:00', NULL, NULL),
    (2, 2, '33호 3030', '2026-05-08 11:10:00', '2026-05-08 12:00:00', '2026-05-09 12:00:00')
ON DUPLICATE KEY UPDATE
    u_no = VALUES(u_no),
    c_number = VALUES(c_number),
    reg_time = VALUES(reg_time),
    park_time = VALUES(park_time),
    expire_date = VALUES(expire_date);

INSERT INTO notifications (
    noti_no, u_no, noti_type, noti_title, noti_message, is_read, created_at
)
VALUES
    (1, 2, 'visitor', '방문 차량 입차 알림', '[33호 3030] 방문 차량이 주차장에 들어왔습니다.', 0, '2026-05-08 12:00:00'),
    (2, 2, 'parking', '주차구역 상태 변경', 'A-B1-002 구역이 사용 중으로 변경되었습니다.', 1, '2026-05-08 13:30:00')
ON DUPLICATE KEY UPDATE
    u_no = VALUES(u_no),
    noti_type = VALUES(noti_type),
    noti_title = VALUES(noti_title),
    noti_message = VALUES(noti_message),
    is_read = VALUES(is_read),
    created_at = VALUES(created_at);

INSERT INTO device_info (
    device_id, u_no, fcm_token, os_type, last_login
)
VALUES
    ('device_2', 2, 'sample-fcm-token-2', 'android', '2026-05-08 08:30:00'),
    ('device_3', 3, 'sample-fcm-token-3', 'android', '2026-05-09 08:30:00')
ON DUPLICATE KEY UPDATE
    u_no = VALUES(u_no),
    fcm_token = VALUES(fcm_token),
    os_type = VALUES(os_type),
    last_login = VALUES(last_login);

INSERT INTO settings (
    setting_no, device_id, alert_push, theme_mode
)
VALUES
    (1, 'device_2', 1, 'light'),
    (2, 'device_3', 1, 'dark')
ON DUPLICATE KEY UPDATE
    device_id = VALUES(device_id),
    alert_push = VALUES(alert_push),
    theme_mode = VALUES(theme_mode);

INSERT INTO waiting_list (
    wait_no, u_no, target_slot_id, is_notified, created_at
)
VALUES
    (1, 2, 'A-B1-001', 0, '2026-05-08 14:10:00'),
    (2, 3, 'ALL', 0, '2026-05-09 18:20:00')
ON DUPLICATE KEY UPDATE
    u_no = VALUES(u_no),
    target_slot_id = VALUES(target_slot_id),
    is_notified = VALUES(is_notified),
    created_at = VALUES(created_at);

INSERT INTO manager_inquiry (
    inquiry_no, m_no, title, category, content, status, answer, created_at, answered_at
)
VALUES
    (1, 1, '주차구역 상태 변경 문의', '주차관리',
     '전기차 충전 구역을 점검 상태로 바꾸려면 어떤 값을 사용해야 하나요?',
     'answered', '주차구역 상태는 empty, occupied, disabled 중 하나를 사용하면 됩니다.',
     '2026-05-04 09:10:00', '2026-05-04 10:00:00')
ON DUPLICATE KEY UPDATE
    m_no = VALUES(m_no),
    title = VALUES(title),
    category = VALUES(category),
    content = VALUES(content),
    status = VALUES(status),
    answer = VALUES(answer),
    created_at = VALUES(created_at),
    answered_at = VALUES(answered_at);

INSERT INTO resident_inquiry (
    inquiry_no, u_no, c_no, title, content, status, answer, created_at, answered_at
)
VALUES
    (1, 2, 3, '차량번호 변경 요청', '차량번호가 변경되어 기존 등록 차량 정보를 수정하고 싶습니다.', 'pending', NULL, '2026-05-06 11:20:00', NULL),
    (2, 2, NULL, '방문객 주차 문의', '주말 방문객 주차 가능 시간을 알고 싶습니다.', 'answered', '방문객 주차는 관리사무소 승인 후 24시간 이용 가능합니다.', '2026-05-06 16:40:00', '2026-05-06 17:10:00')
ON DUPLICATE KEY UPDATE
    u_no = VALUES(u_no),
    c_no = VALUES(c_no),
    title = VALUES(title),
    content = VALUES(content),
    status = VALUES(status),
    answer = VALUES(answer),
    created_at = VALUES(created_at),
    answered_at = VALUES(answered_at);
