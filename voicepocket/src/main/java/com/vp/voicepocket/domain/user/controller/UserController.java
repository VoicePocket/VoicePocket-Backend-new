package com.vp.voicepocket.domain.user.controller;


import com.vp.voicepocket.domain.user.dto.request.UserRequestDto;
import com.vp.voicepocket.domain.user.dto.response.UserResponseDto;
import com.vp.voicepocket.domain.user.entity.vo.Email;
import com.vp.voicepocket.domain.user.service.UserService;
import com.vp.voicepocket.global.common.response.ResponseFactory;
import com.vp.voicepocket.global.common.response.model.ListResult;
import com.vp.voicepocket.global.common.response.model.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원 단건 검색", description = "userId로 회원을 조회합니다.")
    @GetMapping("/user/id/{userId}")
    public SingleResult<UserResponseDto> findUserById(
        @Parameter(description = "회원 ID", required = true) @PathVariable Long userId,
        @Parameter(description = "언어", example = "ko") @RequestParam String lang) {
        return ResponseFactory.createSingleResult(userService.findUserById(userId));
    }

    @Operation(summary = "회원 단건 검색 (이메일)", description = "이메일로 회원을 조회합니다.")
    @GetMapping("/user/email/{email}")
    public SingleResult<UserResponseDto> findUserByEmail(
        @Parameter(description = "회원 이메일", required = true) @PathVariable String email,
        @Parameter(description = "언어", example = "ko") @RequestParam String lang) {
        return ResponseFactory.createSingleResult(userService.findUserByEmail(Email.from(email)));
    }

    @Operation(summary = "회원 목록 조회", description = "모든 회원을 조회합니다.")
    @GetMapping("/users")
    public ListResult<UserResponseDto> findAllUser() {
        return ResponseFactory.createListResult(userService.findAllUser());
    }

    @PutMapping("/user")
    public SingleResult<Long> update(
        @Parameter(description = "회원 ID", required = true) @RequestParam Long userId,
        @Parameter(description = "회원 이름", required = true) @RequestParam String nickName) {
        UserRequestDto userRequestDto = UserRequestDto.builder().nickName(nickName).build();
        return ResponseFactory.createSingleResult(userService.updateUserData(userId, userRequestDto));
    }
}
