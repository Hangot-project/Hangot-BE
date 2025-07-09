package com.hanyang.dataportal.notice.dto.req;


import com.hanyang.dataportal.notice.domain.Notice;
import com.hanyang.dataportal.notice.domain.NoticeLabel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqNoticeDto {
    private String title;
    private String content;
    private NoticeLabel label;
    public Notice toEntity() {
        return Notice.builder().
                title(title).
                content(content).
                label(label).
                build();
    }
}
