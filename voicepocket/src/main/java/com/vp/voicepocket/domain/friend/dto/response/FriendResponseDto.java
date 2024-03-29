package com.vp.voicepocket.domain.friend.dto.response;

import com.vp.voicepocket.domain.friend.entity.Friend;
import com.vp.voicepocket.domain.friend.entity.Status;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FriendResponseDto {

    private final Long id;
    private final Long requestFromId;
    private final Long requestToId;
    private final Status status;

    public static FriendResponseDto from(Friend friend) {
        return new FriendResponseDto(
            friend.getId(),
            friend.getRequestFrom().getId(),
            friend.getRequestTo().getId(),
            friend.getStatus()
        );
    }
}
