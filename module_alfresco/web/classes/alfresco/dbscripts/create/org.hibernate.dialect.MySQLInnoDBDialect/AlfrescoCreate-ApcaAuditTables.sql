--
-- Title:      Apca Audit tables
-- Database:   MySQL InnoDB
-- Since:      V3.4
-- Author:     Alexandre Nicolas
--
--


CREATE TABLE apca_audit_entry
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   audit_user_id varchar(255) NOT NULL,
   audit_site varchar(255),
   audit_app_name varchar(255) NOT NULL,
   audit_action_name varchar(255),
   audit_object varchar(255),
   audit_time BIGINT NOT NULL,
   INDEX idx_apca_alf_aud_ent_tm (audit_time),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-APCA-ExtraTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-APCA-ExtraTables', 'Manually executed script upgrade V3.4. Add Apca Audit table',
     0, 4203, -1, 4203, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );