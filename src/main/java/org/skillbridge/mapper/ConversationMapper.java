package org.skillbridge.mapper;

import org.skillbridge.dto.response.ConversationResponse;
import org.skillbridge.entity.Conversation;
import org.skillbridge.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {

    public ConversationResponse toResponse(Conversation c, User me, long unreadCount) {
        User other = c.otherParticipant(me);
        return new ConversationResponse(
                c.getId(),
                other.getId(),
                (other.getFirstName() + " " + other.getLastName()).trim(),
                other.getEmail(),
                other.getProfilePicture(),
                c.getLastMessageAt(),
                unreadCount,
                c.getCreatedAt()
        );
    }
}