/*
 * Copyright 2021 Jens Hofschröer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.rwthaachen.wzl.gt.nbm.nbhelp.data;

import java.io.IOException;
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

/**
 * Eine neue Klasse von Jens Hofschröer. Erstellt Aug 24, 2020, 1:35:16 PM.
 *
 * @todo Hier fehlt die Beschreibung der Klasse.
 *
 * @author Jens Hofschröer
 */
public final class HelpsetManager
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
          try
          {
            URL location = HelpSetUtilities.checkForHelpset(helpsetFile);
            if(location != null)
            {
              hss.add(new HelpSet(location));
            }
          }
          catch(IOException ex)
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
