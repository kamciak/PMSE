package com.publicationmetasearchengine.gui.pmsecomponents;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import java.io.Serializable;

public class PMSEPanel extends Panel implements Serializable {
    private static final long serialVersionUID = 1L;

    public PMSEPanel(String caption, ComponentContainer content) {
        super(caption, content);
        setStyleName("bubble");
    }

    public PMSEPanel(String caption) {
        super(caption);
        setStyleName("bubble");
    }

    public PMSEPanel(ComponentContainer content) {
        super(content);
        setStyleName("bubble");
    }

    public PMSEPanel() {
        super();
        setStyleName("bubble");
    }
}
