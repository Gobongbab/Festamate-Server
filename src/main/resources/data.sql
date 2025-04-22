SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE profile_image;
TRUNCATE TABLE ban;
TRUNCATE TABLE member;
TRUNCATE TABLE chat_room;
TRUNCATE TABLE coupon;
TRUNCATE TABLE report;
TRUNCATE TABLE room_participant;
TRUNCATE TABLE room_image;
TRUNCATE TABLE room;

SET FOREIGN_KEY_CHECKS = 1;

-- ProfileImage 테이블 더미 데이터 (Member용)
-- ProfileImage 엔티티는 Image 엔티티를 @Embedded 하므로, image의 필드들이 ProfileImage 테이블 컬럼으로 존재한다고 가정합니다.
INSERT INTO profile_image (upload_name, store_name, url)
VALUES ('profile1.jpg', 'uuid_profile1.jpg', 'http://example.com/images/profile1.jpg'),
       ('profile2.png', 'uuid_profile2.png', 'http://example.com/images/profile2.png');

-- Member 테이블 더미 데이터
-- profile_image_id는 위에서 생성한 ProfileImage의 id를 참조합니다.
-- Gender, Major, MemberStatus Enum 값은 실제 Enum 정의에 맞게 문자열로 입력해야 합니다.
INSERT INTO member (kakao_id, is_profile_completed, name, nickname, student_id, phone_number,
                    gender, student_department, maximum_ticket, remaining_ticket, profile_image_id,
                    role, status, token)
VALUES (9876543210, true, '이메이트', '축제러버', '20220002', '010-2222-2222', 'FEMALE', '글로벌어문학부', 2, 1, 2,
        'ROLE_USER', 'ACTIVE', 'fcmToken2'),
       (1112223331, true, '박축제', '축제지기1', '20230003', '010-3333-1111', 'MALE', '전자공학과', 2, 2, 1,
        'ROLE_USER', 'ACTIVE', 'fcmToken3'),
       (1112223332, true, '최메이트', '메이트 파인더', '20200004', '010-3333-2222', 'FEMALE', '국어국문학과', 2, 1,
        2, 'ROLE_USER', 'ACTIVE', 'fcmToken4'),
       (1112223333, false, '정페스타', '페스타고고', '20240005', '010-3333-3333', 'MALE', '기계시스템공학과', 2, 2,
        1, 'ROLE_USER', 'ACTIVE', 'fcmToken5'),
       (1112223334, true, '강즐겜', '즐겜유저', '20210006', '010-3333-4444', 'FEMALE', '화학과', 2, 0, 2,
        'ROLE_USER', 'ACTIVE', 'fcmToken6'),
       (1112223335, true, '조동행', '같이갈래', '20220007', '010-3333-5555', 'MALE', '신소재화학공학부', 2, 2, 1,
        'ROLE_USER', 'ACTIVE', 'fcmToken7'),
       (1112223336, false, '윤신입', '새내기파워', '20250008', '010-3333-6666', 'FEMALE', '바이오융합학부', 2, 2,
        2,
        'ROLE_USER', 'ACTIVE', 'fcmToken8'),
       (1112223337, true, '장코드', '코딩마스터', '20190009', '010-3333-7777', 'MALE', 'AI컴퓨터공학부', 3, 3, 1,
        'ROLE_USER', 'ACTIVE', 'fcmToken9'),
       (1112223338, true, '임스터디', '열공모드', '20200010', '010-3333-8888', 'FEMALE', '수학과', 2, 1, 2,
        'ROLE_USER', 'ACTIVE', 'fcmToken10'),
       (1112223339, true, '한아트', '예술혼', '20210011', '010-3333-9999', 'MALE', '서양화전공', 2, 2, 1,
        'ROLE_USER', 'ACTIVE', 'fcmToken11'),
       (1112223340, false, '오뮤직', '음악사랑', '20220012', '010-4444-1111', 'FEMALE', '스포츠건강과학전공', 2, 2,
        2,
        'ROLE_USER', 'ACTIVE', 'fcmToken12'),
       (1112223341, true, '서경영', '미래 CEO', '20230013', '010-4444-2222', 'MALE', 'Fine Arts학부', 2, 0,
        1,
        'ROLE_USER', 'ACTIVE', 'fcmToken13'),
       (1112223342, true, '신사회', '사회탐구', '20200014', '010-4444-3333', 'FEMALE', '휴먼서비스학부', 2, 1, 2,
        'ROLE_USER', 'ACTIVE', 'fcmToken14'),
       (1112223343, true, '권체육', '운동짱', '20210015', '010-4444-4444', 'MALE', '실용음악학과', 2, 2, 1,
        'ROLE_USER', 'ACTIVE', 'fcmToken15'),
       (1112223344, false, '황지리', '지리박사', '20240016', '010-4444-5555', 'FEMALE', '경제학부', 2, 2, 2,
        'ROLE_USER', 'ACTIVE', 'fcmToken16'),
       (1112223345, true, '안역사', '역사덕후', '20190017', '010-4444-6666', 'MALE', '문헌정보학과', 2, 2, 1,
        'ROLE_USER', 'ACTIVE', 'fcmToken17'),
       (1112223346, true, '송교육', '참선생', '20200018', '010-4444-7777', 'FEMALE', '유아교육과', 2, 1, 2,
        'ROLE_USER', 'ACTIVE', 'fcmToken18'),
       (1112223347, true, '전법학', '정의구현', '20210019', '010-4444-8888', 'MALE', '법학과', 2, 0, 1,
        'ROLE_USER', 'ACTIVE', 'fcmToken19'),
       (1112223348, false, '홍정외', '외교관꿈', '20220020', '010-4444-9999', 'FEMALE', '정치외교학전공', 2, 2, 2,
        'ROLE_USER', 'ACTIVE', 'fcmToken20'),
       (1112223349, true, '유디자인', '디자인감각', '20230021', '010-5555-1111', 'MALE', '디자인비즈학부', 2, 2,
        1, 'ROLE_USER', 'ACTIVE', 'fcmToken21'),
       (1112223350, true, '고건축', '건축학도', '20200022', '010-5555-2222', 'FEMALE', '건축학과', 2, 1, 2,
        'ROLE_USER', 'ACTIVE', 'fcmToken22');


-- 참고: authorities와 oauthInfo 필드는 @ElementCollection, @Embedded 특성상 별도 처리나 추가 컬럼이 필요할 수 있으나, 기본적인 INSERT 문에서는 생략합니다.
-- Room 테이블 더미 데이터
-- host_id는 Member 테이블의 id를 참조합니다.
-- preferredGender Enum 값은 실제 Enum 정의에 맞게 문자열로 입력해야 합니다.
INSERT INTO room (title, place, content, status, max_participants, preferred_gender,
                  preferred_student_id_min, preferred_student_id_max, meeting_date_time, host_id)
VALUES ('같이 축제 갈 사람 구함!', '학교 앞 광장', '저녁 7시에 만나서 같이 공연봐요.', 'MATCHING', 4, 'FEMALE', '20', '23',
        '2025-05-15 19:00:00', 1),
       ('저녁 같이 먹고 푸드트럭 가실 분', '학생회관 앞', '6시쯤 만나서 저녁 먹고 푸드트럭 구경 가요!', 'MATCHING', 3, 'MALE', '21',
        '24', '2025-05-16 18:00:00', 3),
       ('힙합 공연 같이 볼 여성분 구해요', '대운동장 무대 근처', '앞자리에서 같이 힙합 공연 즐겨요!', 'MATCHING', 2, 'FEMALE', '20',
        '23', '2025-05-16 20:00:00', 4),
       ('밴드 공연 같이 볼 사람 (남성 선호)', '소극장 앞', '락밴드 공연 같이 신나게 즐길 분 찾습니다.', 'MATCHING', 4, 'MALE', '20',
        '25', '2025-05-17 19:30:00', 5),
       ('주점 같이 갈 메이트 (아무나)', '주점 거리', '왁자지껄 주점에서 같이 한잔해요!', 'MATCHING', 6, 'FEMALE', '19', '24',
        '2025-05-17 21:00:00', 6),
       ('동아리 공연 응원 갈 파티원 모집', '동아리 부스 거리', '우리 동아리 공연 보러 같이 가요~', 'MATCHING', 5, 'MALE', '20', '25',
        '2025-05-16 15:00:00', 7),
       ('낮에 플리마켓 구경할 메이트', '중앙 도서관 앞', '아기자기한 플리마켓 구경하고 커피 마셔요.', 'MATCHING', 2, 'FEMALE', '22',
        '25', '2025-05-17 14:00:00', 8),
       ('밤샘 주점팟 구합니다!', '상경대 주점', '밤새 신나게 놀 사람 여기 붙어라~', 'MATCHING', 4, 'MALE', '19', '22',
        '2025-05-16 22:00:00', 9),
       ('조용히 야경 보며 맥주 마실 분', '옥상 정원', '축제 야경 보면서 조용히 맥주 한 잔 해요.', 'MATCHING', 2, 'FEMALE', '20',
        '23',
        '2025-05-17 22:30:00', 10),
       ('연예인 무대 같이 볼 사람!', '대운동장 메인 스테이지', '가수 OOO 무대 같이 앞에서 봐요!', 'MATCHING', 3, 'MALE', '20',
        '24',
        '2025-05-18 20:00:00', 11),
       ('축제 첫날 같이 시작할 메이트!', '정문 앞', '오후 3시에 만나서 축제 전체적으로 훑어봐요.', 'MATCHING', 4, 'MALE', '23', '25',
        '2025-05-16 15:00:00', 12),
       ('공대 주점 같이 가실 분 (여성)', '공과대학 앞', '공대 주점 맛있대요! 같이 맛보러 가요.', 'MATCHING', 2, 'FEMALE', '21',
        '24', '2025-05-17 19:00:00', 13),
       ('버스킹 공연 함께 즐길 분 (남성)', '버스킹 존', '잔잔한 버스킹 공연 같이 들어요.', 'MATCHING', 2, 'MALE', '20', '22',
        '2025-05-16 18:30:00', 14),
       ('점심으로 푸드트럭 도장깨기 할 파티', '푸드트럭 존', '여러 푸드트럭 음식 나눠먹어요!', 'MATCHING', 4, 'MALE', '20', '25',
        '2025-05-17 12:00:00', 15),
       ('타로/사주 부스 같이 가실 분', '동아리 부스 거리', '재미로 타로나 사주 봐요!', 'MATCHING', 2, 'FEMALE', '21', '25',
        '2025-05-18 16:00:00', 16),
       ('체험 부스 같이 참여할 메이트 (남성)', '체험 부스 존', '만들기 체험 같은 거 같이 해봐요.', 'MATCHING', 2, 'MALE', '22',
        '24', '2025-05-16 14:00:00', 17),
       ('불꽃놀이 명당에서 같이 볼 사람', '본관 뒤 언덕', '불꽃놀이 잘 보이는 곳에서 같이 봐요!', 'MATCHING', 4, 'FEMALE', '19',
        '25',
        '2025-05-18 21:30:00', 18),
       ('축제 마지막 날 마무리 주점팟', '인문대 주점', '마지막 날 아쉬움을 달래며 한잔!', 'MATCHING', 6, 'MALE', '20', '23',
        '2025-05-18 20:30:00', 19),
       ('사진 찍으면서 축제 구경할 메이트', '캠퍼스 전역', '예쁜 스팟에서 서로 사진 찍어주며 구경해요.', 'MATCHING', 2, 'FEMALE', '20',
        '24', '2025-05-17 15:00:00', 20),
       ('DJ 파티 같이 즐길 사람! (텐션 UP)', '대운동장 특설무대', 'EDM 파티 신나게 같이 즐겨요!', 'MATCHING', 5, 'MALE', '19',
        '23', '2025-05-17 23:00:00', 21),
       ('선배/후배랑 같이 놀아요 (20-22학번)', '학생회관 라운지', '20~22학번끼리 모여서 편하게 얘기하고 놀아요.', 'MATCHING', 4,
        'FEMALE',
        '20', '22', '2025-05-16 17:00:00', 21),
       ('복학생들끼리 모여봅시다!', '복학생 쉼터(?)', '오랜만에 학교 축제 즐기는 복학생들 모여요!', 'MATCHING', 3, 'MALE', '18', '20',
        '2025-05-17 18:00:00', 1),
       ('막걸리 파티원 모집 (여성 우대)', '민속 주점', '파전에 막걸리 크으~ 여자분들 환영!', 'MATCHING', 4, 'FEMALE', '21', '24',
        '2025-05-18 19:00:00', 2),
       ('축구/농구 게임 관람 메이트 (남성)', '체육관 앞', '같이 스포츠 게임 관람하고 응원해요!', 'MATCHING', 3, 'MALE', '20', '23',
        '2025-05-17 16:00:00', 3),
       ('졸업 전에 마지막 축제 즐기실 분', '어디든 좋아요', '곧 졸업인데 마지막 축제 후회없이 놀아요!', 'MATCHING', 4, 'FEMALE', '19',
        '21', '2025-05-16 19:00:00', 4),
       ('영화 상영회 같이 볼 사람', '시청각실', '밤에 하는 영화 상영회 같이 봐요.', 'MATCHING', 2, 'MALE', '20', '25',
        '2025-05-17 22:00:00', 5),
       ('굿즈 쇼핑 메이트 구함', '굿즈 판매 부스', '축제 기념 굿즈 같이 구경하고 사요!', 'MATCHING', 2, 'FEMALE', '22', '25',
        '2025-05-18 14:00:00', 6),
       ('저녁 공연 전까지 시간 때울 분', '카페테리아', '저녁 공연 전에 만나서 수다 떨어요.', 'MATCHING', 3, 'MALE', '21', '23',
        '2025-05-16 17:30:00', 7),
       ('새내기들끼리 모여서 놀아요!', '새내기 부스 앞', '25학번 새내기들 모여서 친해져요!', 'MATCHING', 6, 'FEMALE', '25', '25',
        '2025-05-17 13:00:00', 8),
       ('조용히 산책하며 축제 분위기 느낄 분', '캠퍼스 산책로', '시끄러운 곳 피해 조용히 산책해요.', 'MATCHING', 2, 'MALE', '20', '24',
        '2025-05-18 17:00:00', 9),
       ('보드게임 부스 같이 갈 사람!', '동아리 부스 거리', '보드게임 하면서 시간 보내요~', 'MATCHING', 4, 'FEMALE', '20', '25',
        '2025-05-16 16:00:00', 10);

-- RoomImage 테이블 더미 데이터
-- room_id는 Room 테이블의 id를 참조합니다.
-- RoomImage 엔티티는 Image 엔티티를 @Embedded 하므로, image의 필드들이 RoomImage 테이블 컬럼으로 존재한다고 가정합니다.
INSERT INTO room_image (upload_name, store_name, url, room_id)
VALUES ('festival_1.jpg', 'uuid_festival_1.jpg',
        'https://images.unsplash.com/photo-1472653431158-6364773b2a56?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTJ8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        1),
       ('festival_2.jpg', 'uuid_festival_2.jpg',
        'https://images.unsplash.com/photo-1492684223066-81342ee5ff30', 2),
       ('festival_3.jpg', 'uuid_festival_3.jpg',
        'https://images.unsplash.com/photo-1501238295340-c810d3c156d2?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjB8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        3),
       ('festival_4.jpg', 'uuid_festival_4.jpg',
        'https://images.unsplash.com/photo-1492684223066-81342ee5ff30', 4),
       ('festival_5.jpg', 'uuid_festival_5.jpg',
        'https://plus.unsplash.com/premium_photo-1661661967141-8c5f1b10f31b?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mzd8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        5),
       ('festival_6.jpg', 'uuid_festival_6.jpg',
        'https://images.unsplash.com/photo-1506157786151-b8491531f063?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8ZmVzdGl2YWx8ZW58MHx8MHx8fDA%3D',
        6),
       ('festival_7.jpg', 'uuid_festival_7.jpg',
        'https://images.unsplash.com/photo-1472653431158-6364773b2a56?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTJ8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        7),
       ('festival_8.jpg', 'uuid_festival_8.jpg',
        'https://images.unsplash.com/photo-1492684223066-81342ee5ff30', 8),
       ('festival_9.jpg', 'uuid_festival_9.jpg',
        'https://images.unsplash.com/photo-1501238295340-c810d3c156d2?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjB8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        9),
       ('festival_10.jpg', 'uuid_festival_10.jpg',
        'https://images.unsplash.com/photo-1506157786151-b8491531f063?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8ZmVzdGl2YWx8ZW58MHx8MHx8fDA%3D',
        10),
       ('festival_11.jpg', 'uuid_festival_11.jpg',
        'https://images.unsplash.com/photo-1472653431158-6364773b2a56?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTJ8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        11),
       ('festival_12.jpg', 'uuid_festival_12.jpg',
        'https://images.unsplash.com/photo-1492684223066-81342ee5ff30', 12),
       ('festival_13.jpg', 'uuid_festival_13.jpg',
        'https://images.unsplash.com/photo-1501238295340-c810d3c156d2?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjB8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        13),
       ('festival_14.jpg', 'uuid_festival_14.jpg',
        'https://plus.unsplash.com/premium_photo-1661661967141-8c5f1b10f31b?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mzd8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        14),
       ('festival_15.jpg', 'uuid_festival_15.jpg',
        'https://images.unsplash.com/photo-1506157786151-b8491531f063?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8ZmVzdGl2YWx8ZW58MHx8MHx8fDA%3D',
        15),
       ('festival_16.jpg', 'uuid_festival_16.jpg',
        'https://images.unsplash.com/photo-1472653431158-6364773b2a56?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTJ8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        16),
       ('festival_17.jpg', 'uuid_festival_17.jpg',
        'https://images.unsplash.com/photo-1492684223066-81342ee5ff30', 17),
       ('festival_18.jpg', 'uuid_festival_18.jpg',
        'https://images.unsplash.com/photo-1501238295340-c810d3c156d2?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjB8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        18),
       ('festival_19.jpg', 'uuid_festival_19.jpg',
        'https://plus.unsplash.com/premium_photo-1661661967141-8c5f1b10f31b?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mzd8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        19),
       ('festival_20.jpg', 'uuid_festival_20.jpg',
        'https://images.unsplash.com/photo-1506157786151-b8491531f063?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8ZmVzdGl2YWx8ZW58MHx8MHx8fDA%3D',
        20),
       ('festival_21.jpg', 'uuid_festival_21.jpg',
        'https://images.unsplash.com/photo-1472653431158-6364773b2a56?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTJ8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        21),
       ('festival_22.jpg', 'uuid_festival_22.jpg',
        'https://images.unsplash.com/photo-1492684223066-81342ee5ff30', 22),
       ('festival_23.jpg', 'uuid_festival_23.jpg',
        'https://images.unsplash.com/photo-1501238295340-c810d3c156d2?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjB8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        23),
       ('festival_24.jpg', 'uuid_festival_24.jpg',
        'https://plus.unsplash.com/premium_photo-1661661967141-8c5f1b10f31b?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mzd8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        24),
       ('festival_25.jpg', 'uuid_festival_25.jpg',
        'https://images.unsplash.com/photo-1506157786151-b8491531f063?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8ZmVzdGl2YWx8ZW58MHx8MHx8fDA%3D',
        25),
       ('festival_26.jpg', 'uuid_festival_26.jpg',
        'https://images.unsplash.com/photo-1472653431158-6364773b2a56?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTJ8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        26),
       ('festival_27.jpg', 'uuid_festival_27.jpg',
        'https://images.unsplash.com/photo-1492684223066-81342ee5ff30', 27),
       ('festival_28.jpg', 'uuid_festival_28.jpg',
        'https://images.unsplash.com/photo-1501238295340-c810d3c156d2?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjB8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        28),
       ('festival_29.jpg', 'uuid_festival_29.jpg',
        'https://plus.unsplash.com/premium_photo-1661661967141-8c5f1b10f31b?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mzd8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        29),
       ('festival_30.jpg', 'uuid_festival_30.jpg',
        'https://images.unsplash.com/photo-1506157786151-b8491531f063?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8ZmVzdGl2YWx8ZW58MHx8MHx8fDA%3D',
        30),
       ('festival_31.jpg', 'uuid_festival_31.jpg',
        'https://images.unsplash.com/photo-1472653431158-6364773b2a56?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTJ8fGZlc3RpdmFsfGVufDB8fDB8fHww',
        31);

-- chat_room 테이블 더미 데이터
-- room_id는 Room 테이블의 id를 참조합니다.
INSERT INTO chat_room (id, room_id, name)
VALUES (1, 1, '봄맞이 벚꽃모임'),
       (2, 2, '여름 바다 여행 준비방'),
       (3, 3, '겨울 눈꽃 캠프'),
       (4, 4, '한강 피크닉 크루'),
       (5, 5, '책 읽는 사람들'),
       (6, 6, '스터디 윗 미'),
       (7, 7, '축구는 못 참지'),
       (8, 8, '넷플릭스 같이 볼 사람'),
       (9, 9, '카페 탐방 동호회'),
       (10, 10, '새벽 러너즈'),
       (11, 11, '도시락 나눔 모임'),
       (12, 12, '사진찍기 좋아하는 사람들'),
       (13, 13, '연극 같이 볼래요'),
       (14, 14, '보드게임 원정대'),
       (15, 15, '코딩 같이 해요'),
       (16, 16, '프론트엔드 개발자 모임'),
       (17, 17, '디자이너와 개발자 커넥트'),
       (18, 18, '번개 모임 알림방'),
       (19, 19, '디저트 맛집 정복기'),
       (20, 20, '운동 루틴 공유방'),
       (21, 21, '취업 준비생 모임'),
       (22, 22, '반려동물 자랑방'),
       (23, 23, '여행 플래너스'),
       (24, 24, '자취생 정보 공유'),
       (25, 25, '랜덤 채팅방'),
       (26, 26, '인디 음악 공유'),
       (27, 27, '작곡 작사 클럽'),
       (28, 28, '전시회 같이 가요'),
       (29, 29, '영화 리뷰어즈'),
       (30, 30, '도전! 다이어트'),
       (31, 31, '명상 & 마인드풀니스');



-- Coupon 테이블 더미 데이터
-- member_id는 Member 테이블의 id를 참조합니다.
INSERT INTO coupon (code, used, expires_at, member_id)
VALUES ('WELCOME2025', false, '2025-12-31 23:59:59', 1);

-- Report 테이블 더미 데이터
-- reporter_id, reportedMember_id는 Member 테이블의 id를 참조합니다.
-- room_id는 Room 테이블의 id를 참조합니다.
INSERT INTO report (reporter_id, reported_member_id, room_id, reason, report_date, processed)
VALUES (2, 1, 1, 'UNHEALTHY', '2025-04-18 10:00:00', false),
       (3, 5, 2, 'UNHEALTHY', '2025-04-19 11:00:00',
        false),                                               -- Member 3이 Member 5를 Room 2에서 신고 (사유: 욕설)
       (6, 9, 5, 'ABUSE', '2025-04-19 14:20:00',
        false),                                               -- Member 6이 Member 9를 Room 5에서 신고 (사유: 스팸/광고)
       (10, 1, 10, 'ABUSE', '2025-04-20 09:00:00',
        false),                                               -- Member 10이 Member 1(방장)을 Room 10에서 신고 (사유: 약속 불이행)
       (15, 18, 14, 'ADVERTISING', '2025-04-20 15:35:00',
        false),                                               -- Member 15가 Member 18을 Room 14에서 신고 (사유: 부적절한 내용)
       (20, 11, 20, 'ADVERTISING', '2025-04-21 10:10:00',
        true),                                                -- Member 20이 Member 11을 Room 20에서 신고 (처리 완료)
       (2, 21, 25, 'SPLASH', '2025-04-21 18:00:00', false),   -- Member 2가 Member 22를 Room 25에서 신고
       (7, 13, 30, 'SPLASH', '2025-04-22 08:45:00', false),   -- Member 7이 Member 13을 Room 30에서 신고
       (12, 19, 8, 'POLITICS', '2025-04-22 13:00:00', false), -- Member 12가 Member 19를 Room 8에서 신고
       (1, 14, 17, 'IMPERSONATION', '2025-04-23 11:50:00',
        false),                                               -- Member 1이 Member 14를 Room 17에서 신고
       (16, 4, 28, 'ILLEGAL', '2025-04-23 16:00:00', true);
-- Member 16이 Member 4를 Room 28에서 신고 (처리 완료)


-- RoomParticipant 테이블 더미 데이터 추가
-- 주의: id 컬럼은 자동 증가(auto-increment)되므로 INSERT 문에 포함하지 않습니다.
-- boolean 타입(is_host)의 값은 DB에 따라 true/false 또는 1/0으로 입력해야 할 수 있습니다. (여기서는 true/false 사용)
-- 1. 모든 방에 대한 호스트(Host) 정보 추가
-- Room 테이블 생성 시 사용된 host_id를 기반으로 합니다.
INSERT INTO room_participant (room_id, member_id, participant_role, is_host)
VALUES (1, 1, 'HOST', true),   -- Room 1, Host 1
       (2, 2, 'HOST', true),   -- Room 2, Host 3
       (3, 3, 'HOST', true),   -- Room 3, Host 4
       (4, 4, 'HOST', true),   -- Room 4, Host 5
       (5, 5, 'HOST', true),   -- Room 5, Host 6
       (6, 6, 'HOST', true),   -- Room 6, Host 7
       (7, 7, 'HOST', true),   -- Room 7, Host 8
       (8, 8, 'HOST', true),   -- Room 8, Host 9
       (9, 9, 'HOST', true),   -- Room 9, Host 10
       (10, 10, 'HOST', true), -- Room 10, Host 11
       (11, 11, 'HOST', true), -- Room 11, Host 12
       (12, 12, 'HOST', true), -- Room 12, Host 13
       (13, 13, 'HOST', true), -- Room 13, Host 14
       (14, 14, 'HOST', true), -- Room 14, Host 15
       (15, 15, 'HOST', true), -- Room 15, Host 16
       (16, 16, 'HOST', true), -- Room 16, Host 17
       (17, 17, 'HOST', true), -- Room 17, Host 18
       (18, 18, 'HOST', true), -- Room 18, Host 19
       (19, 19, 'HOST', true), -- Room 19, Host 20
       (20, 20, 'HOST', true), -- Room 20, Host 21
       (21, 21, 'HOST', true), -- Room 21, Host 22
       (22, 1, 'HOST', true),  -- Room 22, Host 1
       (23, 2, 'HOST', true),  -- Room 23, Host 2
       (24, 3, 'HOST', true),  -- Room 24, Host 3
       (25, 4, 'HOST', true),  -- Room 25, Host 4
       (26, 5, 'HOST', true),  -- Room 26, Host 5
       (27, 6, 'HOST', true),  -- Room 27, Host 6
       (28, 7, 'HOST', true),  -- Room 28, Host 7
       (29, 8, 'HOST', true),  -- Room 29, Host 8
       (30, 9, 'HOST', true),  -- Room 30, Host 9
       (31, 10, 'HOST', true);
-- Room 31, Host 10

-- 2. 일부 방에 대한 참여자(Participant) 정보 추가 (예시)
INSERT INTO room_participant (room_id, member_id, participant_role, is_host)
VALUES
    -- Room 1 (Host: 1) 에 참여자 추가
    (1, 2, 'GUEST', false),
    (1, 4, 'GUEST', false),
    -- Room 2 (Host: 3) 에 참여자 추가
    (2, 5, 'GUEST', false),
    -- Room 5 (Host: 6, max_GUESTs: 6) 에 참여자 추가
    (5, 1, 'GUEST', false),
    (5, 8, 'GUEST', false),
    (5, 11, 'GUEST', false),
    (5, 15, 'GUEST', false),
    -- Room 10 (Host: 11, max_GUESTs: 3) 에 참여자 추가
    (10, 19, 'GUEST', false),
    (10, 20, 'GUEST', false),
    -- Room 14 (Host: 15, max_GUESTs: 4) 에 참여자 추가
    (14, 21, 'GUEST', false),
    (14, 21, 'GUEST', false),
    (14, 3, 'GUEST', false),
    -- Room 20 (Host: 21, max_GUESTs: 5) 에 참여자 추가
    (20, 7, 'GUEST', false),
    (20, 12, 'GUEST', false),
    (20, 18, 'GUEST', false),
    -- Room 29 (Host: 8, max_GUESTs: 6, 새내기방) 에 참여자 추가
    (29, 5, 'GUEST', false),  -- Member 5 (정페스타, 24학번)
    (29, 16, 'GUEST', false), -- Member 16 (황지리, 24학번)
    (29, 1, 'GUEST', false); -- Member 1 (김페스타, 21학번인데 그냥 참여)