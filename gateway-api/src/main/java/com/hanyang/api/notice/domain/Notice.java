package com.hanyang.api.notice.domain;

import com.hanyang.api.notice.dto.req.ReqNoticeDto;
import com.hanyang.api.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;
    private String title;
    private String content;
    private LocalDate createDate;
    private LocalDate updateDate;
    private Integer view;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User admin;
    @Enumerated(EnumType.STRING)
    private NoticeLabel label;
    @PrePersist
    public void onPrePersist() {
        createDate = LocalDate.now();
    }
    @PreUpdate
    public void onPreUpdate() {
        updateDate = LocalDate.now();
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public void updateNotice(ReqNoticeDto reqNoticeDto){
        this.content = reqNoticeDto.getContent();
        this.title = reqNoticeDto.getTitle();
    }

    public void updateView() {
        this.view++;
    }
}