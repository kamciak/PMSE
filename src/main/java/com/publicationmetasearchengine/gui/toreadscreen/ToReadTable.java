package com.publicationmetasearchengine.gui.toreadscreen;

import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.utils.DateUtils;
import com.vaadin.ui.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ToReadTable extends Table {
    private static final long serialVersionUID = 1L;

    public static final String TABLE_DATE_COLUMN = "Mark Date";
    public static final String TABLE_LEAD_AUTHOR_COLUMN = "First author";
    public static final String TABLE_TITLE_COLUMN = "Title";
    public static final String TABLE_PUBLICATION_COLUMN = "";
    private static final String[] TABLE_VISIBLE_COLUMNS = {
        TABLE_DATE_COLUMN,
        TABLE_LEAD_AUTHOR_COLUMN,
        TABLE_TITLE_COLUMN
    };

    public ToReadTable() {
        addContainerProperty(TABLE_DATE_COLUMN, String.class, "");
        addContainerProperty(TABLE_LEAD_AUTHOR_COLUMN, String.class, "");
        addContainerProperty(TABLE_TITLE_COLUMN, String.class, "");
        setColumnExpandRatio(TABLE_TITLE_COLUMN, 1);
        addContainerProperty(TABLE_PUBLICATION_COLUMN, Publication.class, null);
        setVisibleColumns(TABLE_VISIBLE_COLUMNS);
        setSortContainerPropertyId(TABLE_DATE_COLUMN);
        setSortAscending(false);
    }

    public void clear() {
        removeAllItems();
    }

    public void addPublication(Date date, Publication publication) {
        Object id = addItem();
        getItem(id).getItemProperty(TABLE_DATE_COLUMN).setValue(DateUtils.formatDate(date));
        getItem(id).getItemProperty(TABLE_LEAD_AUTHOR_COLUMN).setValue(publication.getMainAuthor());
        getItem(id).getItemProperty(TABLE_TITLE_COLUMN).setValue(publication.getTitle());
        getItem(id).getItemProperty(TABLE_PUBLICATION_COLUMN).setValue(publication);
    }

    public void addPublications(Map<Date, List<Publication>> publications) {
        for (Map.Entry<Date, List<Publication>> entry : publications.entrySet()) {
            Date date = entry.getKey();
            for(Publication  publication : entry.getValue())
                addPublication(date, publication);
        }
        sort();
    }

    public List<Publication> getPublications() {
        ArrayList<Publication> publications = new ArrayList<Publication>();
        for (Object id : getItemIds())
            publications.add((Publication) getItem(id).getItemProperty(TABLE_PUBLICATION_COLUMN).getValue());

        return publications;
    }
}
