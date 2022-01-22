package ru.kirakosyan.server.chat.auth;

import java.util.HashSet;
import java.util.Set;

public class AuthService {

    private static Set<User> USERS = new HashSet<>();

    public String getUserNameByLoginAndPassword(String login, String password) {
        USERS.add(new User("login1", "pass1", "username1"));
        USERS.add(new User("login2", "pass2", "username2"));
        USERS.add(new User("login3", "pass3", "username3"));

        User requireUser = new User(login, password);
        for (User user : USERS) {
            if (requireUser.equals(user)) {
                return user.getUserName();
            }
        }

        return null;
    }
}
