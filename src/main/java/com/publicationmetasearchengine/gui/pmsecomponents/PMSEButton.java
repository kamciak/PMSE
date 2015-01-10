package com.publicationmetasearchengine.gui.pmsecomponents;

import com.vaadin.ui.Button;

public class PMSEButton extends Button{
    private static final long serialVersionUID = 1L;

    public PMSEButton() {
        super();
        setStyleName("small default");
    }

    public PMSEButton(String caption, ClickListener listener) {
        super(caption, listener);
        setStyleName("small default");
    }

    public PMSEButton(String caption) {
        super(caption);
        setStyleName("small default");
    }
}
