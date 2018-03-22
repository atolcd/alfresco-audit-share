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
-- Database:   Oracle

CREATE INDEX idx_share_vol_at_time ON share_stats_site_volumetry(at_time);

-- Nécessaire pour l'insertion multiple et la génération par séquence de PK.
-- FUNCTION
CREATE OR REPLACE FUNCTION get_seq_id_site_volumetry RETURN NUMBER IS
BEGIN
  RETURN share_stats_site_volumetry_seq.nextval;
END;
-- END FUNCTION
