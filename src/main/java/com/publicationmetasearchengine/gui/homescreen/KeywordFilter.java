package com.publicationmetasearchengine.gui.homescreen;

import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class KeywordFilter extends HorizontalLayout {
    private static final long serialVersionUID = 1L;

    private final TextField keywordsTextField;
    private final PMSEButton filterBtn = new PMSEButton("Filter");

    public KeywordFilter(String caption) {
        this.keywordsTextField = new TextField(caption);
        setSpacing(true);
        setSizeUndefined();
        addComponent(keywordsTextField);
        addComponent(filterBtn);
        setComponentAlignment(filterBtn, Alignment.BOTTOM_CENTER);
        filterBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                onFilterBtnClick();
            }
        });
    }

    public List<String> getKeywords() {
        if(keywordsTextField.getValue() == null || ((String)keywordsTextField.getValue()).isEmpty())
            return null;
        List<String> keywords = new ArrayList<String>();
        for (String keyword : Arrays.asList(((String)keywordsTextField.getValue()).toLowerCase().split(",")))
            keywords.add(keyword.trim());
        return keywords;
    }

    abstract public void onFilterBtnClick();
}
