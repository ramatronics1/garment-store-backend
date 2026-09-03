package com.garmentstore.notification.infrastructure;

import com.garmentstore.notification.domain.NotificationChannelType;
import com.garmentstore.notification.domain.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /** Fetch IN_APP notifications for a user (most recent first). */
    Page<NotificationLog> findByUserIdAndChannelOrderByCreatedAtDesc(
            Long userId, NotificationChannelType channel, Pageable pageable);

    /** Count unread IN_APP notifications for a user. */
    long countByUserIdAndChannelAndReadAtIsNull(Long userId, NotificationChannelType channel);

    /** Mark a single notification as read. */
    @Modifying
    @Query("UPDATE NotificationLog n SET n.readAt = :readAt WHERE n.id = :id AND n.userId = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId, @Param("readAt") Instant readAt);

    /** Mark all IN_APP notifications as read for a user. */
    @Modifying
    @Query("UPDATE NotificationLog n SET n.readAt = :readAt " +
           "WHERE n.userId = :userId AND n.channel = 'IN_APP' AND n.readAt IS NULL")
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") Instant readAt);
}
