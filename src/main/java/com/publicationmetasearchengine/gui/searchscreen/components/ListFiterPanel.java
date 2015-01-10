package com.publicationmetasearchengine.gui.searchscreen.components;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.data.filters.FilterType;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;

public class ListFiterPanel extends PMSEPanel implements Filter {
    private static final long serialVersionUID = 1L;

    private final FilterType filterType;

    private final VerticalLayout mainLayout = new VerticalLayout();
    private final TextField testTextField = new TextField();

    private final PMSEButton clearBtn = new PMSEButton("clear");

    public ListFiterPanel(String caption, FilterType filterType) {
        super(caption);
        this.filterType = filterType;
        setSizeUndefined();
        mainLayout.setSizeUndefined();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        clearBtn.setStyleName("link");
        clearBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                clear();
            }
        });

        mainLayout.addComponent(testTextField);
        mainLayout.addComponent(clearBtn);
        mainLayout.setComponentAlignment(clearBtn, Alignment.MIDDLE_RIGHT);

        setContent(mainLayout);
    }

    @Override
    public FilterCriteria getFilterCriteria() {
        ArrayList<String> values = new ArrayList<String>();
        if (testTextField.getValue()!= null && !((String)testTextField.getValue()).isEmpty())
            for (String author : ((String) testTextField.getValue()).split(","))
            values.add(author.trim());
        return new FilterCriteria(filterType, values, ComboCondition.Op.AND, null);
    }

    @Override
    public void setFilterCriteria(FilterCriteria filterCriteria) {
        if (filterCriteria.getValues() == null || filterCriteria.getValues().isEmpty())
            clear();
        else {
            testTextField.setValue(filterCriteria.getValues().toString().replaceAll("\\[", "").replaceAll("\\]","").trim());
        }
    }



    @Override
    public void clear() {
        testTextField.setValue("");
    }
}
