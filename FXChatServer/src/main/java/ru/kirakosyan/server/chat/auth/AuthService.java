package ru.kirakosyan.server.chat.auth;

import java.util.HashSet;
import java.util.Set;

public class AuthService implements IAuthService {

    private static Set<User> USERS = new HashSet<>();

    @Override
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

    @Override
    public void updateUsername(String currentUsername, String newUsername) {
        User user = gerUserByUsername(currentUsername);
        if (user != null) {
            user.setUserName(newUsername);
        }
    }

    private User gerUserByUsername(String username) {
        for (User user : USERS) {
            if (user.getUserName().equals(username)) {
                return user;
            }
        }

        return null;
    }
}
