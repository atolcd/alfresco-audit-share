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