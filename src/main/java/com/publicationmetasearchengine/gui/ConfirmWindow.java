package com.publicationmetasearchengine.gui;

import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.Serializable;

public abstract class ConfirmWindow implements Serializable{
    private static final long serialVersionUID = 1L;

    private Application application;
    private Window window;
    private Label label;

    private HorizontalLayout buttonLayout;
    private PMSEButton yesBtn = new PMSEButton("Yes");
    private PMSEButton noBtn = new PMSEButton("No");

    public ConfirmWindow(Application application, String caption, String info) {
        this.application = application;
        window = new Window(caption);
        window.setClosable(false);
        window.setResizable(false);
        window.setModal(true);
        window.setWidth("400px");
        label = new Label(info);

        buttonLayout = initButtonLayout();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(label);
        mainLayout.addComponent(buttonLayout);
        mainLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
        window.setContent(mainLayout);

        application.getMainWindow().getWindow().addWindow(window);
    }

    private HorizontalLayout initButtonLayout() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        yesBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                yesButtonClick();
                application.getMainWindow().getWindow().removeWindow(window);
            }
        });
        yesBtn.setWidth("44px");
        noBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                application.getMainWindow().getWindow().removeWindow(window);
            }
        });
        noBtn.setWidth("44px");

        hl.addComponent(yesBtn);
        hl.addComponent(noBtn);

        return hl;
    }

    abstract public void yesButtonClick();
}
