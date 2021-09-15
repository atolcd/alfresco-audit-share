<#import "/com/atolcd/components/statistics/statistics-tools.lib.ftl" as stats>

<#include "/org/alfresco/components/component.head.inc">
<#include "/com/atolcd/components/statistics/chart-ressources.lib.ftl">

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

<#assign el = args.htmlid?html />

<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/atolcd/statistics/global-usage.css" group="auditShare"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/global-usage.js" group="auditShare"/>
   <@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/repo-usage.js" group="auditShare"/>
</@>

<@markup id="widgets">
   <@createWidgets group="auditShare"/>
</@>

<@uniqueIdDiv>
   <@markup id="html">
      <div id="${el}-body" class="statistic-tool">
        <div id="${el}-repo-usage">
          <div class="yui-g">
            <div class="yui-u first">
              <div class="title">${msg("label.title")}</div>
            </div>
            <div class="yui-u align-right">&nbsp;</div>
          </div>
          <div class="yui-g separator">
            <div class="header">
              <div class="criterias">
                <input type="button" class="criterias-button" id="${el}-module-criteria" name="module-criteria-button" value="${msg('graph.label.repository')}" />
                <select id="${el}-module-criteria-select" name="module-criteria-select">
                  <option value="document">${msg("graph.label.repository")}</option>
                </select>

                <input type="button" class="criterias-button" id="${el}-action-criteria" name="action-criteria-button" value="${msg('label.menu.action')}${msg('label.read')}" />
                <select id="${el}-action-criteria-select" name="action-criteria-select">
                  <option value="read">${msg("label.menu.action")}${msg("label.read")}</option>
                  <option value="updated">${msg("label.menu.action")}${msg("label.updated")}</option>
                  <option value="created">${msg("label.menu.action")}${msg("label.created")}</option>
                  <option value="deleted">${msg("label.menu.action")}${msg("label.deleted")}</option>
                </select>

                <input type="button" class="criterias-button" id="${el}-site-criteria" name="site-criteria-button" value="${msg('graph.label.repository')}" />
                <select id="${el}-site-criteria-select" name="site-criteria-select">
                  <option value="_repository">${msg("graph.label.repository")}</option>
                </select>

                <@stats.renderExportButton el />
              </div>

              <@stats.renderDateFiltersMenu el />
            </div>
          </div>

          <div id="${el}-chart-body" class="main-chart">
            <div class="browsing">
              <div id="${el}-chart-prev" class="img-prev-arrow" title="${msg('label.previous')}"></div>
              <div id="${el}-chart-next" class="img-next-arrow" title="${msg('label.next')}"></div>
            </div>
            <@stats.renderMainChartContainer el />
          </div>

          <@stats.renderPopularyTable el />
        </div>
        <@stats.renderAtolFooter />
      </div>
   </@>
</@>