package com.vp.voicepocket.domain.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "Input Message 모델", description = "TTS 요청을 받을 Message 모델")
public class TTSRequestDto {

    @Schema(title = "Message type", description = "메시지 요청에 따른 타입", example = "ETL")
    @NotBlank
    private String type;

    @Schema(title = "Request UUID", description = "TTS 요청에 대한 UUID", example = "550k8400-e29b-41d4-a716-446655440001")
    @NotBlank
    private String uuid;

    @Schema(title = "Requested User Email", description = "음성 합성 요청을 받은 사용자의 이메일", example = "ssh@gmail.com")
    @Email
    private String requestTo;

    @Schema(title = "Text", description = "합성을 원하는 문장", example = "테스트 문장입니다.")
    @NotBlank
    private String text;
}
