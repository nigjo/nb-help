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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Allows to render a special file in the root of the help server.
 *
 * An instance is registerd as a service in {@link HelpContentHandler#PATH}. The default
 * index page is registered at position 100. If one HelpContentHandler is accepting a
 * given path no other handler is asked.
 *
 * <p>
 * An implementation should not try to handle a file from a "nbdoc:" location. These are
 * already handled by the {@link HelpTemplate} implementations. All "files" below
 * {@code /nb-help/} are never handled by a {@code HelpTemplate} and can be used for
 * "static" resouces.
 *
 * <p>
 * This interface may be added to a {@code java.net.URLStreamHandler}.
 *
 * @author Jens Hofschröer
 */
public interface HelpContentHandler
{
  public static final String PATH = "de-rwthaachen-wzl-gt-nbm-helpserver";

  /**
   * Asked this Handler if it wants to handle a given path.
   *
   * @param path A "absolute" path from the root of the help-webserver.
   *
   * @return true if this handler is able to render the path.
   */
  public boolean acceptPath(String path);

  /**
   * Opens a connection to the rescource.
   *
   * @param u location of the resource to render. The URL is of a fictional "nbhelp:"
   * protocoll. See the "path" component to find the needed
   *
   * @return As defined by {@link java.net.URLStreamHandler#openConnection(java.net.URL)}
   *
   * @see java.net.URLStreamHandler#openConnection(java.net.URL)
   *
   * @throws IOException if an I/O error occurs while opening the connection.
   */
  public URLConnection openConnection(URL u) throws IOException;

}
