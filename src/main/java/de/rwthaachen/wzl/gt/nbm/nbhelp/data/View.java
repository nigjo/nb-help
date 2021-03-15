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
package de.rwthaachen.wzl.gt.nbm.nbhelp.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * A "View" inside a HelpSet.
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
