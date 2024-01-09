/*
 * Copyright (C) 2018 Atol Conseils et Développements.
 * http://www.atolcd.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atolcd.auditshare.repo.xml;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "id", "libelle" })
@XmlRootElement(name = "group")
public class Group implements Serializable {

  private static final long serialVersionUID = 3626364911766188408L;
  @XmlElement(required = true)
  protected String          id;
  @XmlElement(required = true)
  protected String          libelle;

  /**
   * Gets the value of the id property.
   *
   * @return possible object is {@link String }
   *
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the value of the id property.
   *
   * @param value allowed object is {@link String }
   *
   */
  public void setId(String value) {
    this.id = value;
  }

  /**
   * Gets the value of the libelle property.
   *
   * @return possible object is {@link String }
   *
   */
  public String getLibelle() {
    return libelle;
  }

  /**
   * Sets the value of the libelle property.
   *
   * @param value allowed object is {@link String }
   *
   */
  public void setLibelle(String value) {
    this.libelle = value;
  }

}
