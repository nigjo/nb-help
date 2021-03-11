package de.rwthaachen.wzl.gt.nbm.nbhelp.api;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Eine neue Schnittstelle von Jens Hofschröer. Erstellt Mar 10, 2021, 10:56:12 AM.
 *
 * @todo Hier fehlt die Beschreibung der Schnittstelle.
 *
 * @author Jens Hofschröer
 */
public interface HelpContentHandler
{
  public static final String PATH = "de-rwthaachen-wzl-gt-nbm-helpserver";

  public boolean acceptPath(String path);

  public URLConnection openConnection(URL u) throws IOException;

}
