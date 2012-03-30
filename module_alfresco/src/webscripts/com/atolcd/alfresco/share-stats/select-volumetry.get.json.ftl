<#escape x as jsonUtils.encodeJSONString(x)>
{
  <#if values?exists>
    "values":
    [
      <#if values?size !=0>
        <#list values as value>
        ${value?c}<#if value_has_next>,</#if>
        </#list>
      </#if>
    ]
  </#if>
}
</#escape>