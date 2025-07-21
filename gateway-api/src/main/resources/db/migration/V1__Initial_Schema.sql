-- Initial schema for HY-DATA-BE Gateway API

-- Dataset 테이블
CREATE TABLE dataset (
    dataset_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    description LONGTEXT,
    organization VARCHAR(255),
    license VARCHAR(255),
    created_date DATE,
    updated_date DATE,
    view INT DEFAULT 0,
    scrap INT DEFAULT 0,
    resource_name VARCHAR(255),
    resource_url LONGTEXT,
    type VARCHAR(100),
    source_url VARCHAR(500),
    source VARCHAR(255),
    PRIMARY KEY (dataset_id)
);

-- Tag 테이블
CREATE TABLE tag (
    tag_id BIGINT NOT NULL AUTO_INCREMENT,
    tag VARCHAR(255),
    dataset_id BIGINT,
    PRIMARY KEY (tag_id),
    FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ON DELETE CASCADE
);

-- User 테이블
CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    provider_id VARCHAR(255) UNIQUE NOT NULL,
    provider VARCHAR(255),
    role VARCHAR(50) DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (user_id)
);

-- Scrap 테이블
CREATE TABLE scrap (
    scrap_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    dataset_id BIGINT,
    PRIMARY KEY (scrap_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_dataset (user_id, dataset_id)
);

-- Full-text index 추가
ALTER TABLE dataset ADD FULLTEXT(title);

CREATE INDEX idx_dataset_source_url ON dataset(source_url);
CREATE INDEX idx_dataset_organization ON dataset(organization);
CREATE INDEX idx_dataset_view ON dataset(view);
CREATE INDEX idx_dataset_scrap ON dataset(scrap);
CREATE INDEX idx_dataset_source ON dataset(source);
CREATE INDEX idx_dataset_type ON dataset(type);
CREATE INDEX idx_tag_tag ON tag(tag);