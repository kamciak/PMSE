package com.publicationmetasearchengine.gui;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Embedded;

public enum Icon {
    FILTER_ADD("./icons/filter-add.png"),
    FILTER_DEL("./icons/filter-delete.png");

    private Resource resource;
    private Embedded embedded;

    Icon(String path){
        resource = new ThemeResource(path);
        embedded = new Embedded(null, resource);
    }

    public Resource getResource() {
        return resource;
    }

    public Embedded getEmbedded() {
        return embedded;
    }
}
