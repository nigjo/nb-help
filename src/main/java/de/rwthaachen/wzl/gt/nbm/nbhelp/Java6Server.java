package de.rwthaachen.wzl.gt.nbm.nbhelp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

import com.sun.net.httpserver.HttpServer;

import de.rwthaachen.wzl.gt.nbm.nbhelp.HelpProxy.ProxyServerProvider;

/**
 * Eigentlicher Webserver für die Hilfe auf Basis des Java6 Servers.
 *
 * @author Jens Hofschröer
 */
@ServiceProvider(service = ProxyServerProvider.class)
public class Java6Server implements ProxyServerProvider
{
  HttpServer server;

  @Override
  public int createServer() throws IOException
  {
    try
    {
      Class.forName("com.sun.net.httpserver.HttpServer");
      return createRealServer();
    }
    catch(ClassNotFoundException ex)
    {
      Exceptions.printStackTrace(ex);
    }

    return 0;
  }

  private int createRealServer() throws IOException
  {
    Logger serverLogger = Logger.getLogger("com.sun.net.httpserver");
    serverLogger.setUseParentHandlers(false);
    serverLogger.setLevel(Level.ALL);
    serverLogger.addHandler(new ServerLogs());
    server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    return server.getAddress().getPort();
  }

  @Override
  public void setHandler(HelpProxy.ProxyServerHandler requestHandler)
  {
    if(server == null)
    {
      return;
    }
    server.createContext("/", request ->
    {
      requestHandler.handleRequest(new HelpProxy.ProxyServerRequest()
      {
        @Override
        public URI getRequestURI()
        {
          return request.getRequestURI();
        }

        @Override
        public void setResponseCode(int code, int length) throws IOException
        {
          request.sendResponseHeaders(code, length);
        }

        @Override
        public void close()
        {
          request.close();
        }

        @Override
        public OutputStream getResponseBody()
        {
          return request.getResponseBody();
        }

      });
    });
  }

  @Override
  public void startServer()
  {
    if(server != null)
    {
      server.start();
    }
  }

  private static class ServerLogs extends Handler
  {
    public ServerLogs()
    {
      super.setLevel(Level.ALL);
    }

    @Override
    public void publish(LogRecord record)
    {
      //Logs einfach klauen
      record.setLoggerName(ServerLogs.class.getPackage().getName());
      Logger.getLogger(ServerLogs.class.getPackage().getName()).log(record);
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close() throws SecurityException
    {
    }

  }

}
