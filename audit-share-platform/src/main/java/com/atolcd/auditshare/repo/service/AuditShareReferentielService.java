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
package com.atolcd.auditshare.repo.service;

import java.io.InputStream;
import java.util.List;

import com.atolcd.auditshare.repo.xml.Group;

public interface AuditShareReferentielService {

  public static final String auditShareReferentielNodeUUID = "auditshare-user-connections-groups";

  public List<Group> parseReferentielGroups(InputStream file);

  public List<Group> parseRefentielForNodeUUID(String id);
}
