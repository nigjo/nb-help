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

import java.io.OutputStream;
import java.net.URL;
import java.util.List;

/**
 * Accessor class for all data a renderer needs to render a page. An instance of this
 * class should never be cached by any instance or class.
 *
 * @author Jens Hofschröer
 */
public interface HelpRenderContext
{
  /**
   * Current location of the original help page inside NetBeans. This location should only
   * be used to read the original help page. The location inside the users browser will be
   * different.
   *
   * @return A {@code nbdocs:} location.
   */
  public URL getHelpResource();

  /**
   * Get the output to write the rendered page to.
   *
   * @return Stream to write the rendered page to.
   */
  public OutputStream getOuput();

  /**
   * Not implemented, yet.
   * 
   * @return Always an empty list.
   */
  public List<HelpNavigationItem> getNavigationItems();

  /**
   * Not implemented, yet. Find another nbdocs: location for a given help ID.
   *
   * @param helpId help ID to search.
   *
   * @return found location for the help ID or {@code null} if nothing matching is found.
   */
  public URL findHelpId(String helpId);

}
