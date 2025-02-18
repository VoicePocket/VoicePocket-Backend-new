package com.vp.voicepocket.domain.token.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenDto {

    private final String grantType;
    private final String accessToken;
    private final String refreshToken;
    private final Long refreshTokenExpiryDate;

    @Builder
    public TokenDto(String accessToken, String refreshToken, Long refreshTokenExpiryDate) {
        this.grantType = "Bearer";
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiryDate = refreshTokenExpiryDate;
    }

}
