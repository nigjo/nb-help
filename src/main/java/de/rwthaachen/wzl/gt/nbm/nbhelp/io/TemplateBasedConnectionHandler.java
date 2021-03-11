package de.rwthaachen.wzl.gt.nbm.nbhelp.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.URLStreamHandlerRegistration;
import org.openide.util.Utilities;

import de.rwthaachen.wzl.gt.nbm.nbhelp.HelpProxy;
import de.rwthaachen.wzl.gt.nbm.nbhelp.HelpsetManager;
import de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpNavigationItem;
import de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpRenderContext;
import de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpTemplate;

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
  private static final Object CACHE_MUTEX = new Object();
  private static final String CACHE_BASE = HelpProxy.HELP_RESOURCES;
  

  @Override
  protected URLConnection openConnection(URL u) throws IOException
  {
    if(u.getPath().endsWith(".html")
        || u.getPath().endsWith(".htm"))
    {
      URLConnection c = new HelpContentConnection(u);
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

  private static class HelpContentConnection extends URLConnection
  {
    Map<String, String> headers = new TreeMap<>();
    private ByteArrayOutputStream result;
    private long contentLength = -1;
    private URLConnection cacheDelegate;

    protected HelpContentConnection(URL u)
    {
      super(u);
      useCaches = !Boolean.getBoolean("de.rwthaachen.wzl.gt.nbm.helpserver.nocache");
    }

    @Override
    public void connect() throws IOException
    {
      if(useCaches)
      {
        String cachePath = getCachePath();
        File cache = new File(Places.getCacheDirectory(), cachePath);
        if(cache.exists())
        {
          cacheDelegate = Utilities.toURI(cache).toURL().openConnection();
          cacheDelegate.connect();
          long cacheDate = cacheDelegate.getLastModified();
          if(hasChanged(url, cacheDate))
          {
            cacheDelegate = null;
          }
        }
      }

      if(cacheDelegate == null)
      {
        HelpTemplate template = Lookup.getDefault().lookup(HelpTemplate.class);
        result = new ByteArrayOutputStream();
        URL nbdocLocation = new URL("nbdocs:" + url.getPath());
        
        HelpRenderContext context =
            new HelpContentRenderer(nbdocLocation, result);

        long lastmodified = template.renderHelpPage(context);
        if(lastmodified > 0)
        {
          headers.putIfAbsent("last-modified",
              new java.util.Date(lastmodified).toString());
        }

        //result = resultBuffer.toString();
        if(useCaches)
        {
          synchronized(TemplateBasedConnectionHandler.CACHE_MUTEX)
          {
            try(OutputStream out = new FileOutputStream(
                Places.getCacheSubfile(getCachePath())))
            {
              byte[] buffer = result.toByteArray();
              contentLength = buffer.length;
              out.write(buffer);
            }
          }
        }
        if(contentLength < 0)
        {
          contentLength = result.size();
        }
        headers.put("content-length", Long.toString(contentLength));
      }
      //"content-type"
      //"content-encoding"
      //"expires"
      //"date"
      //"last-modified"
    }

    private String getCachePath()
    {
      return CACHE_BASE + "/" + url.getPath();
    }

    public String getHeaderField(String name)
    {
      if(cacheDelegate != null)
      {
        return cacheDelegate.getHeaderField(name);
      }
      return headers.get(name);
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
      if(cacheDelegate != null)
      {
        return cacheDelegate.getInputStream();
      }
      if(result != null)
      {
        return new ByteArrayInputStream(result.toByteArray());
      }
      return new ByteArrayInputStream(new byte[0]);
    }

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
    public List<HelpNavigationItem> getNavigationItems()
    {
      return Collections.emptyList();
    }

    @Override
    public URL findHelpId(String helpId)
    {
      //TODO: protocol change?
      return HelpsetManager.getManager().getHelpLocation(helpId);
    }

  }

}
