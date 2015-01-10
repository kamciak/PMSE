package com.publicationmetasearchengine.data.filters;

import java.util.Date;
import java.util.List;

public class NamedFilterCriteria {

    private final List<FilterCriteria> filters;
    private final String name;
    private Integer id;
    private Integer ownerId;
    private Date lastSearchDate;

    public NamedFilterCriteria(List<FilterCriteria> filters, String name) {
        this.filters = filters;
        this.name = name;
        this.id = null;
        this.ownerId = null;
        this.lastSearchDate = null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getLastSearchDate() {
        return lastSearchDate;
    }

    public void setLastSearchDate(Date lastSearchDate) {
        this.lastSearchDate = lastSearchDate;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public List<FilterCriteria> getFilters() {
        return filters;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("[%s - %s]", id==null?"New":""+id,name);
    }
}
