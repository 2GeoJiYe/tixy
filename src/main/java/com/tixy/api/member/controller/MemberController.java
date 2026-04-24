package com.tixy.api.member.controller;

import com.tixy.api.member.dto.MemberWalletUpdateRequest;
import com.tixy.api.member.service.MemberService;
import com.tixy.core.dto.ApiResponse;
import com.tixy.core.security.dto.LoginUserInfoDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/tixy/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/v1/wallets")
    public ResponseEntity<ApiResponse<Void>> updateMemberWallet(
            @AuthenticationPrincipal LoginUserInfoDto userInfo,
            @RequestBody @Valid MemberWalletUpdateRequest memberWalletUpdateRequest) {

        memberService.updateWallet(userInfo.id(), memberWalletUpdateRequest.walletAddress());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/v1/payment")
    public ResponseEntity<ApiResponse<Void>> getMemberPayment(
            @AuthenticationPrincipal LoginUserInfoDto userInfo) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
