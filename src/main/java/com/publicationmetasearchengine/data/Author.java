package com.publicationmetasearchengine.data;

public class Author {

    private final Integer id;
    private final String name;

    public Author(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("[%d: %s]", id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Author)
            return this.hashCode() == obj.hashCode();
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
