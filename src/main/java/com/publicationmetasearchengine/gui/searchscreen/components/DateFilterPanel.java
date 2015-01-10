package com.publicationmetasearchengine.gui.searchscreen.components;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.data.filters.FilterType;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.utils.DateUtils;
import com.publicationmetasearchengine.utils.Notificator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.VerticalLayout;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.apache.log4j.Logger;

public class DateFilterPanel extends PMSEPanel implements Filter {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DateFilterPanel.class);

    private final FilterType filterType;
    private final PopupDateField fromDateEd = new PopupDateField("From");
    private final PopupDateField toDateEd = new PopupDateField("To");
    private final Calendar dateFormatter = Calendar.getInstance();

    private final PMSEButton clearBtn = new PMSEButton("clear");


    public DateFilterPanel(String caption, FilterType filterType) {
        super(caption);
        this.filterType = filterType;
        setSizeUndefined();
        VerticalLayout mainLayout = new VerticalLayout();

        mainLayout.setSizeUndefined();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        fromDateEd.setDateFormat("yyyy-MM-dd");
        fromDateEd.setResolution(PopupDateField.RESOLUTION_DAY);
        toDateEd.setDateFormat("yyyy-MM-dd");
        toDateEd.setResolution(PopupDateField.RESOLUTION_DAY);
        clearBtn.setStyleName("link");
        clearBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                clear();
            }
        });


        mainLayout.addComponent(fromDateEd);
        mainLayout.addComponent(toDateEd);
        mainLayout.addComponent(clearBtn);
        mainLayout.setComponentAlignment(clearBtn, Alignment.MIDDLE_RIGHT);

        setContent(mainLayout);
    }



    @Override
    public FilterCriteria getFilterCriteria() {
        ArrayList<String> values = new ArrayList<String>(2);
        if (fromDateEd.getValue() != null)
            values.add(DateUtils.formatDate(formatFromDate((Date) fromDateEd.getValue())));
        else
            values.add(null);
        if (toDateEd.getValue() != null)
            values.add(DateUtils.formatDate(formatToDate((Date) toDateEd.getValue())));
        else
            values.add(null);
        return new FilterCriteria(filterType, values, null, ComboCondition.Op.AND);
    }

    @Override
    public void setFilterCriteria(FilterCriteria filterCriteria) {
        try {
            final ArrayList<String> values = filterCriteria.getValues();
            if (values.get(0) != null)
                fromDateEd.setValue(DateFormat.getDateInstance().parse(values.get(0)));
            if (values.get(1) != null)
                toDateEd.setValue(DateFormat.getDateInstance().parse(values.get(1)));
        } catch (ParseException ex) {
            LOGGER.fatal("Should not occure!!!", ex);
            Notificator.showNotification(getApplication(), "Error", "Internal error.\nTry again later.\nIf it repeates, please notify administrator.", Notificator.NotificationType.ERROR);
        }
    }

    private Date formatFromDate(Date fromDate) {
        dateFormatter.setTime(fromDate);
        dateFormatter.set(Calendar.HOUR_OF_DAY, 0);
        dateFormatter.set(Calendar.MINUTE, 0);
        dateFormatter.set(Calendar.SECOND, 0);
        return dateFormatter.getTime();
    }

    private Date formatToDate(Date toDate) {
        dateFormatter.setTime(toDate);
        dateFormatter.set(Calendar.HOUR_OF_DAY, 23);
        dateFormatter.set(Calendar.MINUTE, 59);
        dateFormatter.set(Calendar.SECOND, 59);
        return dateFormatter.getTime();
    }

    @Override
    public void clear() {
        fromDateEd.setValue(null);
        toDateEd.setValue(null);
    }


}
