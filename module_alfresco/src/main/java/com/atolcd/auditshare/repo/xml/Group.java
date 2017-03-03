package com.atolcd.auditshare.repo.xml;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
