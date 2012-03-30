<#escape x as jsonUtils.encodeJSONString(x)>
{
  <#if users??>
    "items":
    [
      <#if users?size !=0>
        <#list users as user>
          "${user}"<#if user_has_next>,</#if>
        </#list>
      </#if>
    ]
  <#elseif values?exists>
    "values":
    [
      <#if values?size !=0>
        <#list values as value>
        ${value?c}
        <#if value_has_next>,</#if>
        </#list>
      </#if>
    ]
  </#if>
}
</#escape>