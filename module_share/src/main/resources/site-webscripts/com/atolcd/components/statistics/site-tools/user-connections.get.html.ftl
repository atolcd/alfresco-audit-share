<#import "/com/atolcd/components/statistics/statistics-tools.lib.ftl" as stats>

<#include "/org/alfresco/components/component.head.inc">
<#include "/com/atolcd/components/statistics/chart-ressources.lib.ftl">

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

<#assign el = args.htmlid?html />

<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/atolcd/statistics/user-connections.css" group="auditShare"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/user-connections.js" group="auditShare"/>
</@>

<@markup id="widgets">
   <@createWidgets group="auditShare"/>
</@>

<@uniqueIdDiv>
   <@markup id="html">
      <div id="${el}-body" class="statistic-tool">
        <div id="${el}-user-connections">
          <div class="yui-g">
            <div class="yui-u first">
              <div class="title">${msg("label.title")}</div>
            </div>
            <div class="yui-u align-right">&nbsp;</div>
          </div>
          <div class="yui-g separator">
            <div class="header">
              <div class="criterias">
                <label>${msg("label.menu.site")}</label><span class="criterias-button"><input type="button" id="${el}-site-criteria" name="site-criteria-button" value="" /></span>

                <span class="criterias-button">
                  <input type="button" id="${el}-chart-type-criteria" name="chart-type-criteria-button" value="${msg('graph.type.bar')}" />
                  <select id="${el}-chart-type-criteria-select" name="chart-type-criteria-select">
                    <option value="bar">${msg("graph.type.bar")}</option>
                    <option value="line">${msg("graph.type.line")}</option>
                  </select>
                </span>

                <@stats.renderExportButton el />

                <select id="${el}-usergroup-criteria-select" name="usergroup-criteria-select" multiple="multiple"></select>
              </div>
            </div>
          </div>

          <div id="${el}-chart-body" class="main-chart">
            <div class="separator browsing">
                <div id="${el}-chart-prev" class="img-prev-arrow" title="${msg('label.previous')}"></div>
                <@stats.renderDateFiltersMenu el />
                <div id="${el}-chart-next" class="img-next-arrow" title="${msg('label.next')}"></div>
            </div>
            <@stats.renderMainChartContainer el />
          </div>

          <div class="users-table-container">
            <table class="users-table">
              <tr>
                <th id="${el}-users-recently-connected-header">${(msg("label.users.recently-connected"))}</th>
                <th id="${el}-users-connected-header">${(msg("label.users.connected"))}</th>
                <th id="${el}-users-never-connected-header" class="last">${(msg("label.users.never-connected"))}</th>
              </tr>
              <tr>
                <td id="${el}-users-recently-connected" class="users-list"></td>
                <td id="${el}-users-connected" class="users-list"></td>
                <td id="${el}-users-never-connected" class="users-list last"></td>
              </tr>
            </table>
          </div>
        </div>
        <@stats.renderAtolFooter />
      </div>
   </@>
</@>