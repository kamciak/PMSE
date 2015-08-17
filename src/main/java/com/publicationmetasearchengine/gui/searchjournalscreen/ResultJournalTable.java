/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.gui.searchjournalscreen;

import com.vaadin.ui.Table;
import java.math.BigDecimal;

/**
 *
 * @author Kamciak
 */
public class ResultJournalTable extends Table {
    private static final long serialVersionUID = 1L;

    public static final String TABLE_JOURNAL_COLUMN = "Journal title";
    public static final String TABLE_JOURNAL_COUNTER_COLUMN = "No. of articles";
    public static final String TABLE_JOURNAL_IMPACT_FACTOR_COLUMN_CURRENT = "Current impact factor";
    public static final String TABLE_JOURNAL_IMPACT_FACTOR_COLUMN_AVERAGE = "Average impact factor";
    private static final String[] TABLE_VISIBLE_COLUMNS = {
        TABLE_JOURNAL_COLUMN,
        TABLE_JOURNAL_COUNTER_COLUMN,
        TABLE_JOURNAL_IMPACT_FACTOR_COLUMN_CURRENT,
        TABLE_JOURNAL_IMPACT_FACTOR_COLUMN_AVERAGE
    };

    public ResultJournalTable() {
        addContainerProperty(TABLE_JOURNAL_COLUMN, String.class, "");
        addContainerProperty(TABLE_JOURNAL_COUNTER_COLUMN, Integer.class, "");
        addContainerProperty(TABLE_JOURNAL_IMPACT_FACTOR_COLUMN_CURRENT, Double.class, "");
        addContainerProperty(TABLE_JOURNAL_IMPACT_FACTOR_COLUMN_AVERAGE, Double.class, "");
        setVisibleColumns(TABLE_VISIBLE_COLUMNS);
    }

    public void clear() {
        removeAllItems();
    }

    public void addJournal(String title, int numberOfArticles, Float [] impactFactor) {
        Object id = addItem();
        getItem(id).getItemProperty(TABLE_JOURNAL_COLUMN).setValue(title);
        getItem(id).getItemProperty(TABLE_JOURNAL_COUNTER_COLUMN).setValue(numberOfArticles);
        getItem(id).getItemProperty(TABLE_JOURNAL_IMPACT_FACTOR_COLUMN_CURRENT).setValue(round(impactFactor[0], 3, BigDecimal.ROUND_HALF_UP));
        getItem(id).getItemProperty(TABLE_JOURNAL_IMPACT_FACTOR_COLUMN_AVERAGE).setValue(round(impactFactor[1], 3, BigDecimal.ROUND_HALF_UP));
    }
    
private double round(double unrounded, int precision, int roundingMode)
{
    BigDecimal bd = new BigDecimal(unrounded);
    BigDecimal rounded = bd.setScale(precision, roundingMode);
    return rounded.doubleValue();
}

}
