package com.atolcd.auditshare.repo.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "Groups" })
@XmlRootElement(name = "referentiel-groups")
public class ReferentielGroups implements Serializable {

  private static final long serialVersionUID = 1205873923209186977L;
  @XmlElement(name = "group")
  protected List<Group>     Groups;

  /**
   * Gets the value of the Groups property.
   *
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list
   * will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the Groups property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   *
   * <pre>
   * getGroups().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list {@link Group }
   *
   *
   */
  public List<Group> getGroups() {
    if (Groups == null) {
      Groups = new ArrayList<Group>();
    }
    return this.Groups;
  }

}
