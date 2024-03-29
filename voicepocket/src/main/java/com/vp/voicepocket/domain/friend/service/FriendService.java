package com.vp.voicepocket.domain.friend.service;

import com.vp.voicepocket.domain.friend.dto.request.FriendRequestDto;
import com.vp.voicepocket.domain.friend.dto.response.FriendResponseDto;
import com.vp.voicepocket.domain.friend.entity.Friend;
import com.vp.voicepocket.domain.friend.entity.Status;
import com.vp.voicepocket.domain.friend.event.FriendAcceptPushEvent;
import com.vp.voicepocket.domain.friend.event.FriendRequestPushEvent;
import com.vp.voicepocket.domain.friend.exception.CFriendRequestNotExistException;
import com.vp.voicepocket.domain.friend.exception.CFriendRequestOnGoingException;
import com.vp.voicepocket.domain.friend.repository.FriendRepository;
import com.vp.voicepocket.domain.token.config.JwtProvider;
import com.vp.voicepocket.domain.token.exception.CAccessTokenException;
import com.vp.voicepocket.domain.user.entity.User;
import com.vp.voicepocket.domain.user.exception.CUserNotFoundException;
import com.vp.voicepocket.domain.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final ApplicationEventPublisher eventPublisher;

    // TODO: Will Remove
    private final JwtProvider jwtProvider;

    public FriendResponseDto requestFriend(String toUserEmail, String accessToken) {
        // TODO: Security Refactoring 이후 제거 - 24.03.26
        Authentication authentication = getAuthByAccessToken(accessToken);
        Long fromUserId = Long.parseLong(authentication.getName());

        User fromUser = userRepository.findById(fromUserId)
            .orElseThrow(CUserNotFoundException::new);

        User toUser = userRepository.findByEmail(toUserEmail)
            .orElseThrow(CUserNotFoundException::new);

        friendRepository.findByRequestUsers(fromUserId, toUser).ifPresent(friend -> {
            throw new CFriendRequestOnGoingException();
        });

        var friendRequest = friendRepository.save(
            Friend.builder().requestFrom(fromUser).requestTo(toUser).build());

        eventPublisher.publishEvent(new FriendRequestPushEvent(friendRequest));

        return FriendResponseDto.from(friendRequest);
    }

    private Authentication getAuthByAccessToken(String accessToken) {
        // 만료된 access token 인지 확인
        if (!jwtProvider.validationToken(accessToken)) {
            throw new CAccessTokenException();
        }

        // AccessToken 에서 Username (pk) 가져오기
        return jwtProvider.getAuthentication(accessToken);
    }

    @Transactional(readOnly = true)
    public List<FriendResponseDto> checkRequest(String accessToken) {
        // 인증이 완료된 상태
        Authentication authentication = getAuthByAccessToken(accessToken);
        Long userId = Long.parseLong(authentication.getName());

        return friendRepository.findByToUser(userId)   // 없을 때 공백 리스트를 반환하기
            .stream()
            .map(FriendResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FriendResponseDto> checkResponse(String accessToken) {

        // 인증이 완료된 상태
        Authentication authentication = getAuthByAccessToken(accessToken);
        Long userId = Long.parseLong(authentication.getName());

        return friendRepository.findByFromUser(userId)   // 없을 때 공백 리스트를 반환하기
            .stream()
            .map(FriendResponseDto::from)
            .collect(Collectors.toList());
    }


    public void update(String email, String accessToken, Status status) {
        Authentication authentication = getAuthByAccessToken(accessToken);
        Long userId = Long.parseLong(authentication.getName());

        Friend friendRequest = friendRepository.findByRequest(email, userId)
            .orElseThrow(CFriendRequestNotExistException::new);
        friendRequest.updateStatus(status);

        if (status.equals(Status.ACCEPT)) {
            eventPublisher.publishEvent(new FriendAcceptPushEvent(friendRequest));
        }
    }

    public void delete(FriendRequestDto friendRequestDto, String accessToken) {
        // User Validation Complete
        Authentication authentication = getAuthByAccessToken(accessToken);
        Long userId = Long.parseLong(authentication.getName());
        String opponentEmail = friendRequestDto.getEmail();
        // Find Friend Request w userId + opponent email
        friendRepository.findByUserIdAndEmail(userId, opponentEmail).ifPresentOrElse(
            friendRepository::delete, CFriendRequestNotExistException::new);
    }
}
