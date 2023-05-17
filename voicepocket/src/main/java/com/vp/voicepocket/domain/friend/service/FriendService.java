package com.vp.voicepocket.domain.friend.service;

import com.vp.voicepocket.domain.friend.dto.request.FriendRequestDto;
import com.vp.voicepocket.domain.friend.dto.response.FriendResponseDto;
import com.vp.voicepocket.domain.friend.entity.Friend;
import com.vp.voicepocket.domain.friend.entity.Status;
import com.vp.voicepocket.domain.friend.exception.CFriendRequestNotExistException;
import com.vp.voicepocket.domain.friend.exception.CFriendRequestOnGoingException;
import com.vp.voicepocket.domain.friend.repository.FriendRepository;
import com.vp.voicepocket.domain.token.config.JwtProvider;
import com.vp.voicepocket.domain.token.exception.CAccessTokenException;
import com.vp.voicepocket.domain.user.entity.User;
import com.vp.voicepocket.domain.user.exception.CUserNotFoundException;
import com.vp.voicepocket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;

    private final FriendRepository friendRepository;

    private final JwtProvider jwtProvider;
    @Transactional
    public FriendResponseDto requestFriend(FriendRequestDto friendRequestDto, String accessToken) {
        Authentication authentication= getAuthByAccessToken(accessToken);

        User to_user =
                userRepository.findByEmail(friendRequestDto.getEmail())
                        .orElseThrow(CUserNotFoundException::new);

        User from_user =
                userRepository.findById(Long.parseLong(authentication.getName()))
                        .orElseThrow(CUserNotFoundException::new);

        if (friendRepository.findByRequest(from_user, to_user, Status.ONGOING).isPresent() ||
                friendRepository.findByRequest(from_user, to_user, Status.ACCEPT).isPresent()) {
            throw new CFriendRequestOnGoingException();
        }

        Friend friend = friendRequestDto.toEntity(from_user, to_user, Status.ONGOING);
        return mapFriendEntityToFriendResponseDTO(friendRepository.save(friend));
    }

    private Authentication getAuthByAccessToken(String accessToken) {
        // 만료된 access token 인지 확인
        if (!jwtProvider.validationToken(accessToken)) {
            throw new CAccessTokenException();
        }

        // AccessToken 에서 Username (pk) 가져오기
        return jwtProvider.getAuthentication(accessToken);
    }

    private FriendResponseDto mapFriendEntityToFriendResponseDTO(Friend friend){
        return FriendResponseDto.builder()
                .id(friend.getId())
                .request_from(friend.getRequest_from())
                .request_to(friend.getRequest_to())
                .status(friend.getStatus())
                .build();
    }

    // TODO: Should Check Proxy Problem?
    @Transactional
    public List<FriendResponseDto> checkRequest(String accessToken) {
        Authentication authentication= getAuthByAccessToken(accessToken);
        User to_user = userRepository.findById(Long.parseLong(authentication.getName())).orElseThrow(CUserNotFoundException::new);
        return friendRepository.findByToUser(to_user)   // 없을 때 공백 리스트를 반환하기
                .stream()
                .map(this::mapFriendEntityToFriendResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public void update(FriendRequestDto friendRequestDto, String accessToken, Status status){
        Authentication authentication= getAuthByAccessToken(accessToken);
        User from_user = userRepository.findByEmail(friendRequestDto.getEmail()).orElseThrow(CUserNotFoundException::new);
        User to_user = userRepository.findById(Long.parseLong(authentication.getName())).orElseThrow(CUserNotFoundException::new);
        Friend modifiedFriend = friendRepository.findByRequest(from_user, to_user, Status.ONGOING).orElseThrow(CFriendRequestNotExistException::new);
        modifiedFriend.updateStatus(status);
    }
}
