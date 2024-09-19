ALTER TABLE hanyang.question
ADD COLUMN image_url tinytext;

ALTER TABLE hanyang.notice
ADD COLUMN label enum ('일반','업데이트','중요');