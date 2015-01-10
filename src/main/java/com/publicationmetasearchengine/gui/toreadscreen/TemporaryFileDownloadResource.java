package com.publicationmetasearchengine.gui.toreadscreen;

import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.StreamResource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

//from https://vaadin.com/forum#!/thread/159584
public class TemporaryFileDownloadResource extends StreamResource {

  private final String filename;
  private String contentType;

  public TemporaryFileDownloadResource(Application application, String fileName, String contentType, File tempFile) throws FileNotFoundException {
    super(new FileStreamResource(tempFile), fileName, application);
    this.filename = fileName;
    this.contentType = contentType;
  }

  public DownloadStream getStream() {
    DownloadStream stream = new DownloadStream(getStreamSource().getStream(), contentType, filename);
    stream.setParameter("Content-Disposition", "attachment;filename=" + filename);
    // This magic incantation should prevent anyone from caching the data
    stream.setParameter("Cache-Control", "private,no-cache,no-store");
    // In theory <=0 disables caching. In practice Chrome, Safari (and, apparently, IE) all ignore <=0. Set to 1s
    stream.setCacheTime(1000);
    return stream;
  }

  private static class FileStreamResource implements
      StreamResource.StreamSource {
        private static final long serialVersionUID = 1L;

    private final InputStream inputStream;

    public FileStreamResource(File fileToDownload)  throws FileNotFoundException {
      inputStream = new DeletingFileInputStream(fileToDownload);
    }

    public InputStream getStream() {
      return inputStream;
    }
  }
}
/**
* This input stream deletes the given file when the InputStream is closed; intended to be used with temporary files.
*
*/
class DeletingFileInputStream extends FileInputStream {
  protected File file = null;
  public DeletingFileInputStream(File file) throws FileNotFoundException {
    super(file);
    this.file = file;
  }

  @Override
  public void close() throws IOException {
    super.close();
    file.delete();
  }
}