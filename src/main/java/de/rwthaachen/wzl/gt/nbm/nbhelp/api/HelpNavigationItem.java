package de.rwthaachen.wzl.gt.nbm.nbhelp.api;

import java.net.URL;
import java.util.List;

/**
 * Eine neue Klasse von Jens Hofschröer. Erstellt Mar 10, 2021, 3:57:42 PM.
 *
 * @todo Hier fehlt die Beschreibung der Klasse.
 *
 * @author Jens Hofschröer
 */
public interface HelpNavigationItem
{
  public URL getTraget();

  public String getTitle();

  public List<HelpNavigationItem> getChildren();

}
