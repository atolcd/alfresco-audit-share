package com.atolcd.auditshare.repo.service;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.log4j.Logger;

import com.atolcd.auditshare.repo.xml.Group;
import com.atolcd.auditshare.repo.xml.ReferentielGroups;

public class AuditShareReferentielServiceImpl implements AuditShareReferentielService {

  public static final Logger logger = Logger.getLogger(AuditShareReferentielServiceImpl.class.getName());

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

  public Map<Object, String> parseRefentielForNodeUUID(String id) {
    InputStream xmlStream = null;
    try {
      // Verification de l'existance du node
      NodeRef xmlRefNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
      if (xmlRefNodeRef != null && nodeService.exists(xmlRefNodeRef)) {
        xmlStream = contentService.getReader(xmlRefNodeRef, ContentModel.PROP_CONTENT).getContentInputStream();
        if (id.equals(REF_GROUP_ID)) {
          // Initialisation du référentiel des groupes
          return parseReferentielGroups(xmlStream);
        } else {
          logger.error("Référentiel (UUDI) non reconnu " + id);
        }
      } else {
        logger.error("NodeRef non trouvé pour l'id passé en paramètre : " + id);
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

    return Collections.emptyMap();
  }

  public Map<Object, String> parseReferentielGroups(InputStream file) {
    Map<Object, String> mapRefGroups = new HashMap<Object, String>();
    logger.info("Début du parsing du référentiel des groupes");
    try {
      JAXBContext jc = JAXBContext.newInstance("com.atolcd.auditshare");
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      ReferentielGroups referentiel = (ReferentielGroups) unmarshaller.unmarshal(file);
      // Vide les referentiels existants
      mapRefGroups.clear();
      for (Group group : referentiel.getGroups()) {
        mapRefGroups.put(group.getId(), group.getLibelle());
      }
      logger.info("Parsing du référentiel des groupes d'utilisateurs [OK]");
      logger.info("Nombre de groupes : " + mapRefGroups.size());
    } catch (Exception e) {
      logger.error("Erreur lors du parsing du référentiel des groupes d'utilisateurs", e);
    }

    return mapRefGroups;
  }
}
