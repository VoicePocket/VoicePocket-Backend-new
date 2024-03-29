package com.vp.voicepocket.domain.token.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {  // 추후 expire 시간과 비교하여 만료시켜주기 위해 BaseEntity를 상속하여 time 정보를 받아옴

    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private Long id;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private Long expiryDate;

    @Builder
    private RefreshToken(Long id, String refreshToken, Long expiryDate) {
        this.id = validateId(id);
        this.value = validateToken(refreshToken);
        this.expiryDate = validateExpiryDate(expiryDate);
    }

    public void updateToken(String refreshToken, Long expiryDate) {
        this.value = validateToken(refreshToken);
        this.expiryDate = validateExpiryDate(expiryDate);
    }

    private Long validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("RefreshToken id is null");
        }
        return id;
    }

    private String validateToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("RefreshToken value is null");
        }
        return refreshToken;
    }

    private Long validateExpiryDate(Long expiryDate) {
        if (expiryDate == null || expiryDate <= 0) {
            throw new IllegalArgumentException("RefreshToken expiryDate is null");
        }
        return expiryDate;
    }
}