INSERT INTO apartments (a_no, a_name, a_pwd)
VALUES
    (1, '명학 아파트', '1111'),
    (2, '성결 아파트', '1234'),
    (3, '안양 아파트', '2222');

INSERT INTO web_manager (w_no, id, pwd)
VALUES
    (1, 'admin', 'admin1234');

INSERT INTO apartment_manager (
    m_no,
    a_no,
    m_id,
    m_pwd,
    m_email,
    m_phone,
    m_address,
    m_name,
    picture
)
VALUES
    (1, 1, 'qwe123', 'qwer1234', 'sample1@naver.com', '1012345678', '방울내로37(j-sky 아파트)', '아무개', 'sample'),
    (2, 2, 'qwe234', 'qwer12345', 'sample4@naver.com', '10123456789', '방울내로36(j-sky 아파트)', '이무개', 'sample'),
    (3, 3, 'qwe345', 'qwer12346', 'sample2@naver.com', '1023456789', '방울내로35(j-sky 아파트)', '김무개', 'sample');


