package com.hanyang.dataportal.qna.domain;

import com.hanyang.dataportal.qna.dto.req.ReqAnswerDto;
import com.hanyang.dataportal.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;
    @Lob
    private String content;
    private String title;
    private LocalDate creatDate;
    @Setter
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "questionId")
    private Question question;
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User admin;

    public void update(ReqAnswerDto reqAnswerDto){
        this.content = reqAnswerDto.getContent();
        this.title = reqAnswerDto.getTitle();
    }

    @PrePersist
    public void onPrePersist() {
        creatDate = LocalDate.now();
    }
}

