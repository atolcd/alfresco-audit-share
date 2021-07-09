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