package com.atolcd.alfresco.helper;

import com.atolcd.alfresco.AuditObjectPopularity;
import java.util.Comparator;

public class AverageComparator
  implements Comparator<AuditObjectPopularity>
{
  public int compare(AuditObjectPopularity o1, AuditObjectPopularity o2)
  {
    return Float.compare(o2.getAverage(), o1.getAverage());
  }
}
