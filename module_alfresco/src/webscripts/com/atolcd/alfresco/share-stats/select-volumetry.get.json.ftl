<#--
 * Copyright (C) 2013 Atol Conseils et Développements.
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