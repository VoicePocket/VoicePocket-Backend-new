package com.vp.voicepocket.global.exception;

import com.vp.voicepocket.domain.firebase.exception.CFCMTokenNotFoundException;
import com.vp.voicepocket.domain.friend.exception.CFriendRequestNotExistException;
import com.vp.voicepocket.domain.friend.exception.CFriendRequestOnGoingException;
import com.vp.voicepocket.domain.token.exception.CAccessDeniedException;
import com.vp.voicepocket.domain.token.exception.CAccessTokenException;
import com.vp.voicepocket.domain.token.exception.CAuthenticationEntryPointException;
import com.vp.voicepocket.domain.token.exception.CExpiredAccessTokenException;
import com.vp.voicepocket.domain.token.exception.CRefreshTokenException;
import com.vp.voicepocket.domain.user.exception.CEmailLoginFailedException;
import com.vp.voicepocket.domain.user.exception.CEmailSignUpFailedException;
import com.vp.voicepocket.domain.user.exception.CUserNotFoundException;
import com.vp.voicepocket.global.common.response.ResponseFactory;
import com.vp.voicepocket.global.common.response.model.CommonResult;
import io.jsonwebtoken.MalformedJwtException;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    private String getMessage(String code) {
        return getMessage(code, null);
    }

    private String getMessage(String code, Object[] args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * 통합 예외
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult defaultException(HttpServletRequest request, Exception e) {
        return ResponseFactory.createFailResult(
        //        (Integer.parseInt(getMessage("unKnown.code")), getMessage("unKnown.msg"));
                (Integer.parseInt(getMessage("unKnown.code"))), e.getMessage());
    }

    /***
     * -1000
     * 유저를 찾지 못했을 때 발생시키는 예외
     */
    @ExceptionHandler(CUserNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult userNotFoundException(
            HttpServletRequest request, CUserNotFoundException e) {
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("userNotFound.code")), getMessage("userNotFound.msg"));
    }

    /***
     * -1001
     * 유저 이메일 로그인 실패 시 발생시키는 예외
     */
    @ExceptionHandler(CEmailLoginFailedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    protected CommonResult emailLoginFailedException(HttpServletRequest request, CEmailLoginFailedException e) {
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("emailLoginFailed.code")), getMessage("emailLoginFailed.msg")
        );
    }

    /***
     * -1002
     * 회원 가입 시 이미 로그인 된 이메일인 경우 발생 시키는 예외
     */
    @ExceptionHandler(CEmailSignUpFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult emailSignupFailedException(HttpServletRequest request, CEmailSignUpFailedException e) {
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("emailSignupFailed.code")), getMessage("emailSignupFailed.msg")
        );
    }

    /** -1003 전달한 Jwt 이 정상적이지 않은 경우 발생 시키는 예외 */
    @ExceptionHandler(CAuthenticationEntryPointException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected CommonResult authenticationEntrypointException(
            HttpServletRequest request, CAuthenticationEntryPointException e) {
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("authenticationEntrypoint.code")),
                getMessage("authenticationEntrypoint.msg"));
    }

    /**
     * -1004
     * 권한이 없는 리소스를 요청한 경우 발생 시키는 예외
     */
    @ExceptionHandler(CAccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected CommonResult accessDeniedException(HttpServletRequest request, CAccessDeniedException e) {
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("accessDenied.code")), getMessage("accessDenied.msg")
        );
    }

    /**
     * -1005
     * refresh token 에러시 발생 시키는 에러
     */
    @ExceptionHandler(CRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected CommonResult refreshTokenException(HttpServletRequest request, CRefreshTokenException e) {
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("refreshTokenInValid.code")), getMessage("refreshTokenInValid.msg")
        );
    }

    /**
     * -1006
     * 액세스 토큰 만료시 발생하는 에러
     */
    @ExceptionHandler(CExpiredAccessTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected CommonResult expiredAccessTokenException(HttpServletRequest request, CExpiredAccessTokenException e) {
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("expiredAccessToken.code")), getMessage("expiredAccessToken.msg")
        );
    }

    /**
     * -1007
     * access token 에러시 발생 시키는 에러
     */
    @ExceptionHandler(CAccessTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected CommonResult accessTokenException(HttpServletRequest request, CAccessTokenException e) {
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("accessTokenInValid.code")), getMessage("accessTokenInValid.msg")
        );
    }

    @ExceptionHandler(MalformedJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected CommonResult malformedJwtException(HttpServletRequest request, MalformedJwtException e){
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("malformedToken.code")), getMessage("malformedToken.msg")
        );
    }
    /***
     * -1010
     * 친구 추가 요청이 존재하지 않는 경우.
     */
    @ExceptionHandler(CFriendRequestNotExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult friendRequestNotExistException(
            HttpServletRequest request, CFriendRequestNotExistException e) {
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("friendRequestNotExist.code")), getMessage("friendRequestNotExist.msg"));
    }

    /***
     * -1011
     * 친구 추가 요청이 진행중인 경우
     */
    @ExceptionHandler(CFriendRequestOnGoingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult friendRequestOnGoingException(
            HttpServletRequest request, CFriendRequestOnGoingException e){
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("friendRequestOnGoing.code")), getMessage("friendRequestOnGoing.msg"));
    }

    /***
     * -1012
     * 유저를 찾지 못했을 때 발생시키는 예외
     */
    @ExceptionHandler(CFCMTokenNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult userNotFoundException(
            HttpServletRequest request, CFCMTokenNotFoundException e) {
        return ResponseFactory.createFailResult(
                Integer.parseInt(getMessage("FCMTokenNotFound.code")), getMessage("FCMTokenNotFound.msg"));
    }
}
