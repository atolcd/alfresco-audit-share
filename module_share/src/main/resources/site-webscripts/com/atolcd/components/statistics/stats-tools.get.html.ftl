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

<div id="${args.htmlid?html}-body" class="tool tools-link">
   <#if page.url.templateArgs.site?? && page.url.templateArgs.site?has_content>
      <h2>${msg("header.site.statistic.tools")}</h2>
   <#else>
      <h2>${msg("header.statistic.tools")}</h2>
   </#if>
   <ul class="toolLink">
      <#list tools as group>
         <#list group as tool>
         <#if tool_index = 0 && tool.group != ""></ul><h3>${tool.groupLabel}</h3><ul class="toolLink"></#if>
         <li class="<#if tool_index=0>first-link</#if><#if tool.selected> selected</#if>">
            <span>
              <a href="<#if page.url.templateArgs.site?? && page.url.templateArgs.site?has_content><#assign last = page.url.url?last_index_of('/') /> ${page.url.url?substring(0, last)}/</#if>${tool.id}" class="tool-link" title="${tool.description?html}">${tool.label?html}</a>
            </span>
          </li>
         </#list>
      </#list>
   </ul>
</div>