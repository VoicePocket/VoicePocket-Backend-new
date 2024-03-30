package com.vp.voicepocket.domain.token.config;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.vp.voicepocket.domain.token.exception.CAuthenticationEntryPointException;
import com.vp.voicepocket.domain.user.entity.enums.UserRole;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = validateAccessToken(request.getHeader(AUTHORIZATION));
            var claims = jwtProvider.parseAccessToken(accessToken);
            validateClaims(claims);
            var authentication = generateAuthenticationToken(claims.getSubject());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }
        filterChain.doFilter(request, response);
    }

    private String validateAccessToken(String accessToken) {
        if (accessToken == null || !accessToken.contains("Bearer")) {
            throw new IllegalArgumentException("Invalid Token");
        }
        // "Bearer " 제거
        return accessToken.substring(7);
    }

    private void validateClaims(Claims claims) {
        var role = claims.get("role", String.class);
        var userId = claims.getSubject();
        if (role == null || !UserRole.isUserRole(role) || userId == null) {
            throw new CAuthenticationEntryPointException();
        }
    }

    private Authentication generateAuthenticationToken(String userId) {
        var userDetails = userDetailsService.loadUserByUsername(userId);
        return new UsernamePasswordAuthenticationToken(userDetails, "",
            userDetails.getAuthorities());
    }

}
