/*
 * Copyright (C) 2013 Atol Conseils et Développements.
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
package com.atolcd.alfresco.repo.template;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.service.cmr.repository.MalformedNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class ShareStatsUtils extends BaseTemplateProcessorExtension implements InitializingBean {
  private static final Log logger = LogFactory.getLog(ShareStatsUtils.class);

  private NodeService      nodeService;
  private PersonService    personService;
  private SiteService      siteService;

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(nodeService, "There must be a NodeService");
    Assert.notNull(personService, "There must be a PersonService");
    Assert.notNull(siteService, "There must be a SiteService");
  }

  public String getPersonFullName(String username) {
    if (this.personService.personExists(username)) {
      NodeRef person = this.personService.getPerson(username);

      String firstName = (String) this.nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
      String lastName = (String) this.nodeService.getProperty(person, ContentModel.PROP_LASTNAME);

      return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName.toUpperCase() : "");
    }
    return "";
  }

  public String getSiteTitle(String shortName) {
    SiteInfo si = this.siteService.getSite(shortName);
    if (si != null) {
      return si.getTitle();
    }
    return "";
  }

  public String getDocumentSite(String documentRef) {
    try {
      NodeRef nodeRef = new NodeRef(documentRef);
      if (this.nodeService.exists(nodeRef)) {
        SiteInfo si = this.siteService.getSite(nodeRef);
        if (si != null) {
          return si.getShortName();
        }
      }
    } catch (MalformedNodeRefException mne) {
      logger.warn(mne);
    }
    return null;
  }
}
