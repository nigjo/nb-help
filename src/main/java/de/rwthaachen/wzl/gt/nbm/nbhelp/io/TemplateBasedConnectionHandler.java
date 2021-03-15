package de.rwthaachen.wzl.gt.nbm.nbhelp.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openide.util.Lookup;
import org.openide.util.URLStreamHandlerRegistration;

import de.rwthaachen.wzl.gt.nbm.nbhelp.HelpProxy;
import de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpNavigationItem;
import de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpRenderContext;
import de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpTemplate;
import de.rwthaachen.wzl.gt.nbm.nbhelp.api.SimpleTextGenerator;
import de.rwthaachen.wzl.gt.nbm.nbhelp.data.HelpsetManager;

/**
 * Eine neue Klasse von Jens Hofschröer. Erstellt Mar 10, 2021, 4:07:33 PM.
 *
 * @todo Hier fehlt die Beschreibung der Klasse.
 *
 * @author Jens Hofschröer
 */
@URLStreamHandlerRegistration(protocol = HelpProxy.HELP_PAGE_PROTOCOL)
public class TemplateBasedConnectionHandler extends URLStreamHandler
{
  @Override
  protected URLConnection openConnection(URL u) throws IOException
  {
    if(u.getPath().endsWith(".html")
        || u.getPath().endsWith(".htm"))
    {
      URLConnection c = new SimpleTextGenerator(
          u, this::writeTemplatedContent);
      c.setDoInput(true);
      c.setDoOutput(false);
      return c;
    }
    else
    {
      //all other resources
      URL ressource = new URL("nbdocs", "", u.getPath());
      return ressource.openConnection();
    }
  }

  protected static boolean hasChanged(URL url, long cacheDate) throws IOException
  {
    URL nbdocLocation = new URL("nbdocs:" + url.getPath());
    long lastModified = nbdocLocation.openConnection().getLastModified();
    return lastModified > cacheDate
        || TimeUnit.DAYS.toMillis(2) < (System.currentTimeMillis() - cacheDate);
  }

  private long writeTemplatedContent(URL url, OutputStream result)
      throws IOException
  {
    HelpTemplate template = Lookup.getDefault().lookup(HelpTemplate.class);
    URL nbdocLocation = new URL("nbdocs:" + url.getPath());

    HelpRenderContext context =
        new HelpContentRenderer(nbdocLocation, result);

    return template.renderHelpPage(context);
  }

  private static class HelpContentRenderer implements HelpRenderContext
  {
    private final URL resource;
    private OutputStream delegateOutput;

    public HelpContentRenderer(URL resource, OutputStream delegateOutput)
    {
      this.resource = resource;
      this.delegateOutput = delegateOutput;
    }

    @Override
    public URL getHelpResource()
    {
      return this.resource;
    }

    @Override
    public OutputStream getOuput()
    {
      return delegateOutput;
    }

    @Override
    @Deprecated
    public List<HelpNavigationItem> getNavigationItems()
    {
      return Collections.emptyList();
    }

    @Override
    @Deprecated
    public URL findHelpId(String helpId)
    {
      //TODO: protocol change?
      return HelpsetManager.getManager().getHelpLocation(helpId);
    }

  }

}
