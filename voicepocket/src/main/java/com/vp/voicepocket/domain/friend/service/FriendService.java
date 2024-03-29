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
import com.vp.voicepocket.domain.user.entity.User;
import com.vp.voicepocket.domain.user.exception.CUserNotFoundException;
import com.vp.voicepocket.domain.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FriendResponseDto requestFriend(UserDetails userDetails, String toUserEmail) {
        Long fromUserId = getUserIdWithUserDetails(userDetails);
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

    @Transactional(readOnly = true)
    public List<FriendResponseDto> checkRequest(UserDetails userDetails) {
        Long userId = getUserIdWithUserDetails(userDetails);
        return friendRepository.findByToUser(userId)   // 없을 때 공백 리스트를 반환하기
            .stream()
            .map(FriendResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FriendResponseDto> checkResponse(UserDetails userDetails) {
        Long userId = getUserIdWithUserDetails(userDetails);

        return friendRepository.findByFromUser(userId)   // 없을 때 공백 리스트를 반환하기
            .stream()
            .map(FriendResponseDto::from)
            .collect(Collectors.toList());
    }


    public void update(UserDetails userDetails, String email, Status status) {
        Long userId = getUserIdWithUserDetails(userDetails);
        Friend friendRequest = friendRepository.findByRequest(email, userId)
            .orElseThrow(CFriendRequestNotExistException::new);
        friendRequest.updateStatus(status);

        if (status.equals(Status.ACCEPT)) {
            eventPublisher.publishEvent(new FriendAcceptPushEvent(friendRequest));
        }
    }

    public void delete(UserDetails userDetails, FriendRequestDto friendRequestDto) {
        // User Validation Complete
        Long userId = getUserIdWithUserDetails(userDetails);
        String opponentEmail = friendRequestDto.getEmail();
        // Find Friend Request w userId + opponent email
        friendRepository.findByUserIdAndEmail(userId, opponentEmail).ifPresentOrElse(
            friendRepository::delete, CFriendRequestNotExistException::new);
    }

    private Long getUserIdWithUserDetails(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
