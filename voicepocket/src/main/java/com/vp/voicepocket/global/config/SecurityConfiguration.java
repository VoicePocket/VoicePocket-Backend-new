package com.vp.voicepocket.global.config;

import com.vp.voicepocket.domain.token.config.JwtAccessDeniedHandler;
import com.vp.voicepocket.domain.token.config.JwtAuthenticationEntryPoint;
import com.vp.voicepocket.domain.token.config.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .httpBasic().disable() // 기본설정은 비 인증시 로그인 폼 화면으로 리다이렉트 되는데 RestApi이므로 disable 함
            .csrf().disable() // rest api이므로 상태를 저장하지 않으니 csrf 보안을 설정하지 않아도된다.
            .formLogin().disable()
            .logout().disable()
            .sessionManagement().sessionCreationPolicy(
                SessionCreationPolicy.STATELESS) // Jwt으로 인증하므로 세션이 필요지 않으므로 생성 안한다.

            .and()
            .authorizeRequests() // URL 별 권한 관리를 설정하는 옵션의 시작점, antMathcers를 작성하기 위해서는 먼저 선언되어야 한다.
            .antMatchers("/api/v1/auth/**").permitAll()
            .antMatchers("/api/v1/admin/**").hasRole("ADMIN")
            .anyRequest().hasRole("USER")

            .and()
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

            .and()
            .addFilterBefore(
                jwtAuthenticationFilter, BasicAuthenticationFilter.class
            )
            .build();
    }

    @Bean
    public WebSecurityCustomizer ignoringWebSecurityCustomizer() {
        return web -> web.ignoring()
            .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**",
                "/favicon.ico", "/error");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
