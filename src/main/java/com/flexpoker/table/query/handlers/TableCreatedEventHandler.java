package com.flexpoker.table.query.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.flexpoker.framework.event.EventHandler;
import com.flexpoker.framework.pushnotifier.PushNotification;
import com.flexpoker.framework.pushnotifier.PushNotificationPublisher;
import com.flexpoker.login.query.repository.LoginRepository;
import com.flexpoker.pushnotifications.OpenTableForUserPushNotification;
import com.flexpoker.table.command.events.TableCreatedEvent;
import com.flexpoker.table.query.repository.TableRepository;
import com.flexpoker.web.dto.outgoing.SeatDTO;
import com.flexpoker.web.dto.outgoing.TableDTO;

@Component
public class TableCreatedEventHandler implements EventHandler<TableCreatedEvent> {

    private final LoginRepository loginRepository;

    private final TableRepository tableRepository;

    private final PushNotificationPublisher pushNotificationPublisher;

    @Inject
    public TableCreatedEventHandler(LoginRepository loginRepository,
            TableRepository tableRepository,
            PushNotificationPublisher pushNotificationPublisher) {
        this.loginRepository = loginRepository;
        this.tableRepository = tableRepository;
        this.pushNotificationPublisher = pushNotificationPublisher;
    }

    @Override
    public void handle(TableCreatedEvent event) {
        handleNewTableInsert(event);
        handlePushNotifications(event);
    }

    private void handleNewTableInsert(TableCreatedEvent event) {
        List<SeatDTO> seats = new ArrayList<>();

        for (int position : event.getSeatPositionToPlayerMap().keySet()) {
            String displayName = loginRepository.fetchUsernameByAggregateId(event
                    .getSeatPositionToPlayerMap().get(Integer.valueOf(position)));
            SeatDTO seatDTO = new SeatDTO(position, displayName,
                    event.getStartingNumberOfChips(), 0, false, 0, 0, false, false,
                    false, false);
            seats.add(seatDTO);
        }

        TableDTO tableDTO = new TableDTO(event.getAggregateId(),
                event.getVersion(), seats, 0, Collections.emptySet(),
                Collections.emptyList());
        tableRepository.save(tableDTO);
    }

    private void handlePushNotifications(TableCreatedEvent event) {
        Consumer<UUID> openTableConsumer = (UUID playerId) -> {
            PushNotification pushNotification = new OpenTableForUserPushNotification(
                    event.getGameId(), event.getAggregateId(), playerId);
            pushNotificationPublisher.publish(pushNotification);
        };
        event.getSeatPositionToPlayerMap().values().forEach(openTableConsumer);
    }
}
