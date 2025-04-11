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
package com.atolcd.alfresco.helper;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class SearchHelper implements InitializingBean {

  private static SearchService searchService;

  public void setSearchService(SearchService searchService) {
    SearchHelper.searchService = searchService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(searchService, "There must be a searchService");
  }

  /**
   * Execute Lucene query and return the first result (NodeRef). Return null if there is no result
   *
   * @param query String Lucene query
   * @return NodeRef
   */
  static public NodeRef getFirstFromQuery(String query) {
    NodeRef nodeRef = null;
    SearchParameters sp = new SearchParameters();
    sp.addStore(new StoreRef("workspace://SpacesStore"));
    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
    sp.setQuery(query);
    ResultSet results = null;

    try {
      results = searchService.query(sp);
      if (results.length() > 0) {
        ResultSetRow row = results.getRow(0);
        nodeRef = row.getNodeRef();
      }
    } finally {
      if (results != null) {
        results.close();
      }
    }
    return nodeRef;
  }
}
