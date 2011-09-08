<#escape x as jsonUtils.encodeJSONString(x)>
   [
   <#list sites as site>
      {
         "name": "${site.shortName}",
         "title": "${site.title}"
      }<#if site_has_next>,</#if>
   </#list>
   ]
</#escape>