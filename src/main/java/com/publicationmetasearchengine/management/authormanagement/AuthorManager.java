package com.publicationmetasearchengine.management.authormanagement;

import com.publicationmetasearchengine.dao.authors.exceptions.AuthorAlreadyExistException;
import com.publicationmetasearchengine.dao.authors.exceptions.AuthorDoesNotExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationWithNoAuthorException;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.data.Publication;
import java.util.ArrayList;
import java.util.List;

public interface AuthorManager {

    Integer addNewAuthor(String name) throws AuthorAlreadyExistException;
    
    Integer addNewAuthor(Author author) throws AuthorAlreadyExistException;

    Integer getAuthorIdByName(String name) throws AuthorDoesNotExistException;

    Author getAuthorById(int authorId) throws AuthorDoesNotExistException;

    void setPublicationAuthorsIds(Integer publicationId, List<Integer> authorsIds);

    ArrayList<Author> getPublicationAuthors(Publication publication) throws PublicationWithNoAuthorException;
}
