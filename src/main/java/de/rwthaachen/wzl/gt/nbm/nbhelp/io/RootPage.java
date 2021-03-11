package de.rwthaachen.wzl.gt.nbm.nbhelp.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import de.rwthaachen.wzl.gt.nbm.nbhelp.HelpsetManager;
import de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpContentHandler;
import de.rwthaachen.wzl.gt.nbm.nbhelp.data.HelpSet;

/**
 * Eine neue Klasse von Jens Hofschröer. Erstellt Mar 10, 2021, 1:17:37 PM.
 *
 * @todo Hier fehlt die Beschreibung der Klasse.
 *
 * @author Jens Hofschröer
 */
@ServiceProvider(service = HelpContentHandler.class,
    path = HelpContentHandler.PATH, position = 100)
public class RootPage implements HelpContentHandler
{
  @Override
  public boolean acceptPath(String path)
  {
    return "/index.html".equals(path);
  }

  @Override
  @NbBundle.Messages("RootPage.header=Known Helpsets")
  public URLConnection openConnection(URL u) throws IOException
  {
    return new URLConnection(u)
    {
      String fullPage;

      @Override
      public void connect() throws IOException
      {
        //nothing to connect to
        StringBuilder pageContent = new StringBuilder();
        pageContent.append(String.format("<header><h1>%s</h1></header>",
            Bundle.RootPage_header()));

        pageContent.append("<main><ul>");
        for(HelpSet helpset : HelpsetManager.getManager().getHelpsets())
        {
          pageContent.append("<li>");
          String primaryTarget = helpset.getPrimaryTarget();
          URL target;
          if(primaryTarget != null)
          {
            target = helpset.getHelpLocation(primaryTarget);
          }
          else
          {
            URL loc = helpset.getResourceLocation();
            target = new URL(loc, "./");
          }
          pageContent.append("<a href=\"")
              .append(target.getPath())
              .append("\">")
              .append(helpset.getTitle())
              .append("</a>");
          pageContent.append("</li>");
        }
        pageContent.append("</ul></main>");

        fullPage = String.format(
            "<html><head>"
            + "<meta charset=\"UTF-8\"><title>%s</title>"
            + "</head><body>%s</body></html>",
            Bundle.RootPage_header(), pageContent.toString()
        );
      }

      @Override
      public int getContentLength()
      {
        return fullPage.getBytes(StandardCharsets.UTF_8).length;
      }

      @Override
      public String getContentType()
      {
        return "text/html";
      }

      @Override
      public InputStream getInputStream() throws IOException
      {
        return new ByteArrayInputStream(fullPage.getBytes(StandardCharsets.UTF_8));
      }

    };
  }

}
