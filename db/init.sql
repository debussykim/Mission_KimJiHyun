DROP
DATABASE IF EXISTS gram__dev;
CREATE
DATABASE gram__dev;
USE
gram__dev;

DROP
DATABASE IF EXISTS gram__test;
CREATE
DATABASE gram__test;
USE
gram__test;

# 어떠한 회원이 특정 회원에 대해서 이미 호감표시를 했는지 검사 (질의가 하나라도 있다면 이미 호감을 표시한 경우)
SELECT *
FROM likeable_person
WHERE from_insta_member_id = 1
  AND to_insta_member_id = 2;

# 어떠한 회원이 호감표시를 총 몇 번 했는지 검사
SELECT COUNT(*)
FROM likeable_person
WHERE from_insta_member_id = 1;

# 사용자는 호감표시를 했지만 케이스 6에 해당되므로 실제로는 수정이 일어난다.
UPDATE likeable_person
SET modify_date = NOW(),
    attractive_type_code = 2
WHERE id = 5;