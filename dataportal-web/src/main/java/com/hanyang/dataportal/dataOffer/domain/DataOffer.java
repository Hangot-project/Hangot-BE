package com.hanyang.dataportal.dataOffer.domain;


import com.hanyang.dataportal.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Builder
@Getter@NoArgsConstructor
@AllArgsConstructor

public class DataOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;
    private String name;
    private String email;  // 이메일주소
    @Lob
    private String requestContent;
    private String dataName;
    private String organizationName;
    @Lob
    private String dataContent;
    private String purpose;
    @Lob
    private String purposeContent;

    private LocalDate date;

    public void setAdmin(User user) {
        this.user = user;
    }
}
