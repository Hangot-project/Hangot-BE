ALTER TABLE hanyang.dataset
ADD COLUMN license VARCHAR(100) default "저작자표시";

ALTER TABLE hanyang.question
ADD COLUMN isOpen TINYINT(1) default 1;