-- Spring Boot startup seed data.
-- 목적:
-- 1. 서버 시작 시 필요한 기본 샘플 데이터를 넣는다.
-- 2. spring.sql.init.mode=always 상태에서도 중복 PK 오류가 나지 않게 한다.
-- 3. 서버 재시작 때 기존 운영 데이터와 주차 상태를 초기값으로 덮어쓰지 않는다.
-- 4. 모든 아파트 FK(a_no)는 1번 "서초 스마트 아파트"만 참조한다.

INSERT INTO apartments (a_no, a_name, a_pwd, a_address, a_detail_address)
VALUES
    (1, '서초 스마트 아파트', '1111', '서울시 서초구 반포대로 37', '관리사무소 1층')
ON DUPLICATE KEY UPDATE
    a_no = a_no;

INSERT INTO web_manager (w_no, w_id, w_pwd)
VALUES
    -- 로그인 확인용 평문 비밀번호: 1234
    (1, 'admin', '$2a$10$a1cFmXtlqoCR.ZCiGRvJaOUXr7gHqWM.tBJg37oKsIABeuPkFoXWy')
ON DUPLICATE KEY UPDATE
    w_no = w_no;

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
    m_no = m_no;

INSERT INTO `user` (
    u_no, u_id, u_pwd, u_name, u_email, u_phone, p_date,
    u_dong, u_ho, a_no, approval_status, reject_reason,
    resident_car_limit, visitor_car_limit
)
VALUES
    (1, 'minjun12', 'user1234', '김민준', 'minjun12@example.com', '01011112222',
     '2026-04-30 09:00:00', '101', '1203', 1, 'APPROVED', NULL, 2, 2),
    (2, 'seoyeon', 'user1234', '이서연', 'seoyeon@example.com', '01022223333',
     '2026-04-29 10:20:00', '102', '804', 1, 'APPROVED', NULL, 1, 2),
    (3, 'doyoon', 'user1234', '박도윤', 'doyoon@example.com', '01033334444',
     '2026-04-28 14:10:00', '103', '1501', 1, 'PENDING', NULL, 1, 2),
    (4, 'hayoon', 'user1234', '정하윤', 'hayoon@example.com', '01044445555',
     '2026-05-01 08:40:00', '104', '502', 1, 'APPROVED', NULL, 1, 2),
    (5, 'jihoon', 'user1234', '최지훈', 'jihoon@example.com', '01055556666',
     '2026-05-02 13:15:00', '105', '1101', 1, 'APPROVED', NULL, 2, 2),
    (6, 'eunwoo', 'user1234', '한은우', 'eunwoo@example.com', '01066667777',
     '2026-05-03 16:30:00', '106', '703', 1, 'PENDING', NULL, 1, 2),
    (7, 'sua', 'user1234', '오수아', 'sua@example.com', '01077778888',
     '2026-05-04 10:05:00', '107', '901', 1, 'REJECTED', '세대 정보 확인 필요', 1, 2)
ON DUPLICATE KEY UPDATE
    u_no = u_no;

INSERT INTO car (
    c_no, c_name, c_number, c_kind, c_note, c_date, u_no
)
VALUES
    -- 차단기 테스트용 입주민 차량: POST /api/gate/check 에서 true 반환
    (1, '김민준 차량', '12가3456', '쏘나타', '기존 AWS DB에 있던 테스트 차량', '2026-04-30 09:05:00', 1),
    (2, '김민준 차량2', '789호1234', '아반떼', '차단기 OCR 테스트 차량', '2026-05-26 09:00:00', 1),
    (3, '이서연 차량', '34나7890', '카니발', '정기 등록 차량', '2026-04-29 10:25:00', 2),
    (4, '정하윤 차량', '56다1122', 'K5', '입주민 등록 차량', '2026-05-01 08:50:00', 4),
    (5, '최지훈 차량', '78라3344', '그랜저', '입주민 등록 차량', '2026-05-02 13:25:00', 5),
    (6, '최지훈 세컨드카', '90마5566', '레이', '세대 추가 차량', '2026-05-02 13:30:00', 5),
    (7, '한은우 차량', '11바7788', '투싼', '가입 승인 대기 차량', '2026-05-03 16:40:00', 6),
    (8, '오수아 차량', '22사9900', '모닝', '가입 반려 샘플 차량', '2026-05-04 10:15:00', 7)
ON DUPLICATE KEY UPDATE
    c_no = c_no;

-- 번호판 인식/차단기 테스트용 추가 승인 입주민.
-- 기존 샘플 입주민의 세대별 차량 제한을 넘기지 않도록 별도 세대 샘플로 둔다.
INSERT INTO `user` (
    u_id, u_pwd, u_name, u_email, u_phone, p_date,
    u_dong, u_ho, a_no, approval_status, reject_reason,
    resident_car_limit, visitor_car_limit
)
SELECT
    sample.u_id, sample.u_pwd, sample.u_name, sample.u_email, sample.u_phone, sample.p_date,
    sample.u_dong, sample.u_ho, sample.a_no, sample.approval_status, sample.reject_reason,
    sample.resident_car_limit, sample.visitor_car_limit
FROM (
    SELECT 'samplecar01' AS u_id, 'user1234' AS u_pwd, '차량샘플1' AS u_name, 'samplecar01@example.com' AS u_email,
           '01090010001' AS u_phone, '2026-06-04 09:00:00' AS p_date, '201' AS u_dong, '101' AS u_ho,
           1 AS a_no, 'APPROVED' AS approval_status, NULL AS reject_reason, 1 AS resident_car_limit, 2 AS visitor_car_limit
    UNION ALL
    SELECT 'samplecar02', 'user1234', '차량샘플2', 'samplecar02@example.com',
           '01090010002', '2026-06-04 09:05:00', '201', '102',
           1, 'APPROVED', NULL, 1, 2
    UNION ALL
    SELECT 'samplecar03', 'user1234', '차량샘플3', 'samplecar03@example.com',
           '01090010003', '2026-06-04 09:10:00', '201', '103',
           1, 'APPROVED', NULL, 1, 2
    UNION ALL
    SELECT 'samplecar04', 'user1234', '차량샘플4', 'samplecar04@example.com',
           '01090010004', '2026-06-04 09:15:00', '201', '104',
           1, 'APPROVED', NULL, 1, 2
    UNION ALL
    SELECT 'samplecar05', 'user1234', '차량샘플5', 'samplecar05@example.com',
           '01090010005', '2026-06-04 09:20:00', '201', '105',
           1, 'APPROVED', NULL, 1, 2
) AS sample
WHERE NOT EXISTS (
    SELECT 1 FROM `user` existing_user
    WHERE existing_user.u_id = sample.u_id
);

-- 요청 샘플 차량 6대 중 5대는 입주민 차량(car), 1대는 방문 차량(registered_cars)으로 등록한다.
-- 이미 같은 샘플 사용자에게 차량이 있으면 서버 재시작 때 덮어쓰지 않는다.
INSERT INTO car (c_name, c_number, c_kind, c_note, c_date, u_no)
SELECT sample.c_name, sample.c_number, '테스트차량', '차단기 테스트용 입주민 차량', sample.c_date, u.u_no
FROM (
    SELECT 'samplecar01' AS u_id, '차량샘플1 차량' AS c_name, '112보5273' AS c_number, '2026-06-04 09:30:00' AS c_date
    UNION ALL
    SELECT 'samplecar02', '차량샘플2 차량', '24조2426', '2026-06-04 09:35:00'
    UNION ALL
    SELECT 'samplecar03', '차량샘플3 차량', '78호12345', '2026-06-04 09:40:00'
    UNION ALL
    SELECT 'samplecar04', '차량샘플4 차량', '42바3579', '2026-06-04 09:45:00'
    UNION ALL
    SELECT 'samplecar05', '차량샘플5 차량', '37나5209', '2026-06-04 09:50:00'
) AS sample
JOIN `user` u ON u.u_id = sample.u_id
WHERE NOT EXISTS (
    SELECT 1 FROM car existing_car
    WHERE existing_car.u_no = u.u_no
);

INSERT INTO parking_lot (
    pl_no, a_no, pl_name, pl_floor, total_spaces, used_spaces
)
VALUES
    (1, 1, '서초 스마트 지하주차장', 'B1', 9, 0),
    (2, 1, '서초 스마트 지상주차장', '1F', 0, 0),
    (3, 1, '서초 스마트 방문주차장', '방문', 0, 0)
ON DUPLICATE KEY UPDATE
    pl_no = pl_no;

INSERT INTO parking_zone (
    pz_no, pl_no, area_number, location, status, zone_type, layout_row, layout_column,
    layout_width, layout_height, status_change_reason, current_car_number
)
VALUES
    (1, 1, 'a-b1-001', 'B1 1번 주차칸', 'empty', 'normal', 1, 1, 1, 2, '초기 등록', NULL),
    (2, 1, 'a-b1-002', 'B1 2번 주차칸', 'empty', 'normal', 1, 2, 1, 2, '초기 등록', NULL),
    (3, 1, 'a-b1-003', 'B1 3번 주차칸', 'empty', 'normal', 1, 4, 1, 2, '초기 등록', NULL),
    (4, 1, 'a-b1-004', 'B1 4번 주차칸', 'empty', 'normal', 1, 5, 1, 2, '초기 등록', NULL),
    (5, 1, 'a-b1-005', 'B1 5번 주차칸', 'empty', 'normal', 1, 7, 1, 2, '초기 등록', NULL),
    (6, 1, 'a-b1-006', 'B1 6번 주차칸', 'empty', 'normal', 1, 8, 1, 2, '초기 등록', NULL),
    (7, 1, 'a-b1-007', 'B1 7번 주차칸', 'empty', 'normal', 3, 1, 2, 1, '초기 등록', NULL),
    (8, 1, 'a-b1-008', 'B1 8번 주차칸', 'empty', 'normal', 3, 4, 2, 1, '초기 등록', NULL),
    (9, 1, 'a-b1-009', 'B1 9번 주차칸', 'empty', 'normal', 3, 7, 2, 1, '초기 등록', NULL)
ON DUPLICATE KEY UPDATE
    pz_no = pz_no;

INSERT INTO registered_cars (
    v_no, u_no, c_number, reg_time, park_time, expire_date
)
VALUES
    -- 차단기 테스트용 방문 차량: POST /api/gate/check 에서 true 반환
    (1, 2, '22허2026', '2026-05-08 09:00:00', NULL, NULL),
    (2, 2, '33호3030', '2026-05-08 11:10:00', '2026-05-08 12:00:00', '2026-05-09 12:00:00')
ON DUPLICATE KEY UPDATE
    v_no = v_no;

-- 요청 샘플 방문 차량 1대.
-- 이미 samplecar01 방문차량이 있으면 서버 재시작 때 덮어쓰지 않는다.
INSERT INTO registered_cars (u_no, c_number, reg_time, park_time, expire_date)
SELECT u.u_no, '123가4567', '2026-06-04 10:00:00', NULL, NULL
FROM `user` u
WHERE u.u_id = 'samplecar01'
  AND NOT EXISTS (
      SELECT 1 FROM registered_cars existing_visitor_car
      WHERE existing_visitor_car.u_no = u.u_no
  );

INSERT INTO notifications (
    noti_no, u_no, noti_type, noti_title, noti_message, is_read, created_at
)
VALUES
    (1, 2, 'visitor', '방문 차량 입차 알림', '[33호3030] 방문 차량이 주차장에 들어왔습니다.', 0, '2026-05-08 12:00:00'),
    (2, 2, 'parking', '주차구역 상태 변경', 'a-b1-002 구역이 비어 있음으로 변경되었습니다.', 1, '2026-05-08 13:30:00')
ON DUPLICATE KEY UPDATE
    noti_no = noti_no;

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
    notification_no = notification_no;

INSERT INTO device_info (
    device_id, u_no, fcm_token, os_type, last_login
)
VALUES
    ('device_2', 2, 'sample-fcm-token-2', 'android', '2026-05-08 08:30:00'),
    ('device_3', 3, 'sample-fcm-token-3', 'android', '2026-05-09 08:30:00')
ON DUPLICATE KEY UPDATE
    device_id = device_id;

INSERT INTO settings (
    setting_no, device_id, alert_push, theme_mode
)
VALUES
    (1, 'device_2', 1, 'light'),
    (2, 'device_3', 1, 'dark')
ON DUPLICATE KEY UPDATE
    setting_no = setting_no;

INSERT INTO waiting_list (
    wait_no, u_no, target_slot_id, is_notified, created_at
)
VALUES
    (1, 2, 'a-b1-001', 0, '2026-05-08 14:10:00'),
    (2, 3, 'ALL', 0, '2026-05-09 18:20:00')
ON DUPLICATE KEY UPDATE
    wait_no = wait_no;

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
    inquiry_no = inquiry_no;

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
    inquiry_no = inquiry_no;
