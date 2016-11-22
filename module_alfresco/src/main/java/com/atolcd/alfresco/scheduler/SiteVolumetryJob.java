package com.atolcd.alfresco.scheduler;

import java.util.Date;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class SiteVolumetryJob extends AbstractScheduledLockedJob implements StatefulJob {

  private static final Log logger = LogFactory.getLog(SiteVolumetryJob.class);

  @Override
  public void executeJob(JobExecutionContext context) throws JobExecutionException {
    JobDataMap jobData = context.getJobDetail().getJobDataMap();
    Object executerObj = jobData.get("jobExecuter");
    if (executerObj == null || !(executerObj instanceof SiteVolumetryJobExecuter)) {
        throw new AlfrescoRuntimeException(
                "SiteVolumetryJob data must contain valid 'Executer' reference");
    }
    final SiteVolumetryJobExecuter jobExecuter = (SiteVolumetryJobExecuter) executerObj;
    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
      public Object doWork() throws Exception {
        logger.info("Start volumetry : "+new Date());
        jobExecuter.execute();
        logger.info("End volumetry : "+new Date());
        return null;
      }
  }, AuthenticationUtil.getSystemUserName());
  }

}
