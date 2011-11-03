--
-- Title:      ShareStats Audit tables
-- Database:   MySQL InnoDB
-- Since:      V3.4
-- Author:     Alexandre Nicolas
--
--


CREATE TABLE share_stats_audit_entry
(
   id NUMERIC(19,0) IDENTITY NOT NULL,
   audit_user_id NVARCHAR(255) NOT NULL,
   audit_site NVARCHAR(255),
   audit_app_name NVARCHAR(255) NOT NULL,
   audit_action_name NVARCHAR(255),
   audit_object NVARCHAR(255),
   audit_time NUMERIC(19,0) NOT NULL,
   PRIMARY KEY (id)
);
CREATE INDEX idx_share_stats_alf_aud_ent_tm ON share_stats_audit_entry (audit_time);

DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-ShareStats-ExtraTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-ShareStats-ExtraTables', 'Manually executed script upgrade V3.4. Add ShareStats Audit table',
     0, 4203, -1, 4203, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );