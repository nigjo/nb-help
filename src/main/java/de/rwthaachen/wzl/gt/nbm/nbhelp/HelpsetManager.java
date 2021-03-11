package de.rwthaachen.wzl.gt.nbm.nbhelp;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.rwthaachen.wzl.gt.nbm.nbhelp.data.HelpSet;
import de.rwthaachen.wzl.gt.nbm.nbhelp.data.View;

/**
 * Eine neue Klasse von Jens Hofschröer. Erstellt Aug 24, 2020, 1:35:16 PM.
 *
 * @todo Hier fehlt die Beschreibung der Klasse.
 *
 * @author Jens Hofschröer
 */
public class HelpsetManager
{
  private static final HelpsetManager manager = new HelpsetManager();
  private Set<HelpSet> hss;
  private FileChangeListener watcher;

  public static HelpsetManager getManager()
  {
    return manager;
  }

  public static Collection<View> findViewsOfType(String viewType)
  {
    List<View> views = new ArrayList<>();
    for(HelpSet helpset : getManager().getHelpsets())
    {
      for(View view : helpset.getViews())
      {
        if(viewType.equals(view.getType()))
        {
          views.add(view);
        }
      }
    }
    return views;
  }

  public URL getHelpLocation(String id)
  {
    if(id == null)
    {
      return null;
    }

    for(HelpSet hs : getHelpsets())
    {
      URL result = hs.getHelpLocation(id);
      if(result != null)
      {
        return result;
      }
    }
    return null;
  }

  public synchronized Collection<HelpSet> getHelpsets()
  {
    ensureHelpsets();
    return new LinkedHashSet<>(hss);
  }

  private synchronized void resetHelpsets()
  {
    if(this.hss != null)
    {
      Logger.getLogger(HelpsetManager.class.getName())
          .log(Level.FINER, "resetting known helpset");
      this.hss = null;
    }
  }

  private synchronized void ensureHelpsets()
  {
    if(hss == null)
    {
      Logger.getLogger(HelpsetManager.class.getName())
          .log(Level.FINER, "start scanning for registered helpsets");
      hss = new LinkedHashSet<>();
      scanForHelpsets();
      Logger.getLogger(HelpsetManager.class.getName())
          .log(Level.FINER, "scanning finished.");
    }
  }

  private void scanForHelpsets()
  {
    //RequestProcessor scanner = new RequestProcessor("helpscanner");

    FileObject serviceRoot = FileUtil.getConfigFile("Services/JavaHelp");
    if(serviceRoot != null)
    {
      try
      {
        serviceRoot.setAttribute("watcherAt", new Date().toString());
        if(watcher == null)
        {
          watcher = new HelpRegistrationWatcher();
          serviceRoot.getFileSystem().addFileChangeListener(watcher);
        }
      }
      catch(IOException ex)
      {
        Exceptions.printStackTrace(ex);
      }

      Map<Locale, List<FileObject>> localizedHelpsets = getLocalizedHelpsets(serviceRoot);

      Locale current = Locale.getDefault();
      Set<Locale> localetests = new LinkedHashSet<>(Arrays.asList(
          Locale.forLanguageTag(current.getLanguage() + '-' + current.getCountry() + '-'
              + current.getVariant()),
          Locale.forLanguageTag(current.getLanguage() + '-' + current.getCountry()),
          Locale.forLanguageTag(current.getLanguage()),
          Locale.ROOT
      ));

      for(Locale localetest : localetests)
      {
        List<FileObject> localeHelpsets =
            localizedHelpsets.get(localetest);
        if(localeHelpsets == null || localeHelpsets.isEmpty())
        {
          continue;
        }
        for(FileObject helpsetFile : localeHelpsets)
        {
          try(InputStream in = helpsetFile.getInputStream())
          {
            Document helpset = XMLUtil.parse(
                new InputSource(in), false, false, XMLUtil.defaultErrorHandler(),
                (publicId, systemId) -> new InputSource(new StringReader("")));
            Element result = helpset.getDocumentElement();
            if("helpsetref".equals(result.getTagName()))
            {
              String location = result.getAttribute("url");
              hss.add(new HelpSet(new URL(location)));
            }
          }
          catch(SAXException | IOException ex)
          {
            Logger.getLogger(HelpsetManager.class.getName())
                .log(Level.WARNING, ex.toString(), ex);
          }
        }
      }
    }
  }

  private Map<Locale, List<FileObject>> getLocalizedHelpsets(FileObject serviceRoot)
  {
    Map<Locale, List<FileObject>> localizedHelpsets = new HashMap<>();
    for(FileObject helpsetFile : serviceRoot.getChildren())
    {
      String name = helpsetFile.getName();
      int underscore;
      Locale hsLocale;
      if((underscore = name.indexOf('_')) > 0)
      {
        String locale = name.substring(underscore + 1);
        hsLocale = Locale.forLanguageTag(locale.replace('_', '-'))
            .stripExtensions();
      }
      else
      {
        hsLocale = Locale.ROOT;
      }

      List<FileObject> localeHelpsets =
          localizedHelpsets.computeIfAbsent(hsLocale, key -> new ArrayList<>());
      localeHelpsets.add(helpsetFile);
    }
    return localizedHelpsets;
  }

  private class HelpRegistrationWatcher implements FileChangeListener
  {
    public HelpRegistrationWatcher()
    {
    }

    @Override
    public void fileFolderCreated(FileEvent fe)
    {
      resetHelpsets();
    }

    @Override
    public void fileDataCreated(FileEvent fe)
    {
      resetHelpsets();
    }

    @Override
    public void fileChanged(FileEvent fe)
    {
      resetHelpsets();
    }

    @Override
    public void fileDeleted(FileEvent fe)
    {
      resetHelpsets();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe)
    {
      resetHelpsets();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe)
    {
      //resetHelpsets();
    }

  }

}
