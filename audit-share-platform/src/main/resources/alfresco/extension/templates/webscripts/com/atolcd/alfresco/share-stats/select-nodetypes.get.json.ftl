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
  [
  <#if nodeTypes?exists && nodeTypes?has_content>
    <#-- Retrieve list of nodeTypes -->
      <#list nodeTypes?sort_by("nodeTypeLabel") as nodeTypeItem>
        {
          "label": "${nodeTypeItem.nodeTypeLabel!}",
          "value": "${nodeTypeItem.nodeTypeValue!}"
        }<#if nodeTypeItem_has_next>,</#if>
      </#list>
  </#if>
  ]
</#escape>