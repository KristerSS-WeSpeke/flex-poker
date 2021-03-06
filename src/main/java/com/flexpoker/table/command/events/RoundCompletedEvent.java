package com.flexpoker.table.command.events;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flexpoker.framework.event.BaseEvent;
import com.flexpoker.table.command.aggregate.HandDealerState;
import com.flexpoker.table.command.framework.TableEvent;

public class RoundCompletedEvent extends BaseEvent implements TableEvent {

    private final UUID gameId;

    private final UUID handId;

    private final HandDealerState nextHandDealerState;

    @JsonCreator
    public RoundCompletedEvent(@JsonProperty(value = "aggregateId") UUID aggregateId,
            @JsonProperty(value = "version") int version,
            @JsonProperty(value = "gameId") UUID gameId,
            @JsonProperty(value = "handId") UUID handId,
            @JsonProperty(value = "handDealerState") HandDealerState nextHandDealerState) {
        super(aggregateId, version);
        this.gameId = gameId;
        this.handId = handId;
        this.nextHandDealerState = nextHandDealerState;
    }

    @Override
    public UUID getGameId() {
        return gameId;
    }

    public UUID getHandId() {
        return handId;
    }

    public HandDealerState getNextHandDealerState() {
        return nextHandDealerState;
    }

}
