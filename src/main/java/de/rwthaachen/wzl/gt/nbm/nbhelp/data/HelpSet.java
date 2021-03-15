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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.util.Exceptions;


/**
 * Eine neue Klasse von Jens Hofschröer. Erstellt Aug 27, 2020, 9:43:02 AM.
 *
 * @todo Hier fehlt die Beschreibung der Klasse.
 *
 * @author Jens Hofschröer
 */
public class HelpSet
{
  private final URL resourceLocation;
  private String title;
  private String primaryTarget;

  private Map<String, URL> mappings;
  private Set<View> views;

  public HelpSet(URL resourceLocation)
  {
    this.resourceLocation = resourceLocation;
  }

  public URL getResourceLocation()
  {
    return resourceLocation;
  }

  public String getPrimaryTarget()
  {
    ensureData();
    return primaryTarget;
  }

  public URL getHelpLocation(String id)
  {
    ensureData();
    return mappings.get(id);
  }

  public Collection<View> getViews()
  {
    ensureData();
    return views;
  }

  public String getTitle()
  {
    ensureData();
    return title;
  }

  private void ensureData()
  {
    if(mappings == null)
    {
      try
      {
        HelpSetUtilities.loadHelpset(this);
      }
      catch(IOException ex)
      {
        if(ex.getCause() instanceof MissingResourceException)
        {
          Logger.getLogger(HelpSet.class.getName())
              .log(Level.WARNING, ex.getLocalizedMessage());
        }
        else
        {
          Exceptions.printStackTrace(ex);
        }
        mappings = Collections.emptyMap();
        views = Collections.emptySet();
        primaryTarget = null;
      }
    }
  }

  //Do not make this public!
  void setTitle(String title)
  {
    this.title = title;
  }

  //Do not make this public!
  void setPrimaryTarget(String primaryTarget)
  {
    this.primaryTarget = primaryTarget;
  }

  //Do not make this public!
  void setViews(Set<View> views)
  {
    this.views = views;
  }

  //Do not make this public!
  void setMappings(Map<String, URL> mappings)
  {
    this.mappings = mappings;
  }

}
