package de.rwthaachen.wzl.gt.nbm.nbhelp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

import de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpContentHandler;

/**
 * Verwaltung für den eigentlichen Webserver für die Hilfeseiten. Alle Anfragen des
 * Browsers werden über die Methode {@link #handleRequest(ProxyServerRequest)}
 * abgewickelt. Der eigentliche {@linkplain ProxyServerProvider Server} muss über 
 * die Services registriert werden.
 *
 * @author Jens Hofschröer
 */
@NbBundle.Messages("de.rwthaachen.wzl.gt.nbm.helpserver.resources=nb-help")
public class HelpProxy implements Runnable
{
  public static final String HANDLER_BASED_PROTOCOL = "nbhelpres";
  public static final String HELP_PAGE_PROTOCOL = "nbhelp";
  public static final String HELP_RESOURCES =
      Bundle.de_rwthaachen_wzl_gt_nbm_helpserver_resources();

  private static Map<String, URLStreamHandler> pathHandlers;

  private ProxyServerProvider serverProvider;
  private int port = -1;

  public static interface ProxyServerRequest
  {
    public URI getRequestURI();

    public void setResponseCode(int code, int length) throws IOException;

    public void close();

    public OutputStream getResponseBody();

  }

  @FunctionalInterface
  public static interface ProxyServerHandler
  {
    public void handleRequest(ProxyServerRequest request) throws IOException;

  }

  public static interface ProxyServerProvider
  {
    public int createServer() throws IOException;

    public void setHandler(ProxyServerHandler requestHandler);

    public void startServer();

  }

  public HelpProxy()
  {
    try
    {
      serverProvider = Lookup.getDefault().lookup(ProxyServerProvider.class);
    }
    catch(NoClassDefFoundError | Exception ex)
    {
      Logger.getLogger(HelpProxy.class.getName())
          .log(Level.WARNING, ex.toString(), ex);
      serverProvider = null;
      this.port = 0;
    }
  }

  @Override
  public void run()
  {
    if(serverProvider == null)
    {
      this.port = 0;
      return;
    }
    try
    {
      this.port = serverProvider.createServer();

      if(port > 0)
      {
        serverProvider.setHandler(this::handleRequest);

        Logger.getLogger(HelpProxy.class.getName()).log(Level.INFO,
            "starting help server at port {0}", port);
        serverProvider.startServer();
      }
    }
    catch(IOException | NoClassDefFoundError ex)
    {
      Exceptions.printStackTrace(ex);
      this.port = 0;
    }
    finally
    {
      if(port < 0)
      {
        port = 0;
      }
    }

  }

  public int getPort()
  {
    return port;
  }

  private void handleRequest(ProxyServerRequest exchange)
      throws IOException
  {
    URI request = exchange.getRequestURI();

    URL doc;
    String path = request.getPath()
        .replace("/./", "/")
        .replace("/../", "/")
        .replace("//", "/");
    if(path.endsWith("/"))
    {
      path += "index.html";
    }

    if(pathHandlers != null && pathHandlers.containsKey(path))
    {
      doc = new URL(HANDLER_BASED_PROTOCOL, request.getHost(), request.getPort(),
          path, pathHandlers.get(path));
    }
    else if(path.indexOf('/', 1) < 0
        || path.startsWith('/' + HELP_RESOURCES + '/'))
    {
      //Sonderbehandlung fuer alles im "root". Kein HELP_PAGE_PROTOCOL
      URLStreamHandler handler = findContentHandler(path, request);
      if(handler != null)
      {
        doc = new URL(HANDLER_BASED_PROTOCOL, request.getHost(), request.getPort(),
            path, handler);
      }
      else
      {
        doc = null;
      }
    }
    else
    {
      doc = new URL(HELP_PAGE_PROTOCOL + ":" + path);
    }

    if(doc == null)
    {
      writeErrorResponse(exchange, 404, path);
    }
    else
    {
      URLConnection docfile = doc.openConnection();
      int length = 0;
      try
      {
        docfile.connect();
        length = docfile.getContentLength();
      }
      catch(IOException | RuntimeException ex)
      {
        if(ex.getCause() instanceof MissingResourceException)
        {
          writeErrorResponse(exchange, 404, path);
        }
        else
        {
          Exceptions.printStackTrace(ex);
          writeErrorResponse(exchange, 500, path);
        }
        docfile = null;
      }
      if(docfile != null)
      {
        if(length <= 0)
        {
          writeErrorResponse(exchange, 404, doc.getPath());
        }
        else
        {
          exchange.setResponseCode(200, length);

          try(InputStream in = docfile.getInputStream())
          {
            FileUtil.copy(in, exchange.getResponseBody());
          }
        }
      }
    }
    exchange.close();
  }

  public URLStreamHandler findContentHandler(String path, URI request) throws
      MalformedURLException
  {
    Collection<? extends HelpContentHandler> contentHandlers =
        Lookups.forPath(HelpContentHandler.PATH)
            .lookupAll(HelpContentHandler.class);
    for(HelpContentHandler contentHandler : contentHandlers)
    {
      if(contentHandler.acceptPath(path))
      {
        URLStreamHandler handler;
        if(contentHandler instanceof URLStreamHandler)
        {
          handler = (URLStreamHandler)contentHandler;
        }
        else
        {
          handler = new URLStreamHandler()
          {
            @Override
            protected URLConnection openConnection(URL u) throws IOException
            {
              return contentHandler.openConnection(u);
            }

          };
        }
        if(pathHandlers == null)
        {
          pathHandlers = new HashMap<>();
        }
        pathHandlers.put(path, handler);
        return handler;
      }
    }
    return null;
  }

  private void writeErrorResponse(
      ProxyServerRequest exchange, int code, String path)
      throws IOException
  {
    exchange.setResponseCode(code, 0);

    try(OutputStream out = exchange.getResponseBody())
    {
      out.write(("Resource not found " + path)
          .getBytes(StandardCharsets.UTF_8));
    }
  }

}
