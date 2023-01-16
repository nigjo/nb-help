package de.rwthaachen.wzl.gt.nbm.nbhelp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.Toolkit;

import javax.swing.event.ChangeListener;

import org.netbeans.api.javahelp.Help;

import org.openide.awt.HtmlBrowser;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

import de.rwthaachen.wzl.gt.nbm.nbhelp.data.HelpsetManager;

/**
 * Schnittstelle der IDE-UI zur Hilfe. Diese Klasse ersetzt die bisherige "JavaHelp"
 * Klasse von NetBeans. Hier wird der WebProxy gestartet und die Hilfeseiten im
 * Standardbrowser des Anwenders geöffnet.
 *
 * @author Jens Hofschröer
 */
@ServiceProviders(
    {
      @ServiceProvider(service = HelpCtx.Displayer.class,
          supersedes = "org.netbeans.modules.javahelp.JavaHelp"),
      @ServiceProvider(service = Help.class,
          supersedes = "org.netbeans.modules.javahelp.JavaHelp")
    })
public class HelpDisplayer extends Help implements HelpCtx.Displayer
{
  public HelpDisplayer()
  {
    //Logger.getLogger(HelpDisplayer.class.getName()).log(Level.INFO, "HIIILLFFEEEE!!!");
  }

  public static void showPage(String page)
  {
    Help help = Lookup.getDefault().lookup(Help.class);
    help.showHelp(new HelpCtx(HelpProxy.HANDLER_BASED_PROTOCOL + ':'
        + (page.startsWith("/") ? "" : "/") + page));
  }

  @Override
  public Boolean isValidID(String id, boolean force)
  {
    Logger.getLogger(HelpDisplayer.class.getName()).log(Level.FINE,
        "validate help id {0}", id);
    if(id == null)
    {
      return false;
    }
    if(id.equals(HelpCtx.DEFAULT_HELP.getHelpID()))
    {
      return true;
    }
    if(force)
    {
      return null != HelpsetManager.getManager().getHelpLocation(id);
    }
    return null;
  }

  static HelpProxy helpprox;

  private static int ensureWebserver()
  {
    synchronized(HelpDisplayer.class)
    {
      if(helpprox == null)
      {
        RequestProcessor processor = new RequestProcessor(HelpDisplayer.class);
        helpprox = new HelpProxy();
        processor.post(helpprox);
      }
    }
    long maxwait = TimeUnit.SECONDS.toMillis(5);
    long start = System.currentTimeMillis();
    int port;
    do
    {
      port = helpprox.getPort();
      if(port == 0)
      {
        Logger.getLogger(HelpDisplayer.class.getName())
            .log(Level.WARNING, "may be no help server has started");
        return port;
      }
      else if(port < 0)
      {
        if(System.currentTimeMillis() - start > maxwait)
        {
          Logger.getLogger(HelpDisplayer.class.getName())
              .log(Level.WARNING, "unable to get server port");
          return port;
        }

        try
        {
          TimeUnit.MILLISECONDS.sleep(500l);
        }
        catch(InterruptedException ex)
        {
          Exceptions.printStackTrace(ex);
          break;
        }
      }
    }
    while(port < 0);
    return port;
  }

  @Override
  public void showHelp(HelpCtx ctx, boolean showmaster)
  {
    URL helpLocation;
    if(ctx.getHelpID().startsWith(HelpProxy.HANDLER_BASED_PROTOCOL + ":/"))
    {
      try
      {
        helpLocation = new URL("http", "localhost",
            ctx.getHelpID().substring(HelpProxy.HANDLER_BASED_PROTOCOL.length() + 1));
      }
      catch(MalformedURLException ex)
      {
        Logger.getLogger(HelpDisplayer.class.getName())
            .log(Level.WARNING, ex.toString());
        return;
      }
    }
    else
    {
      helpLocation = HelpsetManager.getManager()
          .getHelpLocation(ctx.getHelpID());
    }
    if(helpLocation != null)
    {
      try
      {
        int port = ensureWebserver();
        if(port > 0)
        {
          String file = helpLocation.getFile();
          URL browsable = new URL("http", "localhost", port, file);
          HtmlBrowser.URLDisplayer.getDefault().showURLExternal(browsable);
        }
      }
      catch(IOException ex)
      {
        Exceptions.printStackTrace(ex);
      }
    }
    else
    {
      Toolkit.getDefaultToolkit().beep();
    }
  }

  private ChangeSupport cs;

  @Override
  public void addChangeListener(ChangeListener l)
  {
    cs.addChangeListener(l);
  }

  @Override
  public void removeChangeListener(ChangeListener l)
  {
    cs.removeChangeListener(l);
  }

  @Override
  public boolean display(HelpCtx help)
  {
    if(Boolean.TRUE.equals(isValidID(help.getHelpID(), true)))
    {
      showHelp(help, false);
      return true;
    }
    else
    {
      return false;
    }
  }

}
