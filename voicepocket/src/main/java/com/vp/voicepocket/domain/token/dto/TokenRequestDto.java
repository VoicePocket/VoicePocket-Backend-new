package com.vp.voicepocket.domain.token.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenRequestDto {

    @Schema(title = "refreshToken", description = "리프레시 토큰", example = "sampleToken")
    @NotBlank
    String refreshToken;

}
