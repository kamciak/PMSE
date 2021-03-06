package com.publicationmetasearchengine.gui.homescreen;

import com.publicationmetasearchengine.data.Publication;
import com.vaadin.ui.Table;
import java.util.ArrayList;
import java.util.List;


public class PublicationTable extends Table {
    private static final long serialVersionUID = 1L;
    private List<Publication> allPublications = new ArrayList<Publication>();
    public static final String TABLE_SOURCE_COLUMN = "Source";
    public static final String TABLE_LEAD_AUTHOR_COLUMN = "First author";
    public static final String TABLE_TITLE_COLUMN = "Title";
    public static final String TABLE_PUBLICATION_COLUMN = "";
    private static final String[] TABLE_VISIBLE_COLUMNS = {
        TABLE_SOURCE_COLUMN,
        TABLE_LEAD_AUTHOR_COLUMN,
        TABLE_TITLE_COLUMN
    };
    

    public PublicationTable() {
        addContainerProperty(TABLE_SOURCE_COLUMN, String.class, "");
        addContainerProperty(TABLE_LEAD_AUTHOR_COLUMN, String.class, "");
        addContainerProperty(TABLE_TITLE_COLUMN, String.class, "");
        setColumnExpandRatio(TABLE_TITLE_COLUMN, 1);
        setColumnWidth(TABLE_LEAD_AUTHOR_COLUMN, 160);
        addContainerProperty(TABLE_PUBLICATION_COLUMN, Publication.class, null);
        setVisibleColumns(TABLE_VISIBLE_COLUMNS);
    }

    public void clear() {
        removeAllItems();
        allPublications.clear();
    }

    public void addPublicationToTable(Publication publication) {
        Object id = addItem();
        getItem(id).getItemProperty(TABLE_SOURCE_COLUMN).setValue(publication.getSourceDB().getShortName());
        getItem(id).getItemProperty(TABLE_LEAD_AUTHOR_COLUMN).setValue(publication.getMainAuthor());
        getItem(id).getItemProperty(TABLE_TITLE_COLUMN).setValue(publication.getTitle());
        getItem(id).getItemProperty(TABLE_PUBLICATION_COLUMN).setValue(publication);
    }
    
    private void addPublicationToTableAndList(Publication publication) {
        addPublicationToTable(publication);
        allPublications.add(publication);
    }

    public void addMockPublication(Publication publication) {
        Object id = addItem();
        getItem(id).getItemProperty(TABLE_TITLE_COLUMN).setValue(publication.getTitle());
        getItem(id).getItemProperty(TABLE_PUBLICATION_COLUMN).setValue(publication);
    }

    public void addPublications(List<Publication> publications) {
        for (Publication publication : publications)
            addPublicationToTableAndList(publication);
    }
    
    public void cleanAndAddPublications(List<Publication> publications) {
        clear();
        for (Publication publication : publications)
            addPublicationToTableAndList(publication);
        
    }

    public List<Publication> getAllPublication(){
        return allPublications;
    }
 

    public void filterByTitleKeywords(List<String> keywords){
        removeAllItems();
        if (keywords==null || keywords.isEmpty()) {
            for(Publication publication : allPublications)
                addPublicationToTable(publication);
            return;
        }

        for(Publication publication : allPublications) {
            boolean allMatches = true;
            for(String keyword : keywords)
                if (!publication.getTitle().toLowerCase().contains(keyword)) {
                    allMatches = false;
                    break;
                }
            if (allMatches)
                addPublicationToTable(publication);
        }
    }
}
