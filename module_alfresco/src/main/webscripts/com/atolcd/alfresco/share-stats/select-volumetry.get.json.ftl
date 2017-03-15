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
        <#assign maxCount = 0 />
        <#list values as value>
          <#if value?? && value?c?number gt maxCount>
            <#assign maxCount = value?c?number />
          </#if>

          ${(value?c)!'null'}<#if value_has_next>,</#if>
        </#list>
      </#if>
    ],
    "maxCount": ${maxCount?c},
    "sites" : [
      <#list sites as site>
        "${shareStatsutils.getSiteTitle(site)}"
        <#if site_has_next>,</#if>
      </#list>
    ]
    <#if args.stacked?? || (!args.site?? && !args.sites??)>
      , "stackedValues":
      [
        <#assign maxLocal = 0 />
        <#list stackedValues?sort as sv>
          [
            <#list sv.value as val>
              <#if val?? && val?c?number gt maxLocal>
                <#assign maxLocal = val?c?number />
              </#if>

              ${(val?c)!'null'}<#if val_has_next>,</#if>
            </#list>
          ]
          <#if sv_has_next>,</#if>
        </#list>
      ]
      , "maxLocal": ${maxLocal?c}
    </#if>
  </#if>
}
</#escape>