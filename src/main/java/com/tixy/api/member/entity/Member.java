package com.tixy.api.member.entity;

import com.tixy.api.member.enums.MemberRole;
import com.tixy.core.entity.BaseEntity;
import com.tixy.core.exception.order.OrderException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.tixy.core.exception.order.OrderErrorCode.WALLET_ADDRESS_NO_EXIST;

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

    private String walletAddress;

    private Long point;

    @Builder
    private Member(String email, String password, String name, String phone, MemberRole role, Long point) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role == null ? MemberRole.USER : role;
        this.point = point;
    }

    public void checkMemberWallet(){
        if(this.getWalletAddress() == null || this.getWalletAddress().isBlank()){ // 지갑 주소 없으면 주문 XX
            throw new OrderException(WALLET_ADDRESS_NO_EXIST);
        }
    }

    public void addPoint(Long point){
        this.point += point;
    }
}