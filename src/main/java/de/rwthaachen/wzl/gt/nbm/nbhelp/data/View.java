package de.rwthaachen.wzl.gt.nbm.nbhelp.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Eine neue Klasse von Jens Hofschröer. Erstellt Aug 28, 2020, 8:19:58 AM.
 *
 * @todo Hier fehlt die Beschreibung der Klasse.
 *
 * @author Jens Hofschröer
 */
public class View
{
  private final HelpSet parent;
  private String name;
  private String label;
  private String type;
  private String data;

  View(HelpSet parent)
  {
    this.parent = parent;
  }

  public HelpSet getParent()
  {
    return parent;
  }

  public String getName()
  {
    return name;
  }

  void setName(String name)
  {
    this.name = name;
  }

  public String getLabel()
  {
    return label;
  }

  void setLabel(String label)
  {
    this.label = label;
  }

  public String getType()
  {
    return type;
  }

  void setType(String type)
  {
    this.type = type;
  }

  public String getData()
  {
    return data;
  }

  void setData(String data)
  {
    this.data = data;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 29 * hash + Objects.hashCode(this.parent);
    hash = 29 * hash + Objects.hashCode(this.name);
    hash = 29 * hash + Objects.hashCode(this.type);
    hash = 29 * hash + Objects.hashCode(this.data);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj)
    {
      return true;
    }
    if(obj == null)
    {
      return false;
    }
    if(getClass() != obj.getClass())
    {
      return false;
    }
    final View other = (View)obj;
    if(!Objects.equals(this.name, other.name))
    {
      return false;
    }
    if(!Objects.equals(this.type, other.type))
    {
      return false;
    }
    if(!Objects.equals(this.data, other.data))
    {
      return false;
    }
    if(!Objects.equals(this.parent, other.parent))
    {
      return false;
    }
    return true;
  }

  public URL getRelativeDataLocation() throws MalformedURLException
  {
    return new URL(parent.getResourceLocation(), data);
  }

}
