package com.atolcd.alfresco.web.scripts.shareStats;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AuditCount;
import com.atolcd.alfresco.AuditEntry;
import com.atolcd.alfresco.AuditQueryParameters;
import com.csvreader.CsvWriter;

public class AuditExportGet extends AbstractWebScript implements InitializingBean {

	private SelectAuditsGet wsSelectAudits;

	public void setWsSelectAudits(SelectAuditsGet wsSelectAudits) {
		this.wsSelectAudits = wsSelectAudits;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(wsSelectAudits);
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
			String type = req.getParameter("type").equals("action") ? "all" : req.getParameter("type");

			wsSelectAudits.checkForQuery(model, params, type);
			buildCsvFromRequest(model, csv, params);

			csv.close();
			res.setHeader("Content-Disposition", "attachment; filename=\"export.csv\"");
			res.setContentType("application/csv");// application/octet-stream
			baos.writeTo(res.getOutputStream());

		} catch (Exception e) {
			e.printStackTrace();
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
	public void buildCsvFromRequest(Map<String, Object> model, CsvWriter csv, AuditQueryParameters params) throws SQLException, JSONException, IOException {
		// Sélection de TOUS les audits.
		if (model.containsKey("results")) {
			String[] record = new String[4];
			csv.writeRecord(new String[] {I18NUtil.getMessage("csv.site"), 
					I18NUtil.getMessage("csv.module"),
					I18NUtil.getMessage("csv.action"),
					I18NUtil.getMessage("csv.date")});
			List<AuditEntry> auditSamples = (List<AuditEntry>) model.get("results");
			for (AuditEntry auditSample : auditSamples) {
				record[0] = auditSample.getAuditSite();
				record[1] = auditSample.getAuditAppName();
				record[2] = auditSample.getAuditActionName();
				record[3] = getStringDate(auditSample.getAuditTime());
				csv.writeRecord(record);
			}
		} else if (model.containsKey("views")) {
			writeSites(params,csv);
			if (params.getDateFrom() > 0 || params.getDateTo() > 0) {
				String[] dateRecords = new String[2];
				dateRecords[0] = getStringDate(params.getDateFrom());
				dateRecords[1] = getStringDate(params.getDateTo());
				csv.writeRecord(dateRecords);
			}
			List<AuditCount> auditCounts = (List<AuditCount>) model.get("views");
			csv.writeRecord(new String[] { I18NUtil.getMessage("csv.target"), I18NUtil.getMessage("csv.count") });
			writeAuditCount(csv, auditCounts);
		} else if (model.containsKey("dates")) {
			writeSites(params,csv);
			List<List<AuditCount>> auditCountsLists = (List<List<AuditCount>>) model.get("dates");
			String[] slicedDates = ((String) model.get("slicedDates")).split(",");
			for (int i = 0; i < auditCountsLists.size(); i++) {
				String[] dateRecords = new String[2];
				dateRecords[0] = getStringDate(Long.parseLong(slicedDates[i]));
				dateRecords[1] = getStringDate(Long.parseLong(slicedDates[i + 1]));
				csv.writeRecord(dateRecords);
				writeAuditCount(csv, auditCountsLists.get(i));
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
	public void writeAuditCount(CsvWriter csv, List<AuditCount> auditCounts) throws IOException {
		for (AuditCount auditCount : auditCounts) {
			String[] record = new String[2];
			record[0] = auditCount.getTarget();
			record[1] = String.valueOf(auditCount.getCount());
			csv.writeRecord(record);
		}
	}

	public void writeSites(AuditQueryParameters params,CsvWriter csv) throws IOException{
		if(params.getSitesId()!=null){
			String sites = "";
			for(String site:params.getSitesId()){
				sites += site + " / ";
			}
			csv.writeRecord(new String[]{I18NUtil.getMessage("csv.sites"),sites});
		}
	}
	
	/**
	 * 
	 * @param gc
	 *            GrégorianCalendar dont on souhaite récupérer la date
	 * 
	 * @return date String
	 */
	public String getStringDate(long l) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(l);

		String date = "";
		date += String.valueOf(gc.get(Calendar.DAY_OF_MONTH)) + "/";
		date += String.valueOf(gc.get(Calendar.MONTH) + 1) + "/";
		date += String.valueOf(gc.get(Calendar.YEAR));

		return date;
	}
}
