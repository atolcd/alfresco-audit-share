<#escape x as jsonUtils.encodeJSONString(x)>
{
   "items":
   [
   <#list sites as site>
      {
         "name": "${site.shortName}"
      }<#if site_has_next>,</#if>
   </#list>
   ]
}
</#escape>