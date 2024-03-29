package com.vp.voicepocket.domain.friend.event;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.vp.voicepocket.domain.firebase.entity.FCMUserToken;
import com.vp.voicepocket.domain.firebase.exception.CFCMTokenNotFoundException;
import com.vp.voicepocket.domain.firebase.repository.FCMRepository;
import com.vp.voicepocket.domain.user.entity.User;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushEventListener {
    private final FirebaseMessaging firebaseMessaging;
    private final FCMRepository fcmRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendFriendRequestPushMessage(FriendRequestPushEvent friendRequestPushEvent) {
        User toUser = friendRequestPushEvent.getFriend().getRequestTo();
        User fromUser = friendRequestPushEvent.getFriend().getRequestFrom();

        FCMUserToken fcmEntity = fcmRepository.findByUserId(toUser)
            .orElseThrow(CFCMTokenNotFoundException::new);
        String fcmToken = fcmEntity.getFireBaseToken();

        try {
            Message message = getFriendRequestPushMessage(fromUser.getName(), fcmToken);
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("PUSH NOTIFICATION ERROR: {}", e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendFriendAcceptPushMessage(FriendAcceptPushEvent friendAcceptPushEvent) {
        User toUser = friendAcceptPushEvent.getFriend().getRequestTo();
        User fromUser = friendAcceptPushEvent.getFriend().getRequestFrom();

        FCMUserToken fcmEntity = fcmRepository.findByUserId(fromUser)
            .orElseThrow(CFCMTokenNotFoundException::new);
        String fcmToken = fcmEntity.getFireBaseToken();

        try {
            Message message = getFriendAcceptPushMessage(toUser.getName(), fcmToken);
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("PUSH NOTIFICATION ERROR: {}", e.getMessage());
        }
    }

    private Message getFriendRequestPushMessage(String fromUserName, String fcmToken) {
        Notification notification = Notification.builder()
            .setTitle("Friend Request")
            .setBody(fromUserName + " request Friend to you!")
            .build();

        HashMap<String, String> data = new HashMap<>();
        data.put("ID", "1");

        return Message.builder()
            .setToken(fcmToken)
            .setNotification(notification)
            .putAllData(data)
            .build();
    }

    private Message getFriendAcceptPushMessage(String toUserName, String fcmToken) {
        Notification notification = Notification.builder()
            .setTitle("Friend Accept")
            .setBody(toUserName + " Accept your Friend Request!")
            .build();

        HashMap<String, String> data = new HashMap<>();
        data.put("ID", "2");

        return Message.builder()
            .setToken(fcmToken)
            .setNotification(notification)
            .putAllData(data)
            .build();
    }
}
