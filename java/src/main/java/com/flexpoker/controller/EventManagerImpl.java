package com.flexpoker.controller;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.flex.messaging.AsyncMessageCreator;
import org.springframework.flex.messaging.MessageTemplate;
import org.springframework.stereotype.Controller;

import com.flexpoker.model.Game;
import com.flexpoker.model.User;

import flex.messaging.messages.AsyncMessage;

@Controller("eventManager")
public class EventManagerImpl implements EventManager {

    private static final String GAMES_UPDATED = "gamesUpdated";

    private static final String USER_JOINED_GAME = "userJoinedGame";
    
    private static final String GAME_STATUS_UPDATES = "gameStatusUpdates";

    private static final String JMS_CHAT = "jms-chat";

    private MessageTemplate messageTemplate;

    @Override
    public void sendGamesUpdatedEvent() {
        messageTemplate.send(GAMES_UPDATED, null);
    }

    @Override
    public void sendUserJoinedEvent(final User user, final Game game) {
        messageTemplate.send(new AsyncMessageCreator() {
            @Override
            public AsyncMessage createMessage() {
                AsyncMessage message = messageTemplate
                        .createMessageForDestination(GAME_STATUS_UPDATES);
                message.setHeader(AsyncMessage.SUBTOPIC_HEADER_NAME,
                        game.getId() + "." + USER_JOINED_GAME);
               message.setBody(user);
               return message;
            }
        });
    }

    @Override
    public void sendChatEvent(String username, String text) {
        MapMessage mapMessage = new ActiveMQMapMessage();

        try {
            mapMessage.setString("userId", username);
            mapMessage.setString("chatMessage", text);
        } catch (JMSException e) {
            throw new RuntimeException("JMSException thrown while trying to "
                + "send chat event.");
        }

        messageTemplate.send(JMS_CHAT, mapMessage);
    }

    public MessageTemplate getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(MessageTemplate messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

}