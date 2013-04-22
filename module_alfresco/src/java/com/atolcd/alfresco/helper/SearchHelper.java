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
		Assert.notNull(searchService);
	}

	/**
	 * Retourne le nodeRef du premier résultat de la requête. Null si aucun
	 * résultat.
	 * 
	 * @param query
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
