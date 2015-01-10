package com.publicationmetasearchengine.data;

import com.publicationmetasearchengine.gui.ScreenPanel;
import com.publicationmetasearchengine.gui.homescreen.HomeScreenPanel;
import com.publicationmetasearchengine.gui.notificationcriteriasscreen.NotificationCriteriasScreenPanel;
import com.publicationmetasearchengine.gui.searchscreen.SearchScreenPanel;
import java.util.HashMap;
import java.util.Map;

public class User {
    private final Integer id;
    private final String login;
    private final String name;
    private final String surname;
    private final String email;

    private final Map<Class<?extends ScreenPanel>, ScreenPanel> visitedScreenPanels = new HashMap<Class<? extends ScreenPanel>, ScreenPanel>();

    public User(Integer id, String login, String name, String surname, String email) {
        this.id = id;
        this.login = login;
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return String.format("%d - %s %s (%s) - email: %s ", id, name, surname, login, email);
    }

    public ScreenPanel getScreenPanel(ScreenPanel screenPanel) {
        if (screenPanel instanceof HomeScreenPanel   ||
            screenPanel instanceof SearchScreenPanel ||
            screenPanel instanceof NotificationCriteriasScreenPanel)
            if (visitedScreenPanels.containsKey(screenPanel.getClass()))
                return visitedScreenPanels.get(screenPanel.getClass());
            else
                visitedScreenPanels.put(screenPanel.getClass(), screenPanel);

        return screenPanel;
    }
}
