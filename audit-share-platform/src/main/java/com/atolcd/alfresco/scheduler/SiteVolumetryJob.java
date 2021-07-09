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
