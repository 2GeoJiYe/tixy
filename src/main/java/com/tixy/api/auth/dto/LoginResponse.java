package com.tixy.api.auth.dto;

public record LoginResponse(
        AuthTokens tokens,
        UserInfoDto user
) {


    public record UserInfoDto(
            Long id,
            String email,
            String name
    ) {

    }
}