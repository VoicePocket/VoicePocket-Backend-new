package com.vp.voicepocket.domain.friend.controller;

import com.vp.voicepocket.domain.friend.dto.request.FriendRequestDto;
import com.vp.voicepocket.domain.friend.dto.response.FriendResponseDto;
import com.vp.voicepocket.domain.friend.entity.Status;
import com.vp.voicepocket.domain.friend.service.FriendService;
import com.vp.voicepocket.global.common.response.ResponseFactory;
import com.vp.voicepocket.global.common.response.model.CommonResult;
import com.vp.voicepocket.global.common.response.model.ListResult;
import com.vp.voicepocket.global.common.response.model.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Friend")
@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 요청", description = "친구 요청을 합니다.")
    @PostMapping("/friend")
    public SingleResult<FriendResponseDto> friendRequest(
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
        @Parameter(description = "친구 요청 DTO", required = true) @RequestBody @Valid FriendRequestDto friendRequestDto) {
        FriendResponseDto friendResponseDto = friendService.requestFriend(userDetails, friendRequestDto.getEmail());
        return ResponseFactory.createSingleResult(friendResponseDto);
    }

    @Operation(summary = "친구 요청 리스트 확인", description = "나에게 온 친구 요청을 확인합니다.")
    @GetMapping("/friend/requests")
    public ListResult<FriendResponseDto> checkRequest(
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseFactory.createListResult(friendService.checkRequest(userDetails));
    }

    @Operation(summary = "친구 리스트 조회", description = "내 친구 리스트를 조회합니다.")
    @GetMapping("/friend")
    public ListResult<FriendResponseDto> checkResponse(
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseFactory.createListResult(friendService.checkResponse(userDetails));
    }

    @Operation(summary = "친구 요청 취소", description = "친구 요청을 취소합니다.")
    @DeleteMapping("/friend")
    public CommonResult deleteRequest(
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
        @Parameter(description = "친구 요청 DTO", required = true)
        @RequestBody FriendRequestDto friendRequestDto) {
        friendService.delete(userDetails, friendRequestDto);
        return ResponseFactory.createSuccessResult();
    }

    @Operation(summary = "친구 요청 관리", description = "친구 요청을 수락하거나 거절합니다.")
    @PutMapping("/friend/request/{Status}")
    public CommonResult handlingRequest(
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
        @Parameter(description = "친구 요청 DTO", required = true)
        @RequestBody FriendRequestDto friendRequestDto,
        @PathVariable(name = "Status") Status status) {
        friendService.update(userDetails, friendRequestDto.getEmail(), status);
        return ResponseFactory.createSuccessResult();
    }

}
