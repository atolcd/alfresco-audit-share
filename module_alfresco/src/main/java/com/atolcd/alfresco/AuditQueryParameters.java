package com.atolcd.alfresco;

import java.util.ArrayList;
import java.util.List;

public class AuditQueryParameters
{
  private String siteId;
  private List<String> sitesId;
  private String appName;
  private List<String> appNames;
  private String actionName;
  private String object;
  private long dateFrom;
  private long dateTo;
  private String slicedDates;
  private String userId;
  private List<String> usersId;
  
  public List<String> getUsersId()
  {
    return this.usersId;
  }
  
  public void setUsersId(List<String> _usersId)
  {
    if ((_usersId == null) || ((_usersId != null) && (_usersId.isEmpty()))) {
      this.usersId = null;
    } else {
      this.usersId = _usersId;
    }
  }
  
  public void setUsersId(String _usersId)
  {
    if (_usersId != null)
    {
      String[] usersToken = _usersId.split(",");
      this.usersId = new ArrayList(usersToken.length);
      for (String token : usersToken) {
        this.usersId.add(token);
      }
    }
  }
  
  public String getSlicedDates()
  {
    return this.slicedDates;
  }
  
  public void setSlicedDates(String slicedDates)
  {
    this.slicedDates = slicedDates;
  }
  
  public AuditQueryParameters()
  {
    this.siteId = null;
    this.sitesId = null;
    this.appName = null;
    this.appNames = null;
    this.actionName = null;
    this.object = null;
    this.dateFrom = 0L;
    this.dateTo = 0L;
    this.slicedDates = null;
    this.userId = null;
  }
  
  public AuditQueryParameters(String site, List<String> sites, String app, List<String> apps, String action, String obj, long from, long to, String dates, String user)
  {
    this.siteId = site;
    this.sitesId = sites;
    this.appName = app;
    this.appNames = apps;
    this.actionName = action;
    this.object = obj;
    this.dateFrom = from;
    this.dateTo = to;
    this.slicedDates = dates;
    this.userId = user;
  }
  
  public List<String> getSitesId()
  {
    return this.sitesId;
  }
  
  public void setSitesId(List<String> _sitesId)
  {
    if ((_sitesId == null) || ((_sitesId != null) && (_sitesId.isEmpty()))) {
      this.sitesId = null;
    } else {
      this.sitesId = _sitesId;
    }
  }
  
  public void setSitesId(String _sitesId)
  {
    if (_sitesId != null)
    {
      String[] sitesToken = _sitesId.split(",");
      this.sitesId = new ArrayList(sitesToken.length);
      for (String token : sitesToken) {
        this.sitesId.add(token);
      }
    }
  }
  
  public String getSiteId()
  {
    return this.siteId;
  }
  
  public void setSiteId(String siteId)
  {
    this.siteId = siteId;
  }
  
  public String getAppName()
  {
    return this.appName;
  }
  
  public void setAppName(String _appName)
  {
    this.appName = _appName;
  }
  
  public List<String> getAppNames()
  {
    return this.appNames;
  }
  
  public void setAppNames(String _appNames)
  {
    if (_appNames != null)
    {
      String[] appNamesToken = _appNames.split(",");
      this.appNames = new ArrayList(appNamesToken.length);
      for (String token : appNamesToken) {
        this.appNames.add(token);
      }
    }
  }
  
  public String getActionName()
  {
    return this.actionName;
  }
  
  public void setActionName(String actionName)
  {
    this.actionName = actionName;
  }
  
  public String getUserId()
  {
    return this.userId;
  }
  
  public void setUserId(String userId)
  {
    this.userId = userId;
  }
  
  public long getDateFrom()
  {
    return this.dateFrom;
  }
  
  public void setDateFrom(long dateFrom)
  {
    this.dateFrom = dateFrom;
  }
  
  public void setDateFrom(String dateFrom)
  {
    if ((dateFrom == null) || (dateFrom.equals(""))) {
      this.dateFrom = 0L;
    } else {
      this.dateFrom = Long.parseLong(dateFrom);
    }
  }
  
  public long getDateTo()
  {
    return this.dateTo;
  }
  
  public void setDateTo(long dateTo)
  {
    this.dateTo = dateTo;
  }
  
  public void setDateTo(String dateTo)
  {
    if ((dateTo == null) || (dateTo.equals(""))) {
      this.dateTo = 0L;
    } else {
      this.dateTo = Long.parseLong(dateTo);
    }
  }
  
  public String getObject()
  {
    return this.object;
  }
  
  public void setObject(String object)
  {
    this.object = object;
  }
}
