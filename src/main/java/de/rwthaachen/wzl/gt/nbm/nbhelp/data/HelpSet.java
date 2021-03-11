package de.rwthaachen.wzl.gt.nbm.nbhelp.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

  private void ensureData()
  {
    if(mappings == null)
    {
      try
      {
        loadHelpset();
      }
      catch(SAXException | IOException ex)
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

  private void loadHelpset() throws SAXException, IOException
  {
    Document helpset;
    try(InputStream in = resourceLocation.openStream())
    {
      helpset = XMLUtil.parse(
          new InputSource(in), false, false, XMLUtil.defaultErrorHandler(),
          (publicId, systemId) -> new InputSource(new StringReader("")));
    }

    Element docTitle =
        XMLUtil.findElement(helpset.getDocumentElement(), "title", null);
    if(docTitle != null)
    {
      this.title = docTitle.getTextContent();
    }

    mappings = new HashMap<>();
    for(Element child : XMLUtil.findSubElements(helpset.getDocumentElement()))
    {
      if(child.getTagName().equals("maps"))
      {
        Element mapref = XMLUtil.findElement(child, "mapref", null);
        String maploc = mapref.getAttribute("location");
        URL ml = new URL(resourceLocation, maploc);
        try(InputStream in = ml.openStream())
        {
          Document map = XMLUtil.parse(
              new InputSource(in), false, false, XMLUtil.defaultErrorHandler(),
              (publicId, systemId) -> new InputSource(new StringReader("")));

          for(Element id : XMLUtil.findSubElements(map.getDocumentElement()))
          {
            if(!id.getTagName().equals("mapID"))
            {
              continue;
            }

            String target = id.getAttribute("target");
            String url = id.getAttribute("url");
            mappings.putIfAbsent(target, new URL(ml, url));
            if(primaryTarget == null)
            {
              primaryTarget = target;
            }
          }
        }
      }
      else if(child.getTagName().equals("view"))
      {
        View view = new View(this);
        for(Element attr : XMLUtil.findSubElements(child))
        {
          switch(attr.getTagName())
          {
            case "name":
              view.setName(attr.getTextContent());
              break;
            case "label":
              view.setLabel(attr.getTextContent());
              break;
            case "type":
              view.setType(attr.getTextContent());
              break;
            case "data":
              view.setData(attr.getTextContent());
              break;
          }
        }
        if(this.views == null)
        {
          this.views = new LinkedHashSet<>();
        }
        this.views.add(view);
      }
    }
  }

}
