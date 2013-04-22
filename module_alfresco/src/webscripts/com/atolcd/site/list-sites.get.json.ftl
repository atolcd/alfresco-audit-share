[
  <#assign first = true />

  <#list sites as site>
    <#if args.role?? && !isAdmin>
      <#assign role = site.getMembersRole(person.properties.userName)!"" />
      <#if role == args.role>
        <#if first == false>,<#else><#assign first = false /></#if>
        <@siteJSON site=site />
      </#if>
    <#else>
      <#if first == false>,<#else><#assign first = false /></#if>
      <@siteJSON site=site />
    </#if>
  </#list>
]

<#macro siteJSON site>
  <#escape x as jsonUtils.encodeJSONString(x)>
    {
      "name": "${site.shortName}",
      "title": "${site.title}"
    }
  </#escape>
</#macro>