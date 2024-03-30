package com.vp.voicepocket.domain.user.entity;

import com.vp.voicepocket.domain.user.entity.enums.UserRole;
import com.vp.voicepocket.domain.user.entity.vo.Email;
import com.vp.voicepocket.domain.user.exception.CEmailLoginFailedException;
import com.vp.voicepocket.global.common.BaseEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Users")
@Entity
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    // Json 결과로 출력하지 않을 값들은 애노테이션 선언으로 read하지 못하게 함
    @Column(length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "role", nullable = false, updatable = false)
    private UserRole role;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private User(String password, Email email, String name, String nickname, UserRole role) {
        this.password = validatePassword(password);
        this.email = email.getValue();
        this.name = validateName(name);
        this.nickname = validateNickname(nickname);
        this.role = validateRole(role);
    }

    public void updateNickName(String nickName) {
        this.nickname = nickName;
    }

    public void deleteUser() {
        this.deletedAt = LocalDateTime.now();
    }

    public void verifyPassword(PasswordEncoder passwordEncoder, String password) {
        if (!passwordEncoder.matches(password, this.password)) {
            throw new CEmailLoginFailedException();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public String getUsername() {
        return id.toString();
    }

    /**
     * @return 계정 만료 여부 -> 계정 만료 로직 존재 X -> true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * @return 계정 잠김 여부 -> 계정 잠김 로직 존재 X -> true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * @return 패스워드 만료 여부 -> 패스워드 만료 로직 존재 X -> true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


    /**
     * @return 계정 활성화 여부
     */
    @Override
    public boolean isEnabled() {
        return deletedAt == null;
    }

    private String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
        }
        return password;
    }

    private String validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수 입력값입니다.");
        }
        return nickname;
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수 입력값입니다.");
        }
        return name;
    }

    private UserRole validateRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("사용자 권한은 필수 입력값입니다.");
        }
        return role;
    }
}
