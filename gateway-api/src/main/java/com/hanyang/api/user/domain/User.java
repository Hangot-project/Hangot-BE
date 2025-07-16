package com.hanyang.api.user.domain;

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
    @Column(unique = true)
    private String name;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Builder.Default
    private boolean isActive = true;
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Scrap> scrapList = new ArrayList<>();

    public void updateName(String name) {
        this.name = name;
    }
    public void withdraw() {
        this.isActive = false;
    }
}
