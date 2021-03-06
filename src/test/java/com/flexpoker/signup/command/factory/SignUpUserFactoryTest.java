package com.flexpoker.signup.command.factory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.flexpoker.signup.command.aggregate.DefaultSignUpUserFactory;
import com.flexpoker.signup.command.aggregate.SignUpUser;
import com.flexpoker.signup.command.commands.SignUpNewUserCommand;
import com.flexpoker.signup.command.events.NewUserSignedUpEvent;
import com.flexpoker.signup.command.events.SignedUpUserConfirmedEvent;
import com.flexpoker.signup.command.framework.SignUpEvent;

public class SignUpUserFactoryTest {

    private DefaultSignUpUserFactory sut;

    @Before
    public void setup() {
        sut = new DefaultSignUpUserFactory();
    }

    @Test
    public void testCreateNew() {
        SignUpNewUserCommand command = new SignUpNewUserCommand("test",
                "test@test.com", "password");
        SignUpUser signUpUser = sut.createNew(command);
        assertNotNull(signUpUser);
        assertFalse(signUpUser.fetchAppliedEvents().isEmpty());
        assertFalse(signUpUser.fetchNewEvents().isEmpty());
    }

    @Test
    public void testCreateFrom() {
        List<SignUpEvent> events = new ArrayList<>();
        events.add(new NewUserSignedUpEvent(null, 1, null, null, null, null));
        events.add(new SignedUpUserConfirmedEvent(null, 2, null, null));
        SignUpUser signUpUser = sut.createFrom(events);
        assertNotNull(signUpUser);
        assertFalse(signUpUser.fetchAppliedEvents().isEmpty());
        assertTrue(signUpUser.fetchNewEvents().isEmpty());
    }

}
