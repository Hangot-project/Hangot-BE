package com.hanyang.api.notice.dto.res;

import com.hanyang.api.notice.domain.Notice;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

@Data
public class ResNoticeListDto {
    private Integer totalPage;
    private Long totalElement;
    private List<SimpleNotice> data;

    public ResNoticeListDto(Page<Notice> notices) {
        this.totalPage = notices.getTotalPages();
        this.totalElement = notices.getTotalElements();
        this.data = notices.getContent().stream().map(SimpleNotice::new).toList();
    }

    @Data
    public static class SimpleNotice {
        private Long noticeId;
        private String title;
        private LocalDate createDate;
        private Integer view;
        private String adminName;

        public SimpleNotice(Notice notice) {
            this.noticeId = notice.getNoticeId();
            this.title = notice.getTitle();
            this.createDate = notice.getCreateDate();
            this.view = notice.getView();
            this.adminName = notice.getAdmin().getName();
        }
    }

}
