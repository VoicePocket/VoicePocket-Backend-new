package com.vp.voicepocket.domain.message.controller;

import com.vp.voicepocket.domain.message.dto.TTSRequestDto;
import com.vp.voicepocket.domain.message.service.InputMessageService;
import com.vp.voicepocket.global.common.response.ResponseFactory;
import com.vp.voicepocket.global.common.response.model.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Message")
@RequestMapping("/api")
public class MessageController {

    private final InputMessageService inputMessageService;

    @Operation(summary = "TTS 요청", description = "Text To Speech 서비스를 요청합니다.")
    @PostMapping("/tts/send")
    public CommonResult send(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody @Valid TTSRequestDto ttsRequestDto) {
        inputMessageService.sendMessage(userDetails, ttsRequestDto);
        return ResponseFactory.createSuccessResult();
    }
}
