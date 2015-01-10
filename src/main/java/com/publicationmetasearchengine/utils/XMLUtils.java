package com.publicationmetasearchengine.utils;

import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.data.filters.NamedFilterCriteria;
import com.thoughtworks.xstream.XStream;
import org.apache.log4j.Logger;

public class XMLUtils {

    private static final Logger LOGGER = Logger.getLogger(XMLUtils.class);

    private static XStream filterCriteriaXStream = null;

    private static void initializeFilterCriteriaXStream() {
        filterCriteriaXStream = new XStream();
        filterCriteriaXStream.alias("FilterCriterias", NamedFilterCriteria.class);
        filterCriteriaXStream.alias("FilterCriteria", FilterCriteria.class);
        filterCriteriaXStream.alias("value", String.class);
        filterCriteriaXStream.aliasField("type", FilterCriteria.class, "filterType");
        filterCriteriaXStream.addImplicitArray(FilterCriteria.class, "values");
        filterCriteriaXStream.addImplicitArray(NamedFilterCriteria.class, "filters");
        filterCriteriaXStream.useAttributeFor(FilterCriteria.class, "filterType");
        filterCriteriaXStream.useAttributeFor(FilterCriteria.class, "innerOperator");
        filterCriteriaXStream.useAttributeFor(FilterCriteria.class, "outerOperator");
        filterCriteriaXStream.useAttributeFor(NamedFilterCriteria.class, "name");
        filterCriteriaXStream.omitField(NamedFilterCriteria.class, "id");
        filterCriteriaXStream.omitField(NamedFilterCriteria.class, "lastSearchDate");
        filterCriteriaXStream.omitField(NamedFilterCriteria.class, "ownerId");
        LOGGER.info("FilterCriteria xStream initialized");
    }

    public static String serializeFilterCritaria(NamedFilterCriteria filterCriteria) {
        if (filterCriteriaXStream == null)
            initializeFilterCriteriaXStream();
        return filterCriteriaXStream.toXML(filterCriteria);
    }

    public static NamedFilterCriteria deserializeFilterCritaria(String filterCriteriaString) {
        if (filterCriteriaXStream == null)
            initializeFilterCriteriaXStream();
        return (NamedFilterCriteria) filterCriteriaXStream.fromXML(filterCriteriaString);
    }

}
