package de.rwthaachen.wzl.gt.nbm.nbhelp.api;

import java.io.OutputStream;
import java.net.URL;
import java.util.List;

/**
 * Eine neue Klasse von Jens Hofschröer. Erstellt Mar 10, 2021, 3:54:25 PM.
 *
 * @todo Hier fehlt die Beschreibung der Klasse.
 *
 * @author Jens Hofschröer
 */
public interface HelpRenderContext
{
  public URL getHelpResource();

  public OutputStream getOuput();

  public List<HelpNavigationItem> getNavigationItems();

  public URL findHelpId(String helpId);

}
