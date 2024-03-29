package com.vp.voicepocket.domain.token.repository;


import com.vp.voicepocket.domain.token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

}

