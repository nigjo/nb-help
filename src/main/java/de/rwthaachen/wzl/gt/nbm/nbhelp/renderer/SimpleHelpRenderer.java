package de.rwthaachen.wzl.gt.nbm.nbhelp.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

import de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpRenderContext;
import de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpTemplate;

/**
 * Ein einfacher Renderer, der einfach nur alle Seiten 1:1 weiter reicht.
 *
 * @author Jens Hofschröer
 */
@ServiceProvider(service = HelpTemplate.class)
public class SimpleHelpRenderer implements HelpTemplate
{
  @Override
  public long renderHelpPage(HelpRenderContext context)
      throws IOException
  {
    URL helpResource = context.getHelpResource();
    try(OutputStream out = context.getOuput();
        InputStream in = helpResource.openStream())
    {
      FileUtil.copy(in, out);

      return System.currentTimeMillis();
    }
  }

}
