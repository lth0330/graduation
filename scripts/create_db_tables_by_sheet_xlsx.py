from pathlib import Path
from xml.sax.saxutils import escape
from zipfile import ZIP_DEFLATED, ZipFile


OUTPUT_PATH = Path(__file__).resolve().parents[1] / "docs" / "db_tables_by_sheet.xlsx"


TABLES = {
    "README": [
        ["항목", "내용"],
        ["작성 기준", "구글시트 DB 탭(gid=1341170782) + 현재 웹 백엔드 엔티티 기준으로 테이블별 정리"],
        ["주의", "2일 전 회의 이후 변경된 최종 DB가 따로 있으면 이 파일과 비교해서 수정 필요"],
        ["구글시트 반영 방법", "이 xlsx 파일을 구글시트에서 파일 > 가져오기 > 새 스프레드시트 또는 시트 삽입으로 업로드"],
    ],
    "apartments": [
        ["컬럼명", "DB 컬럼명", "타입", "제약조건", "설명", "샘플"],
        ["아파트 번호", "a_no", "int", "PK, auto_increment", "아파트 고유 번호", "1"],
        ["아파트명", "a_name", "varchar(30)", "not null", "아파트 이름", "명학 아파트"],
        ["아파트 비밀번호", "a_pwd", "varchar(30)", "not null, unique", "입주민 가입 인증용 아파트 비밀번호", "1111"],
        ["아파트 주소", "a_address", "varchar(255)", "not null", "아파트 기본 주소", ""],
        ["아파트 상세주소", "a_detail_address", "varchar(255)", "null", "관리사무소 등 상세 주소", ""],
    ],
    "web_manager": [
        ["컬럼명", "DB 컬럼명", "타입", "제약조건", "설명", "샘플"],
        ["웹 관리자 번호", "w_no", "int", "PK, auto_increment", "웹 관리자 고유 번호", "1"],
        ["아이디", "w_id", "varchar(255)", "not null, unique", "웹 관리자 로그인 아이디", "admin"],
        ["비밀번호", "w_pwd", "varchar(255)", "not null, bcrypt", "웹 관리자 로그인 비밀번호", "admin1234"],
    ],
    "apartment_manager": [
        ["컬럼명", "DB 컬럼명", "타입", "제약조건", "설명", "샘플"],
        ["관리자 번호", "m_no", "int", "PK, auto_increment", "아파트 관리자 고유 번호", "1"],
        ["아파트 번호", "a_no", "int", "FK -> apartments.a_no", "소속 아파트 번호", "1"],
        ["아이디", "m_id", "varchar(20)", "not null, unique", "아파트 관리자 로그인 아이디", "qwe123"],
        ["비밀번호", "m_pwd", "varchar(255)", "not null", "아파트 관리자 로그인 비밀번호", "qwer1234"],
        ["이메일", "m_email", "varchar(255)", "not null", "관리자 이메일", ""],
        ["전화번호", "m_phone", "varchar(20)", "null", "관리자 연락처", "01012345678"],
        ["주소", "m_address", "varchar(150)", "null", "관리자가 담당하는 아파트 주소", "방울내로37"],
        ["관리자명", "m_name", "varchar(30)", "null", "관리자 이름", "아무개"],
        ["재직/경력 증명서", "picture", "varchar(255)", "not null", "증빙 이미지 경로", "sample"],
        ["승인 상태", "approval_status", "varchar(20)", "not null", "PENDING/APPROVED/REJECTED", "PENDING"],
        ["거절 사유", "reject_reason", "varchar(255)", "null", "승인 거절 사유", ""],
        ["신청 일시", "requested_at", "datetime", "null", "회원가입 신청 시간", ""],
        ["승인 일시", "approved_at", "datetime", "null", "웹 관리자 승인 시간", ""],
    ],
    "user": [
        ["컬럼명", "DB 컬럼명", "타입", "제약조건", "설명", "샘플"],
        ["입주민 번호", "u_no", "int", "PK, auto_increment", "입주민 고유 번호", "1"],
        ["아이디", "u_id", "varchar(20)", "not null, unique", "입주민 로그인 아이디", "qwer1234"],
        ["비밀번호", "u_pwd", "varchar(20)", "not null", "입주민 로그인 비밀번호", "123"],
        ["이름", "u_name", "varchar(30)", "not null", "입주민 이름", ""],
        ["이메일", "u_email", "varchar(255)", "not null", "입주민 이메일", ""],
        ["전화번호", "u_phone", "varchar(20)", "null", "입주민 연락처", ""],
        ["등록날짜", "p_date", "datetime", "default now()", "입주민 가입 신청 시간", "2026-03-03"],
        ["동", "u_dong", "varchar(20)", "not null", "거주 동", ""],
        ["호", "u_ho", "varchar(20)", "not null", "거주 호수", "101호"],
        ["아파트 번호", "a_no", "int", "FK -> apartments.a_no", "소속 아파트 번호", "1"],
        ["승인 상태", "approval_status", "varchar(20)", "not null", "PENDING/APPROVED/REJECTED", "APPROVED"],
        ["거절 사유", "reject_reason", "varchar(255)", "null", "입주민 승인 거절 사유", ""],
    ],
    "car": [
        ["컬럼명", "DB 컬럼명", "타입", "제약조건", "설명", "샘플"],
        ["차량 번호", "c_no", "int", "PK, auto_increment", "차량 고유 번호", "1"],
        ["차량 이름/차주명", "c_name", "varchar(30)", "not null", "차량명 또는 차주명", "투싼"],
        ["차번호", "c_number", "varchar(255)", "not null", "차량 번호판", "123가 4567"],
        ["차 종류", "c_kind", "varchar(30)", "null", "차종", "SUV"],
        ["비고", "c_note", "varchar(255)", "null", "차량 관련 메모", ""],
        ["등록일", "c_date", "datetime", "null", "차량 등록 시간", "2026-03-03"],
        ["입주민 번호", "u_no", "int", "FK -> user.u_no", "차량 소유 입주민 번호", "3"],
    ],
    "parking_lot": [
        ["컬럼명", "DB 컬럼명", "타입", "제약조건", "설명", "샘플"],
        ["주차장 번호", "pl_no", "int", "PK, auto_increment", "주차장 고유 번호", "1"],
        ["아파트 번호", "a_no", "int", "FK -> apartments.a_no", "주차장이 속한 아파트 번호", "1"],
        ["주차장 이름", "pl_name", "varchar(255)", "not null", "주차장 이름", "지하주차장 A"],
        ["층", "pl_floor", "varchar(20)", "not null", "주차장 층", "B1"],
        ["전체 주차칸 수", "total_spaces", "int", "not null", "전체 주차 가능 칸 수", "120"],
        ["사용 중 주차칸 수", "used_spaces", "int", "not null", "현재 사용 중인 칸 수", "35"],
    ],
    "parking_zone": [
        ["컬럼명", "DB 컬럼명", "타입", "제약조건", "설명", "샘플"],
        ["주차칸 번호", "pz_no", "int", "PK, auto_increment", "주차칸 고유 번호", "1"],
        ["주차장 번호", "pl_no", "int", "FK -> parking_lot.pl_no", "소속 주차장 번호", "1"],
        ["구역 번호", "area_number", "varchar(255)", "not null", "주차칸/구역 번호", "A-01"],
        ["위치", "location", "varchar(255)", "not null", "주차칸 위치 설명", "입구 근처"],
        ["상태", "status", "varchar(20)", "not null", "empty/occupied/disabled 등", "empty"],
        ["배치 행", "layout_row", "int", "null", "프론트 주차장 배치용 행", "1"],
        ["배치 열", "layout_column", "int", "null", "프론트 주차장 배치용 열", "1"],
        ["상태 변경 사유", "status_change_reason", "varchar(255)", "null", "상태 변경 이유", "공사 중"],
    ],
    "manager_inquiry": [
        ["컬럼명", "DB 컬럼명", "타입", "제약조건", "설명", "샘플"],
        ["문의 번호", "inquiry_no", "int", "PK, auto_increment", "문의 고유 번호", "1"],
        ["관리자 번호", "m_no", "int", "FK -> apartment_manager.m_no", "문의 작성 관리자", "1"],
        ["제목", "title", "varchar(100)", "not null", "문의 제목", "주차 현황 업데이트 불가"],
        ["종류", "category", "varchar(30)", "not null", "문의 카테고리", "시스템 오류"],
        ["내용", "content", "longtext", "not null", "문의 내용", ""],
        ["상태", "status", "varchar(20)", "not null", "pending/answered", "pending"],
        ["답변", "answer", "longtext", "null", "웹 관리자 답변", ""],
        ["작성 날짜", "created_at", "datetime", "default now()", "문의 작성 시간", "2026-03-03"],
        ["답변 날짜", "answered_at", "datetime", "null", "답변 등록 시간", ""],
    ],
    "resident_inquiry": [
        ["컬럼명", "DB 컬럼명", "타입", "제약조건", "설명", "샘플"],
        ["문의 번호", "inquiry_no", "int", "PK, auto_increment", "문의 고유 번호", "1"],
        ["입주민 번호", "u_no", "int", "FK -> user.u_no", "문의 작성 입주민", "1"],
        ["차량 번호", "c_no", "int", "FK -> car.c_no, null", "문의 관련 차량", ""],
        ["제목", "title", "varchar(100)", "not null", "문의 제목", ""],
        ["내용", "content", "longtext", "not null", "문의 내용", ""],
        ["상태", "status", "varchar(20)", "not null", "pending/answered", "pending"],
        ["답변", "answer", "longtext", "null", "아파트 관리자 답변", ""],
        ["작성 날짜", "created_at", "datetime", "default now()", "문의 작성 시간", "2026-03-03"],
        ["답변 날짜", "answered_at", "datetime", "null", "답변 등록 시간", ""],
    ],
    "추가검토_앱연동": [
        ["테이블명", "필요한 경우", "주요 컬럼 후보", "비고"],
        ["visitor_car", "앱에서 방문 차량을 등록할 경우", "visitor_car_no, u_no, car_number, visitor_name, visit_start_at, visit_end_at, status", "기존 car와 분리 권장"],
        ["parking_event", "카메라/객체인식/입출차 기록을 남길 경우", "event_no, pz_no, c_no, car_number, event_type, detected_at, source, image_path, confidence", "앱/웹/파이썬 공통 연동 핵심 후보"],
        ["notification", "앱 알림을 저장할 경우", "notification_no, receiver_type, receiver_id, title, content, is_read, created_at", "승인/문의/주차 상태 알림"],
        ["resident_device", "푸시 토큰 또는 자동 로그인 기기를 관리할 경우", "device_no, u_no, device_token, platform, last_login_at", "FCM 연동 시 필요"],
    ],
}


def col_name(index):
    name = ""
    index += 1
    while index:
        index, remainder = divmod(index - 1, 26)
        name = chr(65 + remainder) + name
    return name


def sheet_xml(rows):
    body = []
    for row_index, row in enumerate(rows, start=1):
        cells = []
        for col_index, value in enumerate(row):
            value = "" if value is None else str(value)
            cell_ref = f"{col_name(col_index)}{row_index}"
            cells.append(
                f'<c r="{cell_ref}" t="inlineStr"><is><t>{escape(value)}</t></is></c>'
            )
        body.append(f'<row r="{row_index}">{"".join(cells)}</row>')

    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
 xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheetViews><sheetView workbookViewId="0"><pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews>
  <sheetData>{''.join(body)}</sheetData>
</worksheet>'''


def workbook_xml(sheet_names):
    sheets = []
    for index, name in enumerate(sheet_names, start=1):
        sheets.append(f'<sheet name="{escape(name)}" sheetId="{index}" r:id="rId{index}"/>')

    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
 xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>{''.join(sheets)}</sheets>
</workbook>'''


def workbook_rels_xml(sheet_count):
    rels = []
    for index in range(1, sheet_count + 1):
        rels.append(
            f'<Relationship Id="rId{index}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet{index}.xml"/>'
        )
    rels.append(
        f'<Relationship Id="rId{sheet_count + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>'
    )
    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  {''.join(rels)}
</Relationships>'''


def content_types_xml(sheet_count):
    overrides = []
    for index in range(1, sheet_count + 1):
        overrides.append(
            f'<Override PartName="/xl/worksheets/sheet{index}.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>'
        )
    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
  {''.join(overrides)}
</Types>'''


ROOT_RELS_XML = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>'''


STYLES_XML = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
  <fills count="1"><fill><patternFill patternType="none"/></fill></fills>
  <borders count="1"><border/></borders>
  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
  <cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>
</styleSheet>'''


def create_xlsx():
    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    sheet_names = list(TABLES.keys())

    with ZipFile(OUTPUT_PATH, "w", ZIP_DEFLATED) as archive:
        archive.writestr("[Content_Types].xml", content_types_xml(len(sheet_names)))
        archive.writestr("_rels/.rels", ROOT_RELS_XML)
        archive.writestr("xl/workbook.xml", workbook_xml(sheet_names))
        archive.writestr("xl/_rels/workbook.xml.rels", workbook_rels_xml(len(sheet_names)))
        archive.writestr("xl/styles.xml", STYLES_XML)

        for index, name in enumerate(sheet_names, start=1):
            archive.writestr(f"xl/worksheets/sheet{index}.xml", sheet_xml(TABLES[name]))

    print(OUTPUT_PATH)


if __name__ == "__main__":
    create_xlsx()
