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
package com.atolcd.auditshare.repo.service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atolcd.auditshare.repo.xml.Group;
import com.atolcd.auditshare.repo.xml.ReferentielGroups;

public class AuditShareReferentielServiceImpl implements AuditShareReferentielService {

  public static final Logger logger = LoggerFactory.getLogger(AuditShareReferentielServiceImpl.class.getName());

  private NodeService        nodeService;
  private ContentService     contentService;

  public ContentService getContentService() {
    return contentService;
  }

  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  public NodeService getNodeService() {
    return nodeService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public List<Group> parseRefentielForNodeUUID(String id) {
    InputStream xmlStream = null;
    try {
      // Verification de l'existance du node
      NodeRef xmlRefNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
      if (nodeService.exists(xmlRefNodeRef)) {
        xmlStream = contentService.getReader(xmlRefNodeRef, ContentModel.PROP_CONTENT).getContentInputStream();
        // Initialisation du référentiel des groupes
        return parseReferentielGroups(xmlStream);
      } else {
        logger.error("NodeRef non trouvé pour l'id passé en paramètre : {}", id);
      }
    } catch (Exception e) {
      logger.error("Impossible de parser le référentiel ", e);
    } finally {
      try {
        if (xmlStream != null) {
          xmlStream.close();
        }
      } catch (Exception e) {
        logger.error("Impossible de fermer le flux de données ", e);
      }
    }

    return Collections.emptyList();
  }

  public List<Group> parseReferentielGroups(InputStream file) {
    ReferentielGroups referentiel = new ReferentielGroups();
    logger.info("Début du parsing du référentiel des groupes");

    try {
      JAXBContext jc = JAXBContext.newInstance(ReferentielGroups.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      referentiel = (ReferentielGroups) unmarshaller.unmarshal(file);

      if(logger.isInfoEnabled()) {
        logger.info("Parsing du référentiel des groupes d'utilisateurs [OK]");
        logger.info("Nombre de groupes : {}", referentiel.getGroups().size());
      }
    } catch (Exception e) {
      logger.error("Erreur lors du parsing du référentiel des groupes d'utilisateurs", e);
    }

    return referentiel.getGroups();
  }
}
