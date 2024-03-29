package com.vp.voicepocket.domain.friend.repository;

import com.vp.voicepocket.domain.friend.entity.Friend;
import com.vp.voicepocket.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query(value = "select f from Friend f where f.requestFrom.email = ?1 and f.requestTo.id = ?2 and f.status = 'ONGOING'")
    Optional<Friend> findByRequest(String email, Long id);

    @Query(value = "select f from Friend f where f.requestFrom.id = ?1 and f.status = 'ACCEPT'")
    List<Friend> findByFromUser(Long id);

    @Query("select f from Friend f where f.requestFrom.id = ?1 and f.requestTo = ?2 and f.status <> 'REJECT'")
    Optional<Friend> findByRequestUsers(Long requestFromId, User requestToId);

    @Query("select f from Friend f where f.requestFrom.id = ?1 and f.requestTo.email = ?2 and f.status = 'ONGOING'")
    Optional<Friend> findByUserIdAndEmail(Long id, String email);

    @Query("select f from Friend f where f.requestTo.id = ?1 and f.status = 'ONGOING'")
    Optional<Friend> findByToUser(Long id);
}
