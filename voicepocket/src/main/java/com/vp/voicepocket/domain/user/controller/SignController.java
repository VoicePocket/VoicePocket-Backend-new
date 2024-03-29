package com.vp.voicepocket.domain.user.controller;


import com.vp.voicepocket.domain.token.dto.TokenDto;
import com.vp.voicepocket.domain.token.dto.TokenRequestDto;
import com.vp.voicepocket.domain.user.dto.request.UserLoginRequestDto;
import com.vp.voicepocket.domain.user.dto.request.UserSignupRequestDto;
import com.vp.voicepocket.domain.user.service.SignService;
import com.vp.voicepocket.global.common.response.ResponseFactory;
import com.vp.voicepocket.global.common.response.model.SingleResult;
import com.vp.voicepocket.global.common.response.service.ResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@Tag(name = "SignUp/LogIn")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
public class SignController {
    private final SignService signService;
    private final ResponseService responseService;

    @Operation(summary = "회원 가입", description = "회원 가입을 합니다.")
    @PostMapping("/signup")
    public SingleResult<Long> signup(
            @Parameter(description = "회원 가입 요청 DTO", required = true)
            @RequestBody @Valid UserSignupRequestDto userSignupRequestDto) {
        Long signupId = signService.signup(userSignupRequestDto);
        return responseService.getSingleResult(signupId);
    }

    @Operation(summary = "로그인", description = "이메일로 로그인을 합니다.")
    @PostMapping("/login")
    public SingleResult<TokenDto> login(
            @Parameter(description = "로그인 요청 DTO", required = true)
            @RequestBody @Valid UserLoginRequestDto userLoginRequestDto) {
        TokenDto tokenDto = signService.login(userLoginRequestDto);
        return ResponseFactory.createSingleResult(tokenDto);
    }

    @Operation(
            summary = "액세스, 리프레시 토큰 재발급",
            description = "엑세스 토큰 만료시 회원 검증 후 리프레쉬 토큰을 검증해서 액세스 토큰과 리프레시 토큰을 재발급합니다.")
    @PostMapping("/reissue")
    public SingleResult<TokenDto> reissue(
            @Parameter(description= "토큰 재발급 요청 DTO", required = true)
            @RequestBody TokenRequestDto tokenRequestDto) {
        return responseService.getSingleResult(signService.reissue(tokenRequestDto));
    }
}
