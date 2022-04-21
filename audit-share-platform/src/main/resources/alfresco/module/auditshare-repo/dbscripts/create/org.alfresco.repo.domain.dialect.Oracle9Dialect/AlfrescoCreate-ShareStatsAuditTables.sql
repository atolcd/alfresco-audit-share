-- Copyright (C) 2018 Atol Conseils et Développements.
-- http://www.atolcd.com/
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Lesser General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Lesser General Public License for more details.
--
-- You should have received a copy of the GNU Lesser General Public License
-- along with this program. If not, see <http://www.gnu.org/licenses/>.

-- Title:      ShareStats Audit tables
-- Database:   MySQL InnoDB

CREATE SEQUENCE share_stats_audit_entry_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE share_stats_audit_entry
(
  id NUMBER(19,0) NOT NULL,
  audit_user_id VARCHAR2(255) NOT NULL,
  audit_site VARCHAR2(255),
  audit_app_name VARCHAR2(255) NOT NULL,
  audit_action_name VARCHAR2(255),
  audit_object VARCHAR2(255),
  audit_time NUMBER(19,0) NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX idx_share_stats_alf_aud_ent_tm ON share_stats_audit_entry(audit_time);

CREATE SEQUENCE share_stats_site_volumetry_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE share_stats_site_volumetry
(
  id NUMBER(19,0) NOT NULL,
  site_id VARCHAR2(255) NOT NULL,
  site_size NUMBER(19,0),
  folder_count NUMBER(9,0),
  file_count NUMBER(9,0),
  at_time NUMBER(19,0) NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX idx_share_stats_site_vol_site ON share_stats_site_volumetry(site_id);

DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-ShareStats-ExtraTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-ShareStats-ExtraTables', 'Manually executed script upgrade V3.4. Add Share Stats Audit table',
     0, 4203, -1, 4203, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
