package com.publicationmetasearchengine.data.filters;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import java.util.ArrayList;

public class FilterCriteria {

    private final FilterType filterType;
    private final ArrayList<String> values;
    private final ComboCondition.Op innerOperator;
    private final ComboCondition.Op outerOperator;

    public FilterCriteria(FilterType filterType, ArrayList<String> values, ComboCondition.Op innerOperator, ComboCondition.Op outerOperator) {
        this.filterType = filterType;
        this.values = values;
        this.innerOperator = innerOperator;
        this.outerOperator = outerOperator;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public ComboCondition.Op getInnerOperator() {
        return innerOperator;
    }

    public ComboCondition.Op getOuterOperator() {
        return outerOperator;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", filterType.toString(), values==null?"null":values.toString());
    }
}
