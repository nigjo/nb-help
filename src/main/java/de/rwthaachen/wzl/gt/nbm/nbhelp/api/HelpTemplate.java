package de.rwthaachen.wzl.gt.nbm.nbhelp.api;

import java.io.IOException;

/**
 * Eine neue Schnittstelle von Jens Hofschröer. Erstellt Mar 10, 2021, 3:53:39 PM.
 *
 * @todo Hier fehlt die Beschreibung der Schnittstelle.
 *
 * @author Jens Hofschröer
 */
public interface HelpTemplate
{
  public long renderHelpPage(HelpRenderContext context)
      throws IOException;

}
