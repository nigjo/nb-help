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

/**
 * A "Renderer" for HTML Help Pages. The default implementation will forward the plain
 * HTML Page as it is stored under the {@code nbdocs:} location.
 *
 * To replace the the default implementation you have to register a new
 * {@code @ServiceProvider} that supersedes
 * {@code de.rwthaachen.wzl.gt.nbm.nbhelp.renderer.SimpleHelpRenderer}
 *
 * @author Jens Hofschröer
 */
public interface HelpTemplate
{
  /**
   * Renders a help page to the local browser.
   *
   * @param context the render context provides all data needed to render the page.
   *
   * @return timestamp of last modification
   *
   * @throws IOException any problem while reading or writing stuff.
   */
  public long renderHelpPage(HelpRenderContext context)
      throws IOException;

}
