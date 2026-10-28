package org.skillbridge.service.impl;

import org.skillbridge.dto.request.MessageCreateRequest;
import org.skillbridge.dto.response.MessageResponse;
import org.skillbridge.entity.Conversation;
import org.skillbridge.entity.Message;
import org.skillbridge.entity.User;
import org.skillbridge.enums.NotificationType;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.MessageMapper;
import org.skillbridge.repository.ConversationRepository;
import org.skillbridge.repository.MessageRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class MessageServiceImpl {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageMapper messageMapper;
    private final CurrentUser currentUser;
    private final NotificationServiceImpl notificationService;

    public MessageServiceImpl(MessageRepository messageRepository,
                              ConversationRepository conversationRepository,
                              MessageMapper messageMapper,
                              CurrentUser currentUser,
                              NotificationServiceImpl notificationService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.messageMapper = messageMapper;
        this.currentUser = currentUser;
        this.notificationService = notificationService;
    }

    public Page<MessageResponse> listForConversation(Long conversationId, Pageable pageable) {
        User me = currentUser.require();
        loadConversationAuthorized(conversationId, me);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(messageMapper::toResponse);
    }

    @Transactional
    public MessageResponse send(Long conversationId, MessageCreateRequest req) {
        User me = currentUser.require();
        Conversation c = loadConversationAuthorized(conversationId, me);

        Message m = new Message();
        m.setConversation(c);
        m.setSender(me);
        m.setContent(req.content());
        Message saved = messageRepository.save(m);

        c.setLastMessageAt(Instant.now());
        conversationRepository.save(c);

        notificationService.notify(
                c.otherParticipant(me),
                NotificationType.MESSAGE,
                "New message",
                "You have a new message from " + me.getFirstName()
        );

        return messageMapper.toResponse(saved);
    }

    @Transactional
    public MessageResponse markRead(Long id) {
        User me = currentUser.require();
        Message m = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        Conversation c = m.getConversation();
        if (!c.getParticipantA().getId().equals(me.getId())
                && !c.getParticipantB().getId().equals(me.getId())) {
            throw new AccessDeniedException("Not your message");
        }
        if (m.getSender().getId().equals(me.getId())) {
            throw new IllegalArgumentException("Cannot mark your own message as read");
        }
        m.setRead(true);
        return messageMapper.toResponse(messageRepository.save(m));
    }

    private Conversation loadConversationAuthorized(Long id, User me) {
        Conversation c = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        boolean isParticipant = c.getParticipantA().getId().equals(me.getId())
                || c.getParticipantB().getId().equals(me.getId());
        if (!isParticipant) throw new AccessDeniedException("Not your conversation");
        return c;
    }
}