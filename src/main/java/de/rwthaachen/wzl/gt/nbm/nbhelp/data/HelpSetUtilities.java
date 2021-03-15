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
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.openide.filesystems.FileObject;
import org.openide.xml.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Helper class to load the "content" of a helpset. In this class all XML stuff is handled
 * and removed from the HelpSet class itself.
 *
 * @author Jens Hofschröer
 */
class HelpSetUtilities
{
  static void loadHelpset(HelpSet hsTarget) throws IOException
  {
    Document helpset = loadXml(hsTarget.getResourceLocation());

    Element docTitle =
        XMLUtil.findElement(helpset.getDocumentElement(), "title", null);
    if(docTitle != null)
    {
      hsTarget.setTitle(docTitle.getTextContent());
    }

    LinkedHashSet<View> views = null;
    Map<String, URL> mappings = new HashMap<>();
    hsTarget.setMappings(mappings);
    for(Element child : XMLUtil.findSubElements(helpset.getDocumentElement()))
    {
      if(child.getTagName().equals("maps"))
      {
        Element mapref = XMLUtil.findElement(child, "mapref", null);
        String maploc = mapref.getAttribute("location");
        URL ml = new URL(hsTarget.getResourceLocation(), maploc);
        Document map = loadXml(ml);
        for(Element id : XMLUtil.findSubElements(map.getDocumentElement()))
        {
          if(!id.getTagName().equals("mapID"))
          {
            continue;
          }

          String target = id.getAttribute("target");
          String url = id.getAttribute("url");
          mappings.putIfAbsent(target, new URL(ml, url));
          if(hsTarget.getPrimaryTarget() == null)
          {
            hsTarget.setPrimaryTarget(target);
          }
        }
      }
      else if(child.getTagName().equals("view"))
      {
        View view = new View(hsTarget);
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
        if(views == null)
        {
          views = new LinkedHashSet<>();
          hsTarget.setViews(views);
        }
        views.add(view);
      }
    }
  }

  static Document loadXml(final URL location) throws IOException
  {
    try(InputStream in = location.openStream())
    {
      return loadXml(in);
    }
  }

  public static Document loadXml(final InputStream in) throws IOException
  {
    try
    {
      return XMLUtil.parse(
          new InputSource(in), false, false, XMLUtil.defaultErrorHandler(),
          (publicId, systemId) -> new InputSource(new StringReader("")));
    }
    catch(SAXException ex)
    {
      throw new IOException(ex);
    }

  }

  private HelpSetUtilities()
  {
  }

  static URL checkForHelpset(FileObject helpsetFile)
      throws IOException
  {
    try(InputStream in = helpsetFile.getInputStream())
    {
      Document helpset = HelpSetUtilities.loadXml(in);
      Element result = helpset.getDocumentElement();
      if("helpsetref".equals(result.getTagName()))
      {
        String location = result.getAttribute("url");
        return new URL(location);
      }
    }
    return null;
  }

}
