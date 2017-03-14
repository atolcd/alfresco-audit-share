package com.atolcd.auditshare.repo.service;

import java.io.InputStream;
import java.util.List;

import com.atolcd.auditshare.repo.xml.Group;

public interface AuditShareReferentielService {

  public static String auditShareReferentielNodeUUID = "auditshare-user-connections-groups";

  public List<Group> parseReferentielGroups(InputStream file);

  public List<Group> parseRefentielForNodeUUID(String id);
}