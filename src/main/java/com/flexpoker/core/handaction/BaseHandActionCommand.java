package com.flexpoker.core.handaction;

import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.flexpoker.bso.api.PotBso;
import com.flexpoker.core.api.chat.SendTableChatMessageCommand;
import com.flexpoker.core.api.seatstatus.SetSeatStatusForEndOfHandCommand;
import com.flexpoker.core.api.seatstatus.SetSeatStatusForNewRoundCommand;
import com.flexpoker.model.Game;
import com.flexpoker.model.GameEventType;
import com.flexpoker.model.Hand;
import com.flexpoker.model.HandDealerState;
import com.flexpoker.model.HandEvaluation;
import com.flexpoker.model.HandRoundState;
import com.flexpoker.model.Pot;
import com.flexpoker.model.Seat;
import com.flexpoker.model.Table;
import com.flexpoker.model.card.PocketCards;
import com.flexpoker.repository.api.GameRepository;
import com.flexpoker.util.ButtonSeatPredicate;

public abstract class BaseHandActionCommand {

    protected GameRepository gameRepository;
    
    protected SendTableChatMessageCommand sendTableChatMessageCommand;

    protected PotBso potBso;
    
    protected SetSeatStatusForEndOfHandCommand setSeatStatusForEndOfHandCommand;
    
    protected SetSeatStatusForNewRoundCommand setSeatStatusForNewRoundCommand;
    
    protected void handleMiddleOfRound(Game game, Table table,
            Hand realTimeHand, Seat actionOnSeat) {
        realTimeHand.setHandRoundState(HandRoundState.ROUND_IN_PROGRESS);
        actionOnSeat.setActionOn(false);
        // TODO: stop actionOn timer
        Seat nextToActSeat = realTimeHand.getNextToAct();
        nextToActSeat.setActionOn(true);
        // TODO: start actionOn timer
        determineNextToAct(table, realTimeHand);
    }

    protected void handleEndOfRound(Game game, Table table,
            Hand realTimeHand, int bigBlindAmount) {
        realTimeHand.setOriginatingBettor(null);
        realTimeHand.setHandRoundState(HandRoundState.ROUND_COMPLETE);
        realTimeHand.moveToNextDealerState();
        potBso.calculatePotsAfterRound(game, table);
        table.getCurrentHand().setPots(new HashSet<>(potBso.fetchAllPots(game, table)));

        if (realTimeHand.getHandDealerState() == HandDealerState.COMPLETE) {
            setSeatStatusForEndOfHandCommand.execute(table);
            determineWinners(game, table, realTimeHand.getHandEvaluationList());
        } else {
            setSeatStatusForNewRoundCommand.execute(table);
            determineNextToAct(table, realTimeHand);
            determineLastToAct(table, realTimeHand);
            resetRaiseAmountsAfterRound(table, bigBlindAmount);
            resetPossibleSeatActionsAfterRound(table, realTimeHand);
        }
    }

    private void resetRaiseAmountsAfterRound(Table table, int bigBlindAmount) {
        for (Seat seat : table.getSeats()) {
            seat.setCallAmount(0);
            if (bigBlindAmount > seat.getUserGameStatus().getChips()) {
                seat.setRaiseTo(seat.getUserGameStatus().getChips());
            } else {
                seat.setRaiseTo(bigBlindAmount);
            }
        }
    }

    private void resetPossibleSeatActionsAfterRound(Table table,
            Hand realTimeHand) {
        for (Seat seat : table.getSeats()) {
            realTimeHand.addPossibleSeatAction(seat, GameEventType.CHECK);
            realTimeHand.addPossibleSeatAction(seat, GameEventType.RAISE);
            realTimeHand.removePossibleSeatAction(seat, GameEventType.CALL);
            realTimeHand.removePossibleSeatAction(seat, GameEventType.FOLD);
        }
    }

    private void determineNextToAct(Table table, Hand realTimeHand) {
        List<Seat> seats = table.getSeats();
        Seat actionOnSeat = table.getActionOnSeat();

        int actionOnIndex = seats.indexOf(actionOnSeat);

        for (int i = actionOnIndex + 1; i < seats.size(); i++) {
            if (seats.get(i).isStillInHand()) {
                realTimeHand.setNextToAct(seats.get(i));
                return;
            }
        }

        for (int i = 0; i < actionOnIndex; i++) {
            if (seats.get(i).isStillInHand()) {
                realTimeHand.setNextToAct(seats.get(i));
                return;
            }
        }
    }

    private void determineWinners(Game game, Table table, List<HandEvaluation> handEvaluationList) {
        potBso.setWinners(game, table, handEvaluationList);

        for (Pot pot : potBso.fetchAllPots(game, table)) {
            List<Seat> winners = pot.getWinners();
            int numberOfWinners = winners.size();
            int numberOfChips = pot.getAmount() / numberOfWinners;
            int bonusChips = pot.getAmount() % numberOfWinners;
            int numberOfPlayersInPot = pot.getSeats().size();

            winners.get(0).getUserGameStatus().setChips(
                    winners.get(0).getUserGameStatus().getChips() + bonusChips);

            for (Seat winner : winners) {
                winner.getUserGameStatus().setChips(
                        winner.getUserGameStatus().getChips() + numberOfChips);
                PocketCards pocketCards = table.getCurrentHand().getDeck()
                        .getPocketCards(winner.getPosition());
                if (numberOfPlayersInPot > 1) {
                    winner.setShowCards(pocketCards);
                }
            }
        }
    }

    protected void determineLastToAct(Table table, Hand realTimeHand) {
        List<Seat> seats = table.getSeats();

        int seatIndex;

        if (realTimeHand.getOriginatingBettor() == null) {
            Seat buttonSeat = (Seat) CollectionUtils.find(table.getSeats(),
                    new ButtonSeatPredicate());
            seatIndex = seats.indexOf(buttonSeat);
        } else {
            seatIndex = seats.indexOf(realTimeHand.getOriginatingBettor());
            if (seatIndex == 0) {
                seatIndex = seats.size() - 1;
            } else {
                seatIndex--;
            }
        }

        for (int i = seatIndex; i >= 0; i--) {
            if (seats.get(i).isStillInHand()) {
                realTimeHand.setLastToAct(seats.get(i));
                return;
            }
        }

        for (int i = seats.size() - 1; i > seatIndex; i--) {
            if (seats.get(i).isStillInHand()) {
                realTimeHand.setLastToAct(seats.get(i));
                return;
            }
        }
    }

}
