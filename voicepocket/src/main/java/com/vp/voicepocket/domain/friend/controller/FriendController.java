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
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Friend")
@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class FriendController {

    private final FriendService friendService;

    @Parameter(
        name = "X-AUTH-TOKEN",
        description = "로그인 성공 후 AccessToken",
        required = true,
        schema = @Schema(type = "string"),
        in = ParameterIn.HEADER)
    @Operation(summary = "친구 요청", description = "친구 요청을 합니다.")
    @PostMapping("/friend")
    public SingleResult<FriendResponseDto> friendRequest(
        @RequestHeader("X-AUTH-TOKEN") String accessToken,
        @Parameter(description = "친구 요청 DTO", required = true)
        @RequestBody @Valid FriendRequestDto friendRequestDto) {
        FriendResponseDto friendResponseDto = friendService.requestFriend(
            friendRequestDto.getEmail(), accessToken);
        return ResponseFactory.createSingleResult(friendResponseDto);
    }

    @Parameter(
        name = "X-AUTH-TOKEN",
        description = "로그인 성공 후 AccessToken",
        required = true,
        schema = @Schema(type = "string"),
        in = ParameterIn.HEADER)
    @Operation(summary = "친구 요청 리스트 확인", description = "나에게 온 친구 요청을 확인합니다.")
    @GetMapping("/friend/requests")
    public ListResult<FriendResponseDto> checkRequest(
        @RequestHeader("X-AUTH-TOKEN") String accessToken) {
        return ResponseFactory.createListResult(friendService.checkRequest(accessToken));
    }

    @Parameter(
        name = "X-AUTH-TOKEN",
        description = "로그인 성공 후 AccessToken",
        required = true,
        schema = @Schema(type = "string"),
        in = ParameterIn.HEADER)
    @Operation(summary = "친구 리스트 조회", description = "내 친구 리스트를 조회합니다.")
    @GetMapping("/friend")
    public ListResult<FriendResponseDto> checkResponse(
        @RequestHeader("X-AUTH-TOKEN") String accessToken) {
        return ResponseFactory.createListResult(friendService.checkResponse(accessToken));
    }

    @Parameter(
        name = "X-AUTH-TOKEN",
        description = "로그인 성공 후 AccessToken",
        required = true,
        schema = @Schema(type = "string"),
        in = ParameterIn.HEADER)
    @Operation(summary = "친구 요청 취소", description = "친구 요청을 취소합니다.")
    @DeleteMapping("/friend")
    public CommonResult deleteRequest(
        @RequestHeader("X-AUTH-TOKEN") String accessToken,
        @Parameter(description = "친구 요청 DTO", required = true)
        @RequestBody FriendRequestDto friendRequestDto) {
        friendService.delete(friendRequestDto, accessToken);
        return ResponseFactory.createSuccessResult();
    }

    @Parameter(
        name = "X-AUTH-TOKEN",
        description = "로그인 성공 후 AccessToken",
        required = true,
        schema = @Schema(type = "string"),
        in = ParameterIn.HEADER)
    @Operation(summary = "친구 요청 관리", description = "친구 요청을 수락하거나 거절합니다.")
    @PutMapping("/friend/request/{Status}")
    public CommonResult handlingRequest(
        @RequestHeader("X-AUTH-TOKEN") String accessToken,
        @Parameter(description = "친구 요청 DTO", required = true)
        @RequestBody FriendRequestDto friendRequestDto,
        @PathVariable(name = "Status") Status status) {
        friendService.update(friendRequestDto.getEmail(), accessToken, status);
        return ResponseFactory.createSuccessResult();
    }

}
