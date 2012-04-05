package com.atolcd.alfresco.web.scripts.shareStats;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AuditCount;
import com.atolcd.alfresco.AuditQueryParameters;
import com.atolcd.alfresco.CsvExportEntry;
import com.csvreader.CsvWriter;

public class AuditExportGet extends AbstractWebScript implements InitializingBean {

	private SelectAuditsGet wsSelectAudits;
	private SiteService siteService;

	public void setWsSelectAudits(SelectAuditsGet wsSelectAudits) {
		this.wsSelectAudits = wsSelectAudits;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(wsSelectAudits);
		Assert.notNull(siteService);
	}

	/**
	 * 
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Charset charset = Charset.forName("UTF-8");// ISO-8859-1
			CsvWriter csv = new CsvWriter(baos, ',', charset);

			Map<String, Object> model = new HashMap<String, Object>();
			AuditQueryParameters params = wsSelectAudits.buildParametersFromRequest(req);

			String interval = req.getParameter("interval");
			String type = req.getParameter("type");
			if (type.equals("volumetry") || type.equals("users-count")) {
				String values = req.getParameter("values");
				model.put("values", values.split(","));
			} else {
				wsSelectAudits.checkForQuery(model, params, type);
			}
			buildCsvFromRequest(model, csv, params, type, interval);

			csv.close();
			res.setHeader("Content-Disposition", "attachment; filename=\"export.csv\"");
			res.setContentType("application/csv");// application/octet-stream
			baos.writeTo(res.getOutputStream());

		} catch (Exception e) {
			e.printStackTrace();
			res.reset();
		}
	}

	/**
	 * 
	 * @param model
	 *            ModÃ¨le dans lequel on Ã©crit
	 * @param csv
	 *            CsvWriter utilisÃ© pour Ã©crire dans le modÃ¨le
	 * @throws SQLException
	 * @throws JSONException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void buildCsvFromRequest(Map<String, Object> model, CsvWriter csv, AuditQueryParameters params, String type, String interval)
			throws SQLException, JSONException, IOException {
		// Sélection de TOUS les audits.
		String dateRecord = null;
		if (model.containsKey("dates")) {
			csv.writeRecord(new String[] { I18NUtil.getMessage("csv.date"), I18NUtil.getMessage("csv.action"),
					I18NUtil.getMessage("csv.count") });
			List<List<AuditCount>> auditCountsLists = (List<List<AuditCount>>) model.get("dates");
			// Actuellement, on ne dépasse pas 3 actions / graphique / export
			Map<String, Integer> actions = new HashMap<String, Integer>(3);
			getAllActions(actions, auditCountsLists);
			String[] slicedDates = params.getSlicedDates().split(",");
			for (int i = 0; i < auditCountsLists.size(); i++) {
				dateRecord = getStringDate(Long.parseLong(slicedDates[i]), interval);
				writeAuditCount(csv, auditCountsLists.get(i), dateRecord, actions);
			}
		} else if (model.containsKey("values")) {
			csv.writeRecord(new String[] { I18NUtil.getMessage("csv.date"), I18NUtil.getMessage("csv." + type) });
			String[] slicedDates = params.getSlicedDates().split(",");
			String[] values = (String[]) model.get("values");
			for (int i = 0; i < values.length; i++) {
				dateRecord = getStringDate(Long.parseLong(slicedDates[i]), interval);
				csv.writeRecord(new String[] { dateRecord, values[i] });
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
	 * @param csv
	 *            CsvWriter dans lequel on écrit
	 * @param auditCounts
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
			record[1] = I18NUtil.getMessage("csv." + (String) e.getKey());
			record[2] = ((Integer) e.getValue()).toString();
			csv.writeRecord(record);
			e.setValue(new Integer(0));
		}

	}

	public void writeCsvEntry(CsvWriter csv, List<CsvExportEntry> csvExportEntries, boolean dateFirst, String date) throws IOException {
		for (CsvExportEntry csvExportEntry : csvExportEntries) {
			String[] record;
			int recordIndex = 0;
			if (date != null) {
				record = new String[5];
				if (dateFirst) {
					record[recordIndex++] = date;
				}
			} else {
				record = new String[4];
			}
			record[recordIndex++] = csvExportEntry.getAuditSite();
			record[recordIndex++] = I18NUtil.getMessage("csv." + csvExportEntry.getAuditAppName());
			record[recordIndex++] = I18NUtil.getMessage("csv." + csvExportEntry.getAuditAppName() + "."
					+ csvExportEntry.getAuditActionName());
			record[recordIndex++] = String.valueOf(csvExportEntry.getCount());

			if (!dateFirst && date != null) {
				record[recordIndex++] = date;
			}
			csv.writeRecord(record);
		}
	}

	/**
	 * 
	 * @param gc
	 *            GrégorianCalendar dont on souhaite récupérer la date
	 * 
	 * @return date String
	 */
	public String getStringDate(long timestamp, String dateInterval) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp);

		String date = "";
		switch (intervalEnum.valueOf(dateInterval)) {
		case days:
			date = padZero(gc.get(Calendar.HOUR_OF_DAY)) + "h00";
			date += " - ";
			date += padZero((gc.get(Calendar.HOUR_OF_DAY) + 2) % 24) + "h00";
			break;
		case weeks:
			date = padZero(gc.get(Calendar.DAY_OF_MONTH)) + "/";
			date += padZero(gc.get(Calendar.MONTH) + 1) + "/";
			date += String.valueOf(gc.get(Calendar.YEAR));
			break;
		case months:
			date = padZero(gc.get(Calendar.DAY_OF_MONTH)) + "/";
			date += padZero(gc.get(Calendar.MONTH) + 1) + "/";
			date += String.valueOf(gc.get(Calendar.YEAR));
			break;
		case years:
			String monthNumber = String.valueOf(gc.get(Calendar.MONTH));
			date = I18NUtil.getMessage("csv.month." + monthNumber);
			date += " " + String.valueOf(gc.get(Calendar.YEAR));
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
