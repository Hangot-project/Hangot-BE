create table if not exists hanyang.faq
(
    faq_id bigint not null auto_increment,
    category enum ('기타','데이터라이선스','문제해결','서비스이용','회원정보관리'),
    question varchar(255),
    answer varchar(255),
    primary key (faq_id)
);