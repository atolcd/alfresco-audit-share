/*--
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
package com.atolcd.alfresco;

public class CsvExportEntry {
  long   id              = 0;
  String auditSite       = "";
  String auditAppName    = "";
  String auditActionName = "";
  int    count           = 0;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getAuditSite() {
    return auditSite;
  }

  public void setAuditSite(String auditSite) {
    this.auditSite = auditSite;
  }

  public String getAuditAppName() {
    return auditAppName;
  }

  public void setAuditAppName(String auditAppName) {
    this.auditAppName = auditAppName;
  }

  public String getAuditActionName() {
    return auditActionName;
  }

  public void setAuditActionName(String auditActionName) {
    this.auditActionName = auditActionName;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
