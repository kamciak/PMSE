package com.publicationmetasearchengine.dao.authors;

import com.publicationmetasearchengine.dao.authors.exceptions.AuthorAlreadyExistException;
import com.publicationmetasearchengine.dao.authors.exceptions.AuthorDoesNotExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationWithNoAuthorException;
import com.publicationmetasearchengine.data.Author;
import java.util.ArrayList;
import java.util.List;

public interface AuthorDAO {

    Integer insertAuthor(String name) throws AuthorAlreadyExistException;

    Author getAuthorById(int authorId) throws AuthorDoesNotExistException;

    Integer getAuthorId(String name) throws AuthorDoesNotExistException;

    ArrayList<Author> getPublicationAuthorsById(int publicationId) throws PublicationWithNoAuthorException;

    void setPublicationAuthorsById(int publicationId, List<Integer> authorsIds);

    void clearPublicationAuthors(int publicationId) throws PublicationWithNoAuthorException;
}
