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
package com.atolcd.alfresco.web.scripts.shareStats;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.UrlUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AuditCount;
import com.atolcd.alfresco.AuditObjectPopularity;
import com.atolcd.alfresco.AuditQueryParameters;
import com.atolcd.alfresco.helper.PermissionsHelper;
import com.atolcd.auditshare.repo.service.AuditShareReferentielService;
import com.atolcd.auditshare.repo.xml.Group;
import com.csvreader.CsvWriter;

public class AuditExportGet extends AbstractWebScript implements InitializingBean {
  // Logger
  private static final Log logger = LogFactory.getLog(AuditExportGet.class);

  private static final String MESSAGE_CSV_DATE = "csv.date";
  private static final String MODEL_VALUES = "values";
  private static final String PARAM_GROUPS = "groups";

  private SelectAuditsGet  wsSelectAudits;
  private SiteService      siteService;
  private SysAdminParams   sysAdminParams;
  private AuthorityService authorityService;
  private AuditShareReferentielService auditShareReferentielService;

  public void setSysAdminParams(SysAdminParams sysAdminParams) {
    this.sysAdminParams = sysAdminParams;
  }

  public void setWsSelectAudits(SelectAuditsGet wsSelectAudits) {
    this.wsSelectAudits = wsSelectAudits;
  }

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  public AuthorityService getAuthorityService() {
    return authorityService;
  }

  public void setAuthorityService(AuthorityService authorityService) {
    this.authorityService = authorityService;
  }

  public AuditShareReferentielService getAuditShareReferentielService() {
    return auditShareReferentielService;
  }

  public void setAuditShareReferentielService(AuditShareReferentielService auditShareReferentielService) {
    this.auditShareReferentielService = auditShareReferentielService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(wsSelectAudits, "There must be a wsSelectAudits");
    Assert.notNull(siteService, "There must be a siteService");
    Assert.notNull(auditShareReferentielService, "There must be a auditShareReferentielService");
    Assert.notNull(authorityService, "There must be a authorityService");
  }

  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
    try {
      if (PermissionsHelper.isAuthorized(req)) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Charset charset = Charset.forName("UTF-8"); // ISO-8859-1
        CsvWriter csv = new CsvWriter(baos, ',', charset);

        Map<String, Object> model = new HashMap<String, Object>();
        AuditQueryParameters params = wsSelectAudits.buildParametersFromRequest(req);

        if (logger.isInfoEnabled()) {
          logger.info(params.toJSON());
        }

        String interval = req.getParameter("interval");
        String type = req.getParameter("type");
        if ("volumetry".equals(type) || "users-count".equals(type)) {
          String values = req.getParameter("values");
          model.put(MODEL_VALUES, values.split(","));
        } else {
          wsSelectAudits.checkForQuery(model, params, type);
        }
        buildCsvFromRequest(model, csv, params, type, interval, req);

        csv.close();
        res.setHeader("Content-Disposition", "attachment; filename=\"export.csv\"");
        res.setContentType("application/csv"); // application/octet-stream
        baos.writeTo(res.getOutputStream());
      } else {
        res.setStatus(Status.STATUS_UNAUTHORIZED);
      }

    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage(), e);
      }
      res.reset();
    }
  }

  /**
   *
   * @param model Model for template rendering
   * @param csv CsvWriter object used to add results into the model
   * @param params Audit query parameters
   * @param type Type of the export (name of the csv column)
   * @param interval Date interval
   * @throws SQLException
   * @throws JSONException
   * @throws IOException
   */
  @SuppressWarnings({ "unchecked" })
  public void buildCsvFromRequest(Map<String, Object> model, CsvWriter csv, AuditQueryParameters params, String type, String interval,
      WebScriptRequest req) throws SQLException, JSONException, IOException {

    // Selection of ALL audits
    String dateRecord;
    if (model.containsKey("dates")) {
      csv.writeRecord(
          new String[] { I18NUtil.getMessage(MESSAGE_CSV_DATE), I18NUtil.getMessage("csv.action"), I18NUtil.getMessage("csv.count") });
      List<List<AuditCount>> auditCountsLists = (List<List<AuditCount>>) model.get("dates");

      // XXX: for the moment, no more than 3 actions per graphic per export
      Map<String, Integer> actions = new HashMap<String, Integer>(3);
      getAllActions(actions, auditCountsLists);

      String[] slicedDates = params.getSlicedDates().split(",");
      for (int i = 0; i < auditCountsLists.size(); i++) {
        dateRecord = getStringDate(Long.parseLong(slicedDates[i]), interval);
        writeAuditCount(csv, auditCountsLists.get(i), dateRecord, actions);
      }
    } else if (model.containsKey(MODEL_VALUES)) {
      // Put selected groups in parentheses
      if ("users-count".equals(type) && (req.getParameter(PARAM_GROUPS) != null && !req.getParameter(PARAM_GROUPS).isEmpty())) {
        String[] groupsToken = req.getParameter(PARAM_GROUPS).split(",");
        List<String> libelles = new ArrayList<String>();
        List<Group> groups = auditShareReferentielService.parseRefentielForNodeUUID(AuditShareReferentielService.auditShareReferentielNodeUUID);

        for (String group : groupsToken) {
          for (Group myGroup : groups) {
            if (group.equals(myGroup.getId())) {
              if (StringUtils.isBlank(myGroup.getLibelle())) {
                if (authorityService.authorityExists(group)) {
                  libelles.add(authorityService.getAuthorityDisplayName(group));
                } else {
                  logger.info("The group " + group + " does not exist.");
                }
              } else {
                libelles.add(myGroup.getLibelle());
              }
              break;
            }
          }
        }

        String selectedGroups = StringUtils.join(libelles, ",");

        csv.writeRecord(new String[] { I18NUtil.getMessage(MESSAGE_CSV_DATE), I18NUtil.getMessage("csv." + type) + " (" + selectedGroups + ")" });

      } else {
        csv.writeRecord(new String[] { I18NUtil.getMessage(MESSAGE_CSV_DATE), I18NUtil.getMessage("csv." + type) });
      }
      String[] slicedDates = params.getSlicedDates().split(",");
      String[] values = (String[]) model.get(MODEL_VALUES);
      for (int i = 0; i < values.length; i++) {
        dateRecord = getStringDate(Long.parseLong(slicedDates[i]), interval);
        csv.writeRecord(new String[] { dateRecord, values[i] });
      }
    } else if (model.containsKey("popularity")) {
      if ("mostread".equals(type)) {
        csv.writeRecord(
            new String[] { I18NUtil.getMessage("csv.document-name"), I18NUtil.getMessage("csv.view"), I18NUtil.getMessage("csv.target") });
      } else {
        csv.writeRecord(new String[] { I18NUtil.getMessage("csv.document-name"), I18NUtil.getMessage("csv.update"),
            I18NUtil.getMessage("csv.target") });
      }
      List<AuditObjectPopularity> listAudit = (List<AuditObjectPopularity>) model.get("popularity");

      String baseUrl = UrlUtil.getShareUrl(sysAdminParams) + "/page";

      for (AuditObjectPopularity auditObject : listAudit) {
        String url = baseUrl;
        if (!StringUtils.isEmpty(auditObject.getAuditSite())) {
          url += "/site/" + auditObject.getAuditSite();
        }
        url += "/document-details?nodeRef=" + auditObject.getAuditObject();
        csv.writeRecord(new String[] { auditObject.getObjectName(), Integer.toString(auditObject.getPopularity()), url });
      }
    }
  }

  public void getAllActions(Map<String, Integer> actions, List<List<AuditCount>> auditCountLists) {
    for (List<AuditCount> auditCountList : auditCountLists) {
      for (AuditCount auditCount : auditCountList) {
        actions.put(auditCount.getTarget(), 0);
      }
    }
  }

  /**
   *
   * @param csv CsvWriter object used to write results
   * @param auditCounts Audit results
   * @param date Date
   * @param actions
   * @throws IOException
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void writeAuditCount(CsvWriter csv, List<AuditCount> auditCounts, String date, Map<String, Integer> actions) throws IOException {

    for (AuditCount auditCount : auditCounts) {
      actions.put(auditCount.getTarget(), auditCount.getCount());
    }

    Iterator i = actions.entrySet().iterator();
    while (i.hasNext()) {
      Map.Entry<String, Integer> e = (Map.Entry<String, Integer>) i.next();
      String[] record = new String[3];

      record[0] = date;
      record[1] = I18NUtil.getMessage("csv." + e.getKey());
      record[2] = e.getValue().toString();
      csv.writeRecord(record);
      e.setValue(0);
    }
  }

  /**
   *
   * @param timestamp Date timestamp
   * @param dateInterval Date interval
   *
   * @return Date String
   */
  public String getStringDate(long timestamp, String dateInterval) {
    GregorianCalendar gc = new GregorianCalendar();
    gc.setTimeInMillis(timestamp);

    String date;
    switch (intervalEnum.valueOf(dateInterval)) {
      case days:
        date = padZero(gc.get(Calendar.HOUR_OF_DAY)) + "h00";
        date += " - ";
        date += padZero((gc.get(Calendar.HOUR_OF_DAY) + 2) % 24) + "h00";
        break;
      case weeks:
      case months:
        date = padZero(gc.get(Calendar.DAY_OF_MONTH)) + "/";
        date += padZero(gc.get(Calendar.MONTH) + 1) + "/";
        date += String.valueOf(gc.get(Calendar.YEAR));
        break;
      case years:
      default:
        String monthNumber = String.valueOf(gc.get(Calendar.MONTH));
        date = I18NUtil.getMessage("csv.month." + monthNumber);
        date += " " + gc.get(Calendar.YEAR);
        break;
    }

    return date;
  }

  public String padZero(int n) {
    String ret = String.valueOf(n);
    if (n < 10) {
      ret = "0" + ret;
    }
    return ret;
  }
}
