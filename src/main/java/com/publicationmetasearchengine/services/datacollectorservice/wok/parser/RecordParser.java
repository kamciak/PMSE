package com.publicationmetasearchengine.services.datacollectorservice.wok.parser;

import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.Author;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.CategoryInfo;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.RawRecord;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.SourceInfo;
import com.publicationmetasearchengine.utils.DateUtils;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class RecordParser {

    private final Document wokDoc;

    public RecordParser(String recordXml) {
        wokDoc = Jsoup.parse(recordXml);
    }

    public String getId() {
        return wokDoc.select("UID").first().text();
    }

    public String getTitle() {
        return wokDoc.select("title[type=item]").first().text();
    }

    public SourceInfo getSourceInfo() throws ParseException {
        String issue = wokDoc.select("pub_info").first().attr("issue");
        String pageRange = wokDoc.select("pub_info > page").first().text();
        return new SourceInfo(
                wokDoc.select("title[type=source]").first().text(),
                wokDoc.select("pub_info").first().attr("vol"),
                (issue==null ||issue.isEmpty())?null:issue,
                (pageRange==null || pageRange.isEmpty())?null:pageRange,
                DateUtils.parseDateOnly(wokDoc.select("pub_info").first().attr("sortdate")));

    }

    public List<Author> getAuthors() {
        List<Author> authors = new ArrayList<Author>();
        for(Element rawAuthor : wokDoc.select("summary>names>name")) {
            Element firstName = rawAuthor.select("first_name").first();
            Element lastName = rawAuthor.select("last_name").first();
            authors.add(new Author(
                    firstName!=null?firstName.text():null,
                    lastName!=null?lastName.text():null));
        }

        return authors;
    }

    public CategoryInfo getCategoryInfo() {
        CategoryInfo categoryInfo = new CategoryInfo();
        Element rawCategoryInfo = wokDoc.select("category_info").first();
        for(Element heading : rawCategoryInfo.select("headings > heading"))
            categoryInfo.addHeading(heading.text());
        for(Element subheading : rawCategoryInfo.select("subheadings > subheading"))
            categoryInfo.addSubheading(subheading.text());
        return categoryInfo;
    }

    public String getAbstract() {
        return wokDoc.select("abstract_text").text();
    }

    public String getDOI() {
        try {
            return wokDoc.select("identifier[type=doi]").first().attr("value");
        } catch (NullPointerException ex) {
            return null;
        }
    }

    public RawRecord getRecord() throws ParseException {
        return new RawRecord(
                getId(),
                getTitle(),
                getSourceInfo(),
                getAuthors(),
                getCategoryInfo(),
                getAbstract(),
                getDOI());
    }

}
