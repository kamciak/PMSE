package com.publicationmetasearchengine.utils;

import com.vaadin.Application;
import com.vaadin.ui.Window.Notification;
import java.io.Serializable;

public class Notificator implements Serializable{
    private static final long serialVersionUID = 1L;

    public enum NotificationType{
        HUMANIZED(1),
        WARNING(2),
        ERROR(3),
        TRAY(4);

        int type;

        NotificationType(int type) {
            this.type = type;
        }

        public int getType(){
            return type;
        }

    }

    public static void showNotification(Application application, String caption, String text, NotificationType type){
        Notification notification = new Notification(caption, text, type.getType());
        application.getMainWindow().getWindow().showNotification(notification);
    }
}
