package com.atolcd.alfresco.repo.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.atolcd.alfresco.web.scripts.shareStats.SelectAuditsGet;

public class ShareStats extends BaseScopableProcessorExtension implements InitializingBean {

    private SelectAuditsGet wsSelectAudits;

    public void setWsSelectAudits(SelectAuditsGet wsSelectAudits) {
        this.wsSelectAudits = wsSelectAudits;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        Assert.notNull(wsSelectAudits);
    }

    public int getDocumentPopularity(String nodeRef) {
        return wsSelectAudits.getDocumentPopularity(nodeRef);
    }
}
