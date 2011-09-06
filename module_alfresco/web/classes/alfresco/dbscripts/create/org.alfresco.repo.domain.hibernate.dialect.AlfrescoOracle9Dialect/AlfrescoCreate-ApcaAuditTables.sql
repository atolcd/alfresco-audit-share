--
-- Title:      Apca Audit tables
-- Database:   MySQL InnoDB
-- Since:      V3.4
-- Author:     Alexandre Nicolas
--
--

CREATE SEQUENCE apca_audit_entry_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE apca_audit_entry
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
CREATE INDEX idx_apca_alf_aud_ent_tm ON apca_audit_entry(audit_time);


DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-APCA-ExtraTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-APCA-ExtraTables', 'Manually executed script upgrade V3.4. Add Apca Audit table',
     0, 4203, -1, 4203, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );