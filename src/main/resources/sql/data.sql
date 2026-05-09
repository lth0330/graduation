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
