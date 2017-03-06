package com.atolcd.auditshare.repo.service;

import java.io.InputStream;
import java.util.Map;

public interface AuditShareReferentielService {

  public static final String REF_GROUP_ID = "ref-group";

  public Map<Object, String> parseReferentielGroups(InputStream file);

  public Map<Object, String> parseRefentielForNodeUUID(String id);
}
