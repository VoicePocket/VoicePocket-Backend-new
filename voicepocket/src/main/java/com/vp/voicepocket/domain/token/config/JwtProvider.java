package com.vp.voicepocket.domain.token.config;

import com.vp.voicepocket.domain.token.dto.TokenDto;
import com.vp.voicepocket.domain.token.exception.CAuthenticationEntryPointException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.TextCodec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

    private static final String ROLE = "role";
    private static final Long ACCESS_TOKEN_VALID_MILLISECOND = 60 * 60 * 1000L; // 1 hour
    private static final Long REFRESH_TOKEN_VALID_MILLISECOND = 14 * 24 * 60 * 60 * 1000L; // 14 day

    private final String secretKey;
    private final String issuer;
    private final UserDetailsService userDetailsService;

    private final JwtParser jwtParser;

    public JwtProvider(@Value("${spring.jwt.secret}") String secretKey,
        @Value("${spring.jwt.issuer}") String issuer, UserDetailsService userDetailsService) {
        this.secretKey = TextCodec.BASE64URL.encode(secretKey.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.userDetailsService = userDetailsService;
        this.jwtParser = Jwts.parser().setSigningKey(secretKey);
    }

    // Generate Access, Refresh Token
    public TokenDto generateTokens(Long userId, String role) {
        Date now = new Date();
        var accessToken = generateAccessToken(userId, role, now);
        var refreshTokenExpiryDate = calculateRefreshTokenExpiryDate(now);
        var refreshToken = Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setExpiration(refreshTokenExpiryDate)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();

        return TokenDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .refreshTokenExpiryDate(refreshTokenExpiryDate.getTime())
            .build();
    }

    // reissue
    public TokenDto reissueAccessToken(Long userId, String role, String refreshToken, Long refreshTokenExpiryDate) {
        Date now = new Date();
        var accessToken = generateAccessToken(userId, role, now);
        return TokenDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .refreshTokenExpiryDate(refreshTokenExpiryDate)
            .build();
    }

    public Claims parseAccessToken(String accessToken) {
        return jwtParser.parseClaimsJws(accessToken).getBody();
    }

    public Claims parseAccessTokenWithOutExpiration(String accessToken) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // Jwt 토큰 복호화해서 가져오기
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // Jwt 로 인증정보를 조회
    @Deprecated(forRemoval = true)
    public Authentication getAuthentication(String token) {

        // Jwt 에서 claims 추출
        Claims claims = parseClaims(token);

        // 권한 정보가 없음
        if (claims.get(ROLE) == null) {
            throw new CAuthenticationEntryPointException();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        return new UsernamePasswordAuthenticationToken(userDetails, "",
            userDetails.getAuthorities());
    }

    // jwt 의 유효성 및 만료 일자 확인
    @Deprecated(forRemoval = true)
    public boolean validationToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 Jwt 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("잘못된 토큰입니다.");
        }
        return false;
    }

    private Date calculateAccessTokenExpiryDate(Date now) {
        return new Date(now.getTime() + ACCESS_TOKEN_VALID_MILLISECOND);
    }

    private Date calculateRefreshTokenExpiryDate(Date now) {
        return new Date(now.getTime() + REFRESH_TOKEN_VALID_MILLISECOND);
    }

    private String generateAccessToken(Long userId, String role, Date now) {
        var claims = Jwts.claims(
            Map.of(
                Claims.SUBJECT, userId,
                ROLE, role
            )
        );
        return Jwts.builder().setHeaderParam(Header.TYPE, Header.JWT_TYPE).setClaims(claims)
            .setIssuedAt(now).setIssuer(issuer).setExpiration(calculateAccessTokenExpiryDate(now))
            .signWith(SignatureAlgorithm.HS256, secretKey).compact();
    }
}
