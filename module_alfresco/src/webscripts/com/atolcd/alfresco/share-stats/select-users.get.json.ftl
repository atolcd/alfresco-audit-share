<#escape x as jsonUtils.encodeJSONString(x)>
{
  <#if users??>
    "items":
    [
      <#if users?size !=0>
        <#list users?sort as user>
          {
            "username": "${user}",
            "fullName": "${shareStatsutils.getPersonFullName(user)!""}"
          }
          <#if user_has_next>,</#if>
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