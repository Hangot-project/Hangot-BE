create table if not exists hanyang.data_request
(
    data_request_id bigint not null auto_increment,
    organization    varchar(255) null,
    purpose varchar(255),
    title varchar(255),
    content tinytext,
    primary key (data_request_id)
    );

create table if not exists hanyang.dataset
(
    created_date date,
    download integer,
    update_date date,
    view integer,
    dataset_id bigint not null auto_increment,
    organization    varchar(255),
    title varchar(255),
    description tinytext,
    primary key (dataset_id)
    );

create table if not exists hanyang.dataset_theme
(
    dataset_id bigint,
    dataset_theme_id bigint not null auto_increment,
    theme varchar(255),
    primary key (dataset_theme_id),
    foreign key (dataset_id) references hanyang.dataset (dataset_id)
    );

create table if not exists hanyang.resource
(
    dataset_id bigint,
    resource_id bigint not null auto_increment,
    resource_name varchar(255),
    type enum ('csv','docx','json','pdf','xls','xlsx'),
    resource_url tinytext,
    primary key (resource_id),
    foreign key (dataset_id) references hanyang.dataset (dataset_id)
    );

create table if not exists hanyang.users
(
    is_active bit not null,
    user_id bigint not null auto_increment,
    email varchar(255),
    name varchar(255),
    password varchar(255),
    role enum ('ROLE_ADMIN','ROLE_USER'),
    primary key (user_id),
    unique (name),
    unique (email)
    );

create table if not exists hanyang.download
(
    dataset_id bigint,
    download_id bigint not null auto_increment,
    user_id bigint,
    primary key (download_id),
    foreign key (dataset_id) references hanyang.dataset (dataset_id),
    foreign key (user_id) references hanyang.users (user_id)
    );

create table if not exists hanyang.notice
(
    create_date date,
    update_date date,
    view integer,
    notice_id bigint not null auto_increment,
    user_id bigint,
    content varchar(255),
    title varchar(255),
    primary key (notice_id),
    foreign key (user_id) references hanyang.users (user_id)
    );

create table if not exists hanyang.question
(
    create_date date,
    view integer,
    question_id bigint not null auto_increment,
    user_id bigint,
    answer_status enum ('대기','완료'),
    category enum ('기타','데이터라이선스','문제해결','서비스이용','회원정보관리'),
    title varchar(255),
    content tinytext,
    primary key (question_id),
    foreign key (user_id) references hanyang.users (user_id)
    );

create table if not exists hanyang.answer
(
    creat_date date,
    answer_id bigint not null auto_increment,
    question_id bigint,
    user_id bigint,
    title varchar(255),
    content tinytext,
    primary key (answer_id),
    foreign key (user_id) references hanyang.users (user_id),
    foreign key (question_id) references hanyang.question (question_id)
    );

create table if not exists hanyang.scrap
(
    dataset_id bigint,
    scrap_id bigint not null auto_increment,
    user_id bigint,
    primary key (scrap_id),
    foreign key (dataset_id) references hanyang.dataset (dataset_id),
    foreign key (user_id) references hanyang.users (user_id)
    );

