package com.publicationmetasearchengine.management.authormanagement;

import com.publicationmetasearchengine.dao.authors.AuthorDAO;
import com.publicationmetasearchengine.dao.authors.exceptions.AuthorAlreadyExistException;
import com.publicationmetasearchengine.dao.authors.exceptions.AuthorDoesNotExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationWithNoAuthorException;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.data.Publication;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;


@Component
@Configurable(preConstruction = true)
public class AuthorManagerImpl implements AuthorManager {
    private static final Logger LOGGER = Logger.getLogger(AuthorManagerImpl.class);

    @Autowired
    AuthorDAO authorDAO;

    @Override
    public Integer addNewAuthor(String name) throws AuthorAlreadyExistException {
        Integer result = authorDAO.insertAuthor(name);
        LOGGER.info(String.format("Author %s added to database under ID: %d", name, result));
        return result;
    }

    @Override
    public Author getAuthorById(int authorId) throws AuthorDoesNotExistException {
        Author author = authorDAO.getAuthorById(authorId);
        LOGGER.debug(String.format("Author %s requested from databae", author));
        return author;
    }

    @Override
    public Integer getAuthorIdByName(String name) throws AuthorDoesNotExistException {
        Integer result = authorDAO.getAuthorId(name);
        LOGGER.debug(String.format("Id for author [%s]: %d", name, result));
        return result;
    }

    @Override
    public ArrayList<Author> getPublicationAuthors(Publication publication) throws PublicationWithNoAuthorException {
        final ArrayList<Author> authors = authorDAO.getPublicationAuthorsById(publication.getId());
        LOGGER.debug(String.format("Found %d authors for publication %s", authors.size(), publication));
        return authors;
    }

    @Override
    public void setPublicationAuthorsIds(Integer publicationId, List<Integer> authorsIds) {
        authorDAO.setPublicationAuthorsById(publicationId, authorsIds);
        LOGGER.debug(String.format("Publication %d authors set to: %s", publicationId, authorsIds));
    }
}
