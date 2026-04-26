package com.tixy.api.member.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberWalletUpdateRequest(
        @NotBlank
        String walletAddress
) {
}
