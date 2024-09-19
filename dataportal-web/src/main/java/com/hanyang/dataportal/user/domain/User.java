package com.hanyang.dataportal.user.domain;

import com.hanyang.dataportal.notice.domain.Notice;
import com.hanyang.dataportal.qna.domain.Answer;
import com.hanyang.dataportal.qna.domain.Question;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Users")
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(unique = true)
    private String email;
    private String password;
    @Column(unique = true)
    private String name;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Builder.Default
    private boolean isActive = true;
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Scrap> scrapList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Notice> noticeList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Download> downloadList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Question> questionList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Answer> answers = new ArrayList<>();

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateName(String name) {
        this.name = name;
    }
    public void withdraw() {
        this.isActive = false;
    }
}
