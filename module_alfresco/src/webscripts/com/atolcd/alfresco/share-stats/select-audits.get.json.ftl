<#escape x as jsonUtils.encodeJSONString(x)>
<#-- Macro ensemble de stats -->
<#macro auditCount auditItems>
  "totalResults": ${auditItems?size?c},
  "items" :
  [
    <#list auditItems as auditItem>
      {
        "count": ${auditItem.count?c},
        "target": "${auditItem.target}"
      }<#if auditItem_has_next>,</#if>
    </#list>
  ]
</#macro>


<#-- Template statistiques de popularity -->
<#if popularity?exists>
  <#if popularity?size !=0>
  {
    "items" :
    [
      <#list popularity as auditItem>
        {
          "popularity": ${auditItem.popularity?c},
          "nodeRef": "${auditItem.auditObject}",
          "name": "${auditItem.objectName}"
        }<#if auditItem_has_next>,</#if>
      </#list>
    ]
  }
  </#if>
<#-- Template statistiques par dates -->
<#elseif dates?exists>
  <#if dates?size !=0>
  {
    "totalResults": ${dates?size?c},
    "items":
    [
      <#list dates as date>
      {
        <@auditCount auditItems=date />
      }<#if date_has_next>,</#if>
      </#list>
    ]
  }
  </#if>
</#if>
</#escape>