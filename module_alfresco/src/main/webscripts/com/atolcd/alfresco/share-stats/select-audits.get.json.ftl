<#--
 * Copyright (C) 2018 Atol Conseils et Développements.
 * http://www.atolcd.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
-->

<#macro auditCount auditItems>
  <#escape x as jsonUtils.encodeJSONString(x)>
    "totalResults": <#if args.combined??>1<#else>${auditItems?size?c}</#if>,
    "items" :
    [
      <#if args.combined??>
        {
          <#assign count = 0 />
          <#list auditItems as auditItem><#assign count = (count + auditItem.count) /></#list>
          "count": ${count},
          "target": "${args.type!""}"
        }
      <#else>
        <#list auditItems as auditItem>
          {
            "count": ${auditItem.count?c},
            "target": "${auditItem.target}"
          }<#if auditItem_has_next>,</#if>
        </#list>
      </#if>
    ]
  </#escape>
</#macro>

<#escape x as jsonUtils.encodeJSONString(x)>
  {
  <#if popularity?exists>
    <#-- By popularity -->
    <#if popularity?size !=0>
      "items" :
      [
        <#list popularity as auditItem>
          {
            "popularity": ${auditItem.popularity?c},
            "nodeRef": "${auditItem.auditObject}",
            "site": "${auditItem.auditSite}",
            "siteTitle": "${shareStatsutils.getSiteTitle(auditItem.auditSite)}",
            "siteComponent": "${auditItem.siteComponent!""}",
            "name": "${auditItem.objectName}",
            "displayName": "${auditItem.objectDisplayName}"
          }<#if auditItem_has_next>,</#if>
        </#list>
      ]
    </#if>
  <#elseif dates?exists>
    <#-- By dates -->
    <#if dates?size !=0>
      "totalResults": ${dates?size?c},
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