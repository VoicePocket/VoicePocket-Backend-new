package com.vp.voicepocket.domain.user.service;


import com.vp.voicepocket.domain.firebase.entity.FCMUserToken;
import com.vp.voicepocket.domain.firebase.repository.FCMRepository;
import com.vp.voicepocket.domain.token.config.JwtProvider;
import com.vp.voicepocket.domain.token.dto.TokenDto;
import com.vp.voicepocket.domain.token.entity.RefreshToken;
import com.vp.voicepocket.domain.token.exception.CRefreshTokenException;
import com.vp.voicepocket.domain.token.repository.RefreshTokenRepository;
import com.vp.voicepocket.domain.user.dto.request.UserLoginRequestDto;
import com.vp.voicepocket.domain.user.dto.request.UserSignupRequestDto;
import com.vp.voicepocket.domain.user.entity.User;
import com.vp.voicepocket.domain.user.exception.CEmailLoginFailedException;
import com.vp.voicepocket.domain.user.exception.CEmailSignUpFailedException;
import com.vp.voicepocket.domain.user.exception.CUserNotFoundException;
import com.vp.voicepocket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SignService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final FCMRepository fcmRepository;

    @Transactional
    public Long signup(UserSignupRequestDto userSignupRequestDto) {
        userRepository.findByEmail(userSignupRequestDto.getEmail()).ifPresent(user -> {
            throw new CEmailSignUpFailedException();
        });
        return userRepository.save(userSignupRequestDto.toEntity(passwordEncoder)).getId();
    }

    @Transactional
    public TokenDto login(UserLoginRequestDto userLoginRequestDto) {
        // 회원이 존재하는지 확인
        User user = userRepository.findByEmail(userLoginRequestDto.getEmail())
            .orElseThrow(CEmailLoginFailedException::new);

        // password 일치 여부 확인
        user.verifyPassword(passwordEncoder, userLoginRequestDto.getPassword());

        // token 발급
        TokenDto tokenDto = jwtProvider.generateTokens(user.getId(), user.getRole().toString());

        // RefreshToken 관리
        refreshTokenRepository.findById(user.getId()).ifPresentOrElse(
            refreshToken -> refreshToken.updateToken(tokenDto.getRefreshToken(),
                tokenDto.getRefreshTokenExpiryDate()),
            () -> refreshTokenRepository.save(
                RefreshToken.builder().id(user.getId()).refreshToken(tokenDto.getRefreshToken())
                    .expiryDate(tokenDto.getRefreshTokenExpiryDate()).build()));

        // FCM TOKEN 관리
        fcmRepository.findByUserId(user).ifPresentOrElse(
            fcmUserToken -> fcmUserToken.update(userLoginRequestDto.getFcmToken()),
            () -> fcmRepository.save(FCMUserToken.builder().userId(user).FireBaseToken(
                userLoginRequestDto.getFcmToken()).build()));
        return tokenDto;
    }

    @Transactional
    public TokenDto reissue(String accessToken, String refreshToken) {
        accessToken = validateAccessToken(accessToken);
        Long userId = Long.parseLong(
            jwtProvider.parseAccessTokenWithOutExpiration(accessToken).getSubject());
        // RefreshTokenRepository 에서 Username (pk) 가져오기
        var token = refreshTokenRepository.findById(userId)
            .orElseThrow(CRefreshTokenException::new);
        token.validateRefreshToken(refreshToken);
        // user pk로 유저 검색 / repo 에 저장된 Refresh Token 가져오기
        User user = userRepository.findById(userId).orElseThrow(CUserNotFoundException::new);
        // AccessToken, RefreshToken 토큰 재발급, 리프레쉬 토큰 저장
        return jwtProvider.reissueAccessToken(user.getId(), user.getRole().toString(), refreshToken,
            token.getExpiryDate());
    }

    private String validateAccessToken(String accessToken) {
        if (accessToken == null || !accessToken.contains("Bearer")) {
            throw new IllegalArgumentException("Invalid Token");
        }
        // "Bearer " 제거
        return accessToken.substring(7);
    }
}
