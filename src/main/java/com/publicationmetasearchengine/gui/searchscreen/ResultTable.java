package com.publicationmetasearchengine.gui.searchscreen;

import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.utils.DateUtils;
import com.vaadin.ui.Table;
import java.util.List;

public class ResultTable extends Table {
    private static final long serialVersionUID = 1L;

    public static final String TABLE_PUBLICATION_DATE_COLUMN = "Publication date";
    public static final String TABLE_TITLE_COLUMN = "Title";
    public static final String TABLE_AUTHOR_COLUMN = "Author";
    public static final String TABLE_PUBLICATION_COLUMN = "";
    private static final String[] TABLE_VISIBLE_COLUMNS = {
        TABLE_PUBLICATION_DATE_COLUMN,
        TABLE_AUTHOR_COLUMN,
        TABLE_TITLE_COLUMN
    };

    public ResultTable() {
        addContainerProperty(TABLE_PUBLICATION_DATE_COLUMN, String.class, "");
        addContainerProperty(TABLE_AUTHOR_COLUMN, String.class, "");
        addContainerProperty(TABLE_TITLE_COLUMN, String.class, "");
        setColumnExpandRatio(TABLE_TITLE_COLUMN, 1);
        addContainerProperty(TABLE_PUBLICATION_COLUMN, Publication.class, null);
        setVisibleColumns(TABLE_VISIBLE_COLUMNS);
    }

    public void clear() {
        removeAllItems();
    }

    public void addPublication(Publication publication) {
        Object id = addItem();
        getItem(id).getItemProperty(TABLE_PUBLICATION_DATE_COLUMN).setValue(DateUtils.formatDateOnly(publication.getPublicationDate()));
        getItem(id).getItemProperty(TABLE_AUTHOR_COLUMN).setValue(publication.getMainAuthor());
        getItem(id).getItemProperty(TABLE_TITLE_COLUMN).setValue(publication.getTitle());
        getItem(id).getItemProperty(TABLE_PUBLICATION_COLUMN).setValue(publication);
    }

    public void addPublications(List<Publication> publications) {
        for (Publication p : publications) {
            addPublication(p);
        }
    }
}
