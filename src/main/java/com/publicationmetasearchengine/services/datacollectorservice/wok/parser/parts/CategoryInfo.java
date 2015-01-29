package com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts;

import java.util.ArrayList;
import java.util.List;

public class CategoryInfo {

    private final List<String> headings = new ArrayList<String>();
    private final List<String> subheadings = new ArrayList<String>();

    public void addHeading(String heading) {
        headings.add(heading);
    }

    public void addSubheading(String subheading) {
        subheadings.add(subheading);
    }

    public List<String> getHeadings() {
        return headings;
    }

    public List<String> getSubheadings() {
        return subheadings;
    }
}
