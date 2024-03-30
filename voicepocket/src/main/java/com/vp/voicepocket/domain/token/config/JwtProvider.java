package com.vp.voicepocket.domain.token.config;

import com.vp.voicepocket.domain.token.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

    private static final String ROLE = "role";
    private static final Long ACCESS_TOKEN_VALID_MILLISECOND = 60 * 60 * 1000L; // 1 hour
    private static final Long REFRESH_TOKEN_VALID_MILLISECOND = 14 * 24 * 60 * 60 * 1000L; // 14 day

    private final String secretKey;
    private final String issuer;
    private final JwtParser jwtParser;

    public JwtProvider(@Value("${spring.jwt.secret}") String secretKey, @Value("${spring.jwt.issuer}") String issuer) {
        this.secretKey = TextCodec.BASE64URL.encode(secretKey.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
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
