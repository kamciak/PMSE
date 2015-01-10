package com.publicationmetasearchengine.gui.searchscreen.components;

import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.vaadin.ui.Component;

public interface Filter extends Component {
    FilterCriteria getFilterCriteria();

    void setFilterCriteria(FilterCriteria filterCriteria);

    void clear();
}
