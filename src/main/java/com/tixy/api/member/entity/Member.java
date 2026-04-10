package com.tixy.api.member.entity;

import com.tixy.api.member.enums.MemberRole;
import com.tixy.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// @AllArgsConstructor 지양: 필드 순서 변경 시 위험
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 100) // OAuth2 로그인 시 비밀번호가 없을 수 있으므로 nullable = true (기본값)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING) // Enum은 반드시 STRING으로! (ORDINAL 금지: 순서 바뀌면 DB 꼬임)
    @Column(nullable = false)
    private MemberRole role;

    @Builder
    private Member(String email, String password, String name, String phone) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = MemberRole.USER; // 기본값 강제 세팅: 가입 시엔 무조건 USER
    }
}