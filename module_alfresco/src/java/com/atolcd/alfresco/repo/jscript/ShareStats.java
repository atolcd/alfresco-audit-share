package com.atolcd.alfresco.repo.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AtolVolumetryEntry;
import com.atolcd.alfresco.web.scripts.shareStats.InsertAuditPost;
import com.atolcd.alfresco.web.scripts.shareStats.SelectAuditsGet;

public class ShareStats extends BaseScopableProcessorExtension implements InitializingBean {

    private InsertAuditPost wsInsertAudits;

    public void setWsInsertAudits(InsertAuditPost wsInsertAudits) {
        this.wsInsertAudits = wsInsertAudits;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        Assert.notNull(wsInsertAudits);
    }
    
    public boolean insertVolumetry(String siteId,long siteSize, int folderCount,int fileCount,long atTime) {
    	boolean success = true;
    	try{
    		AtolVolumetryEntry atolVolumetryEntry = new AtolVolumetryEntry(siteId,siteSize,folderCount,fileCount,atTime);
    		this.wsInsertAudits.insertVolumetry(atolVolumetryEntry);
    	}catch(Exception e){
    		e.printStackTrace();
    		success = false;
    	}
    	return success;
    }
}
