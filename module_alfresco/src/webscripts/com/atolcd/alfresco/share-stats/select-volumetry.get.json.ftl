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
    ],
    "sites" : [
      <#list sites as site>
        "${shareStatsutils.getSiteTitle(site)}"
        <#if site_has_next>,</#if>
      </#list>
    ]
    <#if args.sites?? || (!args.site?? && !args.sites??)>
      , "stackedValues":
      [
        <#list stackedValues?sort as sv>
          [
            <#list sv.value as val>
              ${val?c}<#if val_has_next>,</#if>
            </#list>
          ]
          <#if sv_has_next>,</#if>
        </#list>
      ]
    </#if>
  </#if>
}
</#escape>