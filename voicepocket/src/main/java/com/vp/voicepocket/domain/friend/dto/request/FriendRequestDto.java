package com.vp.voicepocket.domain.friend.dto.request;

import com.vp.voicepocket.domain.user.entity.vo.Email;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestDto {

    @javax.validation.constraints.Email
    private String email;

    public String getEmail(){
        return Email.from(email).getValue();
    }
}
