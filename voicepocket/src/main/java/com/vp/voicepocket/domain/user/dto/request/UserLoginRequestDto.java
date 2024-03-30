package com.vp.voicepocket.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "User Login Request", description = "사용자 로그인 입력 모델")
public class UserLoginRequestDto {

    @Schema(title = "email", description = "사용자 이메일", example = "sample@gmail.com")
    @Email
    private String email;

    @Schema(title = "password", description = "사용자 패스워드", example = "sample!")
    @NotBlank
    private String password;

    @Schema(title = "fcmToken", description = "FCM Token", example = "sampleToken")
    @NotBlank
    private String fcmToken;
}
