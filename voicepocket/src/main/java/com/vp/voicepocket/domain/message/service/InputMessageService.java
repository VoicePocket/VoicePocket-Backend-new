package com.vp.voicepocket.domain.message.service;

import com.vp.voicepocket.domain.message.dto.TTSRequestDto;
import com.vp.voicepocket.domain.message.model.InputMessage;
import com.vp.voicepocket.domain.user.entity.User;
import com.vp.voicepocket.domain.user.exception.CUserNotFoundException;
import com.vp.voicepocket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InputMessageService {

    private final RabbitTemplate rabbitTemplate;    // RabbitTemplate을 통해 Exchange에 메세지를 보내도록 설정
    private final UserRepository userRepository;

    @Transactional
    public void sendMessage(UserDetails userDetails, TTSRequestDto ttsRequestDto) {
        Long userId = Long.parseLong(userDetails.getUsername());

        User user = userRepository.findById(userId)
                .orElseThrow(CUserNotFoundException::new);

        InputMessage inputMessage = InputMessage.builder()
                .type(ttsRequestDto.getType())
                .uuid(ttsRequestDto.getUuid())
                .requestFrom(user.getEmail())
                .requestTo(ttsRequestDto.getRequestTo())
                .text(ttsRequestDto.getText())
                .build();
        log.debug("inputMessage: {}", inputMessage);
        rabbitTemplate.convertAndSend("input.exchange", "input.key", inputMessage);
    }

}