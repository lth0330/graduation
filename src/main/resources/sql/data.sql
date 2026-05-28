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
    -- 로그인 확인용 평문 비밀번호: admin1234
    (1, 'admin', '$2a$10$a1cFmXtlqoCR.ZCiGRvJaOUXr7gHqWM.tBJg37oKsIABeuPkFoXWy')
ON DUPLICATE KEY UPDATE
    w_id = VALUES(w_id),
    w_pwd = VALUES(w_pwd);

INSERT INTO apartment_manager (
    m_no, a_no, m_id, m_pwd, m_email, m_phone, m_address, m_name,
    picture, approval_status, reject_reason, requested_at, approved_at
)
VALUES
    -- 로그인 확인용 평문 비밀번호: qwer1234
    (1, 1, 'qwe123', '$2a$10$Kj1149qiaxyideN8RwApmOXHWrx2ky/hj9ZhTsYVr3qH5Qz3q.VCu',
     'xm3003@naver.com', '01012345678', '서울시 서초구 반포대로 37',
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
     '2026-04-28 14:10:00', '103', '1501', 1, 'PENDING', NULL),
    (4, 'hayoon', 'user1234', '정하윤', 'hayoon@example.com', '01044445555',
     '2026-05-01 08:40:00', '104', '502', 1, 'APPROVED', NULL),
    (5, 'jihoon', 'user1234', '최지훈', 'jihoon@example.com', '01055556666',
     '2026-05-02 13:15:00', '105', '1101', 1, 'APPROVED', NULL),
    (6, 'eunwoo', 'user1234', '한은우', 'eunwoo@example.com', '01066667777',
     '2026-05-03 16:30:00', '106', '703', 1, 'PENDING', NULL),
    (7, 'sua', 'user1234', '오수아', 'sua@example.com', '01077778888',
     '2026-05-04 10:05:00', '107', '901', 1, 'REJECTED', '세대 정보 확인 필요')
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
    (3, '이서연 차량', '34나 7890', '카니발', '정기 등록 차량', '2026-04-29 10:25:00', 2),
    (4, '정하윤 차량', '56다 1122', 'K5', '입주민 등록 차량', '2026-05-01 08:50:00', 4),
    (5, '최지훈 차량', '78라 3344', '그랜저', '입주민 등록 차량', '2026-05-02 13:25:00', 5),
    (6, '최지훈 세컨드카', '90마 5566', '레이', '세대 추가 차량', '2026-05-02 13:30:00', 5),
    (7, '한은우 차량', '11바 7788', '투싼', '가입 승인 대기 차량', '2026-05-03 16:40:00', 6),
    (8, '오수아 차량', '22사 9900', '모닝', '가입 반려 샘플 차량', '2026-05-04 10:15:00', 7)
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
    (1, 1, '서초 스마트 지하주차장', 'B1', 4, 0)
ON DUPLICATE KEY UPDATE
    a_no = VALUES(a_no),
    pl_name = VALUES(pl_name),
    pl_floor = VALUES(pl_floor),
    total_spaces = VALUES(total_spaces),
    used_spaces = VALUES(used_spaces);

INSERT INTO parking_zone (
    pz_no, pl_no, area_number, location, status, zone_type, layout_row, layout_column,
    status_change_reason, current_car_number
)
VALUES
    (1, 1, 'a-b1-001', 'B1 1번 주차칸', 'empty', 'normal', 1, 1, '초기 등록', NULL),
    (2, 1, 'a-b1-002', 'B1 2번 주차칸', 'empty', 'normal', 1, 2, '초기 등록', NULL),
    (3, 1, 'a-b1-003', 'B1 3번 주차칸', 'empty', 'normal', 1, 3, '초기 등록', NULL),
    (4, 1, 'a-b1-004', 'B1 4번 주차칸', 'empty', 'normal', 1, 4, '초기 등록', NULL)
ON DUPLICATE KEY UPDATE
    pl_no = VALUES(pl_no),
    area_number = VALUES(area_number),
    location = VALUES(location),
    status = VALUES(status),
    zone_type = VALUES(zone_type),
    layout_row = VALUES(layout_row),
    layout_column = VALUES(layout_column),
    status_change_reason = VALUES(status_change_reason),
    current_car_number = VALUES(current_car_number);

DELETE FROM parking_zone WHERE pz_no NOT IN (1, 2, 3, 4);
DELETE FROM parking_lot WHERE pl_no <> 1;

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
    (2, 2, 'parking', '주차구역 상태 변경', 'a-b1-002 구역이 비어 있음으로 변경되었습니다.', 1, '2026-05-08 13:30:00')
ON DUPLICATE KEY UPDATE
    u_no = VALUES(u_no),
    noti_type = VALUES(noti_type),
    noti_title = VALUES(noti_title),
    noti_message = VALUES(noti_message),
    is_read = VALUES(is_read),
    created_at = VALUES(created_at);

INSERT INTO manager_notification (
    notification_no, m_no, a_no, notification_type, title, message,
    reference_type, reference_id, is_read, created_at
)
VALUES
    (1, 1, 1, 'resident_inquiry', '입주민 문의가 등록되었습니다.',
     '입주민이 새로운 문의를 등록했습니다. 문의 관리 화면에서 확인하세요.',
     'resident_inquiry', 1, 0, '2026-05-28 09:00:00'),
    (2, 1, 1, 'abnormal_parking', '이상 주차가 감지되었습니다.',
     '일반 주차칸이 남아 있는 상태에서 통로 주차가 감지되었습니다.',
     'parking_history', NULL, 0, '2026-05-28 09:10:00')
ON DUPLICATE KEY UPDATE
    m_no = VALUES(m_no),
    a_no = VALUES(a_no),
    notification_type = VALUES(notification_type),
    title = VALUES(title),
    message = VALUES(message),
    reference_type = VALUES(reference_type),
    reference_id = VALUES(reference_id),
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
    (1, 2, 'a-b1-001', 0, '2026-05-08 14:10:00'),
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
     '2026-05-04 09:10:00', '2026-05-04 10:00:00'),
    (2, 1, '관리자 계정 권한 문의', '계정관리',
     '새로 가입한 관리자의 승인 상태가 대기 중일 때 어떤 메뉴까지 접근 가능한지 확인 부탁드립니다.',
     'pending', NULL, '2026-05-10 14:20:00', NULL),
    (3, 1, '알림 기능 테스트 문의', '시스템',
     '입주민 문의가 들어오면 아파트 관리자 화면에 알림 숫자가 표시되는지 테스트하고 있습니다.',
     'answered', '입주민 문의 등록 시 관리자 알림 테이블에 저장되도록 처리되어 있습니다.',
     '2026-05-12 09:30:00', '2026-05-12 10:05:00')
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
    (2, 2, NULL, '방문객 주차 문의', '주말 방문객 주차 가능 시간을 알고 싶습니다.', 'answered', '방문객 주차는 관리사무소 승인 후 24시간 이용 가능합니다.', '2026-05-06 16:40:00', '2026-05-06 17:10:00'),
    (3, 4, 4, '주차 위치 확인 요청', '퇴근 후 주차 위치가 앱에 바로 반영되지 않는 경우가 있어 확인 부탁드립니다.', 'pending', NULL, '2026-05-08 18:25:00', NULL),
    (4, 5, 5, '세대 차량 추가 등록 문의', '같은 세대에서 차량을 2대 등록할 때 별도 승인 절차가 필요한지 궁금합니다.', 'answered', '세대 차량 추가 등록은 관리자 승인 후 이용 가능합니다.', '2026-05-09 09:45:00', '2026-05-09 10:15:00'),
    (5, 5, 6, '통로 주차 기준 문의', '일반 주차칸이 남아 있을 때 통로 주차를 하면 알림이 발생하는지 알고 싶습니다.', 'pending', NULL, '2026-05-10 20:10:00', NULL),
    (6, 6, 7, '가입 승인 대기 중 차량 문의', '가입 승인 대기 상태에서도 차량 정보를 미리 등록할 수 있는지 확인 부탁드립니다.', 'pending', NULL, '2026-05-11 13:35:00', NULL),
    (7, 4, NULL, '주차 대기 알림 문의', '주차칸이 비었을 때 대기 신청자에게 알림이 가는지 문의드립니다.', 'answered', '대기 신청 기능과 사용자 알림 테이블을 통해 처리할 수 있습니다.', '2026-05-12 15:00:00', '2026-05-12 15:40:00')
ON DUPLICATE KEY UPDATE
    u_no = VALUES(u_no),
    c_no = VALUES(c_no),
    title = VALUES(title),
    content = VALUES(content),
    status = VALUES(status),
    answer = VALUES(answer),
    created_at = VALUES(created_at),
    answered_at = VALUES(answered_at);
