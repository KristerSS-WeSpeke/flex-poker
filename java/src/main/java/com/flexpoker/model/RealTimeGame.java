package com.flexpoker.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealTimeGame {

    private List<User> users;

    private Map<String, Integer> eventVerificationMap =
            new HashMap<String, Integer>();

    private Blinds currentBlinds;

    public RealTimeGame(List<User> users) {
        this.users = users;
    }

    public boolean isEventVerified(String event) {
        synchronized (this) {
            Integer numberOfVerified = eventVerificationMap.get(event);

            if (numberOfVerified == null) {
                return false;
            }

            return numberOfVerified == users.size();
        }
    }

    public void verifyEvent(User user, String string) {
        synchronized (this) {
            Integer numberOfVerified = eventVerificationMap.get(string);
            if (numberOfVerified == null) {
                numberOfVerified = 0;
            }
            eventVerificationMap.put(string, ++numberOfVerified);
        }
    }

    public Blinds getCurrentBlinds() {
        return currentBlinds;
    }

}