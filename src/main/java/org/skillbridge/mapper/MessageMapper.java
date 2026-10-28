package org.skillbridge.mapper;

import org.skillbridge.dto.response.MessageResponse;
import org.skillbridge.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageResponse toResponse(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getConversation().getId(),
                m.getSender().getId(),
                (m.getSender().getFirstName() + " " + m.getSender().getLastName()).trim(),
                m.getContent(),
                m.isRead(),
                m.getCreatedAt()
        );
    }
}