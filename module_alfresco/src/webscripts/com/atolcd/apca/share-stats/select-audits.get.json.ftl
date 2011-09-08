<#escape x as jsonUtils.encodeJSONString(x)>
<#-- Macro ensemble de stats -->
<#macro auditCount auditItems>
  "totalResults": ${auditItems?size?c},
  "items" :
  [
    <#list auditItems as auditItem>
      {
        "count": ${auditItem.count},
        "target": "${auditItem.target}"
      }<#if auditItem_has_next>,</#if>
    </#list>
  ]
</#macro>
{
  <#if results?exists>
    <#if results?size !=0>
  <#-- Template "all" -->

    "totalResults": ${results?size?c},
    "items":
    [
      <#list results as result>
        {
          "id": ${result.id},
          "auditUserId": "${result.auditUserId}",
          "auditAppName": "${result.auditAppName}",
          "auditActionName": "${result.auditActionName}",
          "auditSite": "${result.auditSite}",
          "auditObject": "${result.auditObject}",
          "auditTime": "${result.auditTime}"
        }<#if result_has_next>,</#if>
     </#list>
    ]

    </#if>
  <#-- Template statistiques simple -->
  <#elseif views?exists>
    <#if views?size !=0>
  
    "type": "${type}",
    <@auditCount auditItems=views />
  
    </#if>
  <#-- Template statistiques par dates -->
  <#elseif dates?exists>
    <#if dates?size !=0>
  
    "totalResults": ${dates?size?c},
    "type": "${type}",
    "slicedDates" : "${slicedDates}",
    "items":
    [
      <#list dates as date>
      {
        <@auditCount auditItems=date />
      }<#if date_has_next>,</#if>
      </#list>
    ]
  
    </#if>
  </#if>
}
</#escape>