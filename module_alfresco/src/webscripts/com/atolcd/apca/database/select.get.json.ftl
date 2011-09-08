<#escape x as jsonUtils.encodeJSONString(x)>
<#if results?exists>
	<#if results?size !=0>
{
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
}
  </#if>
<#elseif modules?exists>
{
  "totalResults": ${modules?size?c},
  "items":
  [
    <#list modules as module>
      {
        "count": ${module.count},
        "target": "${module.target}"
      }<#if module_has_next>,</#if>
   </#list>
  ]
}
<#elseif actions?exists>
{
  "totalResults": ${actions?size?c},
  "items":
  [
    <#list actions as action>
      {
        "count": ${count.id},
        "target": "${action.target}"
      }<#if action_has_next>,</#if>
   </#list>
  ]
}
</#if>
</#escape>