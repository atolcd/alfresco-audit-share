package com.atolcd.alfresco.web.scripts.shareStats;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AtolVolumetryEntry;
import com.atolcd.alfresco.AuditEntry;

public class InsertAuditPost extends DeclarativeWebScript implements InitializingBean {
    private static final String INSERT_ENTRY = "alfresco.atolcd.audit.insertEntry";
    private static final String INSERT_VOLUMETRY = "alfresco.atolcd.audit.insertVolumetry";
    // SqlMapClientTemplate for ibatis calls
    private SqlSessionTemplate sqlSessionTemplate;
    private SiteService siteService;

    private static final Log logger = LogFactory.getLog(InsertAuditPost.class);

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.sqlSessionTemplate);
        Assert.notNull(this.siteService);
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        // Map that will be passed to the template
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("success", false);
        try {
            // Check for the sqlMapClientTemplate Bean
            if (this.sqlSessionTemplate != null) {
                // Get the input content given into the request.
                String jsonArg = req.getContent().getContent();

                if (!jsonArg.isEmpty()) {
                    // Fill an auditSample from the request content and insert
                    // it
                    AuditEntry auditSample = new AuditEntry(jsonArg);
                    getSiteFromObject(auditSample);
                    insert(auditSample);
                    model.put("success", true);
                }
            }
        } catch (InvalidNodeRefException invalidNodeRefException) {
            // Le noeud n'existe pas/plus, on ne déclenche rien. Assainissement du log ...
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebScriptException("[ShareStats-DbInsert] Error in executeImpl function");
        }
        return model;
    }

    public void insert(AuditEntry auditSample) throws SQLException, JSONException {
        if (!auditSample.getAuditSite().isEmpty()) {
        	sqlSessionTemplate.insert(INSERT_ENTRY, auditSample);
            logger.info("Insert ok : " + auditSample.toJSON());
        }
    }

    public void getSiteFromObject(AuditEntry auditSample) {
        if (auditSample.getAuditSite().equals("/service")) {
            NodeRef nodeRef = new NodeRef(auditSample.getAuditObject());
            SiteInfo siteInfo = siteService.getSite(nodeRef);
            if (siteInfo != null) {
                auditSample.setAuditSite(siteInfo.getShortName());
            } else {
                auditSample.setAuditSite("");
            }
        }

    }
    
    public void insertVolumetry(AtolVolumetryEntry atolVolumetryEntry){
    	sqlSessionTemplate.insert(INSERT_VOLUMETRY, atolVolumetryEntry);
        logger.info("Insert volumetrie ok ");
    }
}
