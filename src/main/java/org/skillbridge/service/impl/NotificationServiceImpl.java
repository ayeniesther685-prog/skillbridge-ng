package org.skillbridge.service.impl;

import org.skillbridge.dto.response.NotificationResponse;
import org.skillbridge.entity.Notification;
import org.skillbridge.entity.User;
import org.skillbridge.enums.NotificationType;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.NotificationMapper;
import org.skillbridge.repository.NotificationRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final CurrentUser currentUser;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   NotificationMapper notificationMapper,
                                   CurrentUser currentUser) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.currentUser = currentUser;
    }

    /** Fire-and-forget helper used by other services. Never throws on missing user. */
    @Transactional
    public void notify(User user, NotificationType type, String title, String message) {
        if (user == null) return;
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        notificationRepository.save(n);
    }

    public Page<NotificationResponse> listMine(Pageable pageable) {
        User me = currentUser.require();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(me.getId(), pageable)
                .map(notificationMapper::toResponse);
    }

    public long unreadCount() {
        User me = currentUser.require();
        return notificationRepository.countByUserIdAndReadFalse(me.getId());
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        User me = currentUser.require();
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id " + id));
        if (!n.getUser().getId().equals(me.getId())) {
            throw new AccessDeniedException("Not your notification");
        }
        n.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(n));
    }

    @Transactional
    public int markAllRead() {
        User me = currentUser.require();
        var page = notificationRepository.findByUserIdOrderByCreatedAtDesc(me.getId(), Pageable.unpaged());
        int count = 0;
        for (Notification n : page) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
                count++;
            }
        }
        return count;
    }
}