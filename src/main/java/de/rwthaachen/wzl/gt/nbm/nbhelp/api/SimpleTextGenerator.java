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
package de.rwthaachen.wzl.gt.nbm.nbhelp.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openide.modules.Places;
import org.openide.util.Utilities;

import de.rwthaachen.wzl.gt.nbm.nbhelp.HelpProxy;

/**
 * Helperclass to create a new URLConnection to text content.
 *
 * @author Jens Hofschröer
 */
public class SimpleTextGenerator extends URLConnection
{
  private static final Object CACHE_MUTEX = new Object();
  private static final String CACHE_BASE = HelpProxy.HELP_RESOURCES;
  private Map<String, String> headers = new TreeMap<>();
  private ByteArrayOutputStream result;
  private long contentLength = -1;
  private URLConnection cacheDelegate;
  private SimpleTextWriter contentWriter;

  @FunctionalInterface
  public static interface SimpleTextWriter
  {
    long writeContent(URL resourc, OutputStream out) throws IOException;

  }

  public SimpleTextGenerator(URL u, SimpleTextWriter contentWriter)
  {
    super(u);
    this.contentWriter = contentWriter;
    useCaches = !Boolean.getBoolean("de.rwthaachen.wzl.gt.nbm.helpserver.nocache");
  }

  private static boolean hasChanged(URL url, long cacheDate) throws IOException
  {
    URL nbdocLocation = new URL("nbdocs:" + url.getPath());
    long lastModified = nbdocLocation.openConnection().getLastModified();
    return lastModified > cacheDate || TimeUnit.DAYS.toMillis(2) < (System.
        currentTimeMillis() - cacheDate);
  }

  private String getCachePath()
  {
    return CACHE_BASE + "/" + url.getPath();
  }

  @Override
  public void connect() throws IOException
  {
    if(useCaches)
    {
      synchronized(CACHE_MUTEX)
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
    }
    if(cacheDelegate == null)
    {
      result = new ByteArrayOutputStream();
      long lastmodified = contentWriter.writeContent(url, result);
      if(lastmodified > 0)
      {
        headers.putIfAbsent("last-modified", new java.util.Date(lastmodified).toString());
      }
      if(useCaches)
      {
        synchronized(CACHE_MUTEX)
        {
          try(OutputStream out =
              new FileOutputStream(Places.getCacheSubfile(getCachePath())))
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

  @Override
  public String getHeaderField(String name)
  {
    return headers.get(name);
  }

  @Override
  public Map<String, List<String>> getHeaderFields()
  {
    return headers.entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey(), e -> Arrays.asList(e.getValue())));
  }

  @Override
  public long getContentLengthLong()
  {
    if(cacheDelegate != null)
    {
      return cacheDelegate.getContentLengthLong();
    }
    return super.getContentLengthLong();
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
