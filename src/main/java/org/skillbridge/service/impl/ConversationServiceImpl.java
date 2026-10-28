package org.skillbridge.service.impl;

import org.skillbridge.dto.request.ConversationCreateRequest;
import org.skillbridge.dto.response.ConversationResponse;
import org.skillbridge.entity.Conversation;
import org.skillbridge.entity.User;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.ConversationMapper;
import org.skillbridge.repository.ConversationRepository;
import org.skillbridge.repository.MessageRepository;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConversationServiceImpl {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;
    private final CurrentUser currentUser;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   MessageRepository messageRepository,
                                   UserRepository userRepository,
                                   ConversationMapper conversationMapper,
                                   CurrentUser currentUser) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.conversationMapper = conversationMapper;
        this.currentUser = currentUser;
    }

    public Page<ConversationResponse> listMine(Pageable pageable) {
        User me = currentUser.require();
        return conversationRepository.findAllForUser(me.getId(), pageable)
                .map(c -> {
                    long unread = messageRepository
                            .countByConversationIdAndSenderIdNotAndReadFalse(c.getId(), me.getId());
                    return conversationMapper.toResponse(c, me, unread);
                });
    }

    @Transactional
    public ConversationResponse findOrCreate(ConversationCreateRequest req) {
        User me = currentUser.require();
        if (me.getId().equals(req.otherUserId())) {
            throw new IllegalArgumentException("Cannot start a conversation with yourself");
        }
        User other = userRepository.findById(req.otherUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long a = Math.min(me.getId(), other.getId());
        Long b = Math.max(me.getId(), other.getId());

        Conversation conv = conversationRepository.findByParticipantAIdAndParticipantBId(a, b)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setParticipantA(a.equals(me.getId()) ? me : other);
                    c.setParticipantB(b.equals(me.getId()) ? me : other);
                    return conversationRepository.save(c);
                });

        long unread = messageRepository
                .countByConversationIdAndSenderIdNotAndReadFalse(conv.getId(), me.getId());
        return conversationMapper.toResponse(conv, me, unread);
    }

    public ConversationResponse findById(Long id) {
        User me = currentUser.require();
        Conversation c = loadAndAuthorize(id, me);
        long unread = messageRepository
                .countByConversationIdAndSenderIdNotAndReadFalse(c.getId(), me.getId());
        return conversationMapper.toResponse(c, me, unread);
    }

    private Conversation loadAndAuthorize(Long id, User me) {
        Conversation c = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        boolean isParticipant = c.getParticipantA().getId().equals(me.getId())
                || c.getParticipantB().getId().equals(me.getId());
        if (!isParticipant) throw new AccessDeniedException("Not your conversation");
        return c;
    }
}