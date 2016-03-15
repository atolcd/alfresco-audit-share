package com.atolcd.alfresco;

public class AuditObjectPopularity
{
  private int popularity;
  private String auditObject;
  private String objectName;
  private String objectDisplayName;
  private String auditSite;
  private String siteComponent;
  private String auditUser;
  private float average;
  
  public float getAverage()
  {
    return this.average;
  }
  
  public void setAverage(float average)
  {
    this.average = average;
  }
  
  public String getAuditUser()
  {
    return this.auditUser;
  }
  
  public void setAuditUser(String auditUser)
  {
    this.auditUser = auditUser;
  }
  
  public int getPopularity()
  {
    return this.popularity;
  }
  
  public void setPopularity(int popularity)
  {
    this.popularity = popularity;
  }
  
  public String getAuditObject()
  {
    return this.auditObject;
  }
  
  public void setAuditObject(String auditObject)
  {
    this.auditObject = auditObject;
  }
  
  public String getObjectName()
  {
    return this.objectName;
  }
  
  public void setObjectName(String objectName)
  {
    this.objectName = objectName;
  }
  
  public String getObjectDisplayName()
  {
    return this.objectDisplayName;
  }
  
  public void setObjectDisplayName(String objectDisplayName)
  {
    this.objectDisplayName = objectDisplayName;
  }
  
  public String getAuditSite()
  {
    return this.auditSite;
  }
  
  public void setAuditSite(String auditSite)
  {
    this.auditSite = auditSite;
  }
  
  public String getSiteComponent()
  {
    return this.siteComponent;
  }
  
  public void setSiteComponent(String siteComponent)
  {
    this.siteComponent = siteComponent;
  }
}
