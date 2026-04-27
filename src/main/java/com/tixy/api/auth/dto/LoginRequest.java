package com.tixy.api.auth.dto;

import com.tixy.core.annotation.Masking;

public record LoginRequest(
        String email,

        @Masking
        String password
) {
}