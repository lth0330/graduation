INSERT INTO apartments (a_no, a_name, a_pwd, a_address, a_detail_address)
VALUES
    (1, '한빛 아파트', '1111', '서울시 강남구 한빛로 37', '관리사무소'),
    (2, '그린 파크', '1234', '서울시 송파구 그린로 88', '관리사무소'),
    (3, '중앙 아파트', '2222', '경기도 성남시 중앙로 45', '관리사무소'),
    (4, '라온 타워', '3333', '인천시 연수구 라온로 10', '관리사무소');

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
    (
        1,
        1,
        'qwe123',
        '$2a$10$Kj1149qiaxyideN8RwApmOXHWrx2ky/hj9ZhTsYVr3qH5Qz3q.VCu',
        'sample1@naver.com',
        '01012345678',
        '서울시 강남구 한빛로 37',
        '김관리',
        'sample-manager-1.png',
        'APPROVED',
        NULL,
        '2026-04-27 09:00:00',
        '2026-04-27 11:30:00'
    ),
    (
        2,
        2,
        'qwe234',
        '$2a$10$CsMRs2A4fEsEgS60oiEnD.qz/nkzn4MzD17w5TwmPZMaDQcqzJH7e',
        'sample2@naver.com',
        '01023456789',
        '서울시 송파구 그린로 88',
        '이관리',
        'sample-manager-2.png',
        'PENDING',
        NULL,
        '2026-04-28 10:15:00',
        NULL
    ),
    (
        3,
        3,
        'qwe345',
        '$2a$10$yFLyresin06dL4/jXnRsn.VeLJKmgAT.5S8nq50gwgbzgvJsbNjLm',
        'sample3@naver.com',
        '01034567890',
        '경기도 성남시 중앙로 45',
        '박관리',
        'sample-manager-3.png',
        'REJECTED',
        '관리사무소 주소 확인이 필요합니다.',
        '2026-04-29 14:20:00',
        NULL
    ),
    (
        4,
        4,
        'qwe456',
        '$2a$10$Gf98eXniRRu9WUbxRDgk4eX9PCWC521cw1rY73.4Vj/svi2ZC3oz2',
        'sample4@naver.com',
        '01045678901',
        '인천시 연수구 라온로 10',
        '최관리',
        'sample-manager-4.png',
        'PENDING',
        NULL,
        '2026-04-30 16:00:00',
        NULL
    );

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
    (
        1,
        'minjun12',
        'user1234',
        '김민준',
        'minjun12@example.com',
        '01012345678',
        '2026-04-30 09:00:00',
        '101',
        '1203',
        1,
        'PENDING',
        NULL
    ),
    (
        2,
        'seoyeon',
        'user1234',
        '이서연',
        'seoyeon@example.com',
        '01023456789',
        '2026-04-29 10:20:00',
        '102',
        '804',
        1,
        'APPROVED',
        NULL
    ),
    (
        3,
        'doyoon',
        'user1234',
        '박도윤',
        'doyoon@example.com',
        '01034567890',
        '2026-04-28 14:10:00',
        '103',
        '1501',
        1,
        'REJECTED',
        '입주민 정보 확인이 필요합니다.'
    );

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
    (1, '김민준 차량', '12가 3456', 'SUV', '입주민 등록 차량', '2026-04-30 09:05:00', 1),
    (2, '이서연 차량', '34나 7890', '세단', '', '2026-04-29 10:25:00', 2),
    (3, '박도윤 차량', '56다 1122', '전기차', '전기차 충전 구역 이용', '2026-04-28 14:15:00', 3);

INSERT INTO parking_lot (
    pl_no,
    a_no,
    pl_name,
    pl_floor,
    total_spaces,
    used_spaces
)
VALUES
    (1, 1, 'A동 지하주차장', 'B1', 120, 82),
    (2, 1, 'A동 지하주차장', 'B2', 110, 76),
    (3, 1, '방문객 주차장', '1F', 40, 18);

INSERT INTO parking_zone (
    pz_no,
    pl_no,
    area_number,
    location,
    status,
    layout_row,
    layout_column,
    status_change_reason
)
VALUES
    (1, 1, 'A-B1-001', 'A동 지하주차장 B1 입구 앞', 'empty', 1, 1, '초기 등록'),
    (2, 1, 'A-B1-002', 'A동 지하주차장 B1 중앙', 'occupied', 1, 2, '입주민 사용 중'),
    (3, 1, 'A-B1-003', 'A동 지하주차장 B1 엘리베이터 근처', 'empty', 1, 3, '초기 등록'),
    (4, 1, 'A-B1-004', 'A동 지하주차장 B1 전기차 구역', 'disabled', 1, 4, '시설 점검'),
    (5, 2, 'A-B2-001', 'A동 지하주차장 B2 입구 앞', 'occupied', 1, 1, '입주민 사용 중');

INSERT INTO apartments (a_no, a_name, a_pwd, a_address, a_detail_address)
VALUES
    (5, '스카이 빌리지', '4444', '서울시 마포구 하늘로 12', '101동 관리사무소'),
    (6, '해든 아파트', '5555', '부산시 해운대구 해든로 21', '정문 관리사무소'),
    (7, '누리 센트럴', '6666', '대구시 수성구 누리로 33', '지하 1층 관리사무소'),
    (8, '라움 레지던스', '7777', '광주시 서구 라움로 18', '커뮤니티센터 1층'),
    (9, '미래 포레스트', '8888', '대전시 유성구 미래로 52', '관리동 2층');

INSERT INTO web_manager (w_no, w_id, w_pwd)
VALUES
    (2, 'admin2', '$2a$10$SkDj7r414ZLt9G3jiBn6a.YmGWmV15c1odDYqu5YyG2bDZ6WyajU6'),
    (3, 'admin3', '$2a$10$1ybhUWB8o1YPzYrsaIUbNuozF8egaWjDGvU39SRDVkKWorHxUALOC'),
    (4, 'admin4', '$2a$10$z5V.CUq.SjfE4bw6Sb2.9.eun2t69HAOjbeVrMy.PbAMh.YZCKrSK'),
    (5, 'admin5', '$2a$10$gnYc77/ST0hFy161roqXm.JXMZcwn9T9qVJGd5xEhixW5YDMO5FLW'),
    (6, 'admin6', '$2a$10$kLq8k4PnyP0C.OUSovBD..oXNV7ncujM.DbJ8eu9ZeYCVo.0s6/3i');

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
    (
        5,
        5,
        'skyadmin',
        '$2a$10$Kj1149qiaxyideN8RwApmOXHWrx2ky/hj9ZhTsYVr3qH5Qz3q.VCu',
        'skyadmin@example.com',
        '01050000001',
        '서울시 마포구 하늘로 12',
        '정하늘',
        'sample-manager-5.png',
        'APPROVED',
        NULL,
        '2026-05-01 09:10:00',
        '2026-05-01 13:20:00'
    ),
    (
        6,
        6,
        'haedeun',
        '$2a$10$CsMRs2A4fEsEgS60oiEnD.qz/nkzn4MzD17w5TwmPZMaDQcqzJH7e',
        'haedeun@example.com',
        '01050000002',
        '부산시 해운대구 해든로 21',
        '윤바다',
        'sample-manager-6.png',
        'APPROVED',
        NULL,
        '2026-05-01 10:30:00',
        '2026-05-01 14:00:00'
    ),
    (
        7,
        7,
        'nuriadmin',
        '$2a$10$yFLyresin06dL4/jXnRsn.VeLJKmgAT.5S8nq50gwgbzgvJsbNjLm',
        'nuriadmin@example.com',
        '01050000003',
        '대구시 수성구 누리로 33',
        '강누리',
        'sample-manager-7.png',
        'PENDING',
        NULL,
        '2026-05-02 11:45:00',
        NULL
    ),
    (
        8,
        8,
        'raumadmin',
        '$2a$10$Gf98eXniRRu9WUbxRDgk4eX9PCWC521cw1rY73.4Vj/svi2ZC3oz2',
        'raumadmin@example.com',
        '01050000004',
        '광주시 서구 라움로 18',
        '오라움',
        'sample-manager-8.png',
        'REJECTED',
        '첨부 이미지 확인이 필요합니다.',
        '2026-05-02 15:10:00',
        NULL
    ),
    (
        9,
        9,
        'futuremgr',
        '$2a$10$Kj1149qiaxyideN8RwApmOXHWrx2ky/hj9ZhTsYVr3qH5Qz3q.VCu',
        'futuremgr@example.com',
        '01050000005',
        '대전시 유성구 미래로 52',
        '한미래',
        'sample-manager-9.png',
        'PENDING',
        NULL,
        '2026-05-03 08:50:00',
        NULL
    );

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
    (
        4,
        'jiyoon',
        'user1234',
        '한지윤',
        'jiyoon@example.com',
        '01060000001',
        '2026-05-01 09:30:00',
        '201',
        '502',
        5,
        'APPROVED',
        NULL
    ),
    (
        5,
        'hyunwoo',
        'user1234',
        '최현우',
        'hyunwoo@example.com',
        '01060000002',
        '2026-05-01 10:05:00',
        '202',
        '1101',
        6,
        'APPROVED',
        NULL
    ),
    (
        6,
        'gaeun',
        'user1234',
        '문가은',
        'gaeun@example.com',
        '01060000003',
        '2026-05-02 13:15:00',
        '203',
        '704',
        7,
        'PENDING',
        NULL
    ),
    (
        7,
        'taeho',
        'user1234',
        '서태호',
        'taeho@example.com',
        '01060000004',
        '2026-05-02 16:40:00',
        '204',
        '1508',
        8,
        'REJECTED',
        '동/호수 확인 서류가 필요합니다.'
    ),
    (
        8,
        'sumin',
        'user1234',
        '임수민',
        'sumin@example.com',
        '01060000005',
        '2026-05-03 09:25:00',
        '205',
        '903',
        9,
        'APPROVED',
        NULL
    );

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
    (4, '한지윤 차량', '78라 3344', '세단', '정기 등록 차량', '2026-05-01 09:40:00', 4),
    (5, '최현우 차량', '91마 7788', 'SUV', '장기 주차 차량', '2026-05-01 10:15:00', 5),
    (6, '문가은 차량', '23바 9900', '경차', '승인 대기 입주민 차량', '2026-05-02 13:25:00', 6),
    (7, '서태호 차량', '45사 2211', '전기차', '승인 반려 입주민 차량', '2026-05-02 16:50:00', 7),
    (8, '임수민 차량', '67아 5566', '하이브리드', '방문 차량 전환 예정', '2026-05-03 09:35:00', 8);

INSERT INTO parking_lot (
    pl_no,
    a_no,
    pl_name,
    pl_floor,
    total_spaces,
    used_spaces
)
VALUES
    (4, 5, '스카이 빌리지 지하주차장', 'B1', 95, 41),
    (5, 6, '해든 아파트 지하주차장', 'B1', 130, 92),
    (6, 7, '누리 센트럴 주차타워', '2F', 80, 36),
    (7, 8, '라움 레지던스 지하주차장', 'B2', 105, 58),
    (8, 9, '미래 포레스트 방문객 주차장', '1F', 45, 17);

INSERT INTO parking_zone (
    pz_no,
    pl_no,
    area_number,
    location,
    status,
    layout_row,
    layout_column,
    status_change_reason
)
VALUES
    (6, 4, 'S-B1-001', '스카이 빌리지 B1 입구 앞', 'empty', 1, 1, '초기 등록'),
    (7, 5, 'H-B1-014', '해든 아파트 B1 중앙 구역', 'occupied', 2, 4, '입주민 사용 중'),
    (8, 6, 'N-2F-021', '누리 센트럴 주차타워 2F', 'empty', 3, 1, '초기 등록'),
    (9, 7, 'R-B2-008', '라움 레지던스 B2 전기차 구역', 'disabled', 2, 3, '충전기 점검'),
    (10, 8, 'F-1F-003', '미래 포레스트 방문객 주차장 1F', 'occupied', 1, 3, '방문 차량 사용 중');
