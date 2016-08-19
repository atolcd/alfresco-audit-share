<#import "/com/atolcd/components/statistics/statistics-tools.lib.ftl" as stats>
<#include "/com/atolcd/components/statistics/chart-ressources.lib.ftl">

<#include "/org/alfresco/components/component.head.inc">

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
   <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/atolcd/statistics/data-purge.css" group="auditShare"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/data-purge.js" group="auditShare"/>
</@>

<@markup id="widgets">
   <@createWidgets group="auditShare"/>
</@>

<@uniqueIdDiv>
   <@markup id="html">
      <div id="${el}-body" class="statistic-tool">
        <div id="${el}-data-purge">
          <div class="yui-g">
            <div class="yui-u first">
              <div class="title">${msg("label.title")}</div>
            </div>
            <div class="yui-u align-right">&nbsp;</div>
          </div>
          <div class="yui-g separator">
            <div class="header">
              <div class="criterias">
                <input type="button" class="criterias-button" id="${el}-table-criteria" name="table-criteria-button" value="${msg('purge-db.label.audit')}" />
                <select id="${el}-table-criteria-select" name="table-criteria-select">
                  <option value="audit_entry">${msg("purge-db.label.audit")}</option>
                  <option value="volumetry">${msg("purge-db.label.volumetry")}</option>
                  <option value="all">${msg("purge-db.label.all")}</option>
                </select>

                <input type="button" class="criterias-button" id="${el}-site-criteria" name="site-criteria-button" value="" />
               <@stats.renderExportButton el />
              </div>

              <div id="${el}-repository-purge-criteria-container" class="repository-purge-criteria-container">
                <br /><br /><input id="${el}-repository_purge-criteria" type="checkbox"></input><label for="${el}-repository_purge-criteria">${msg("label.menu.repository_purge")}</label>
              </div>

              <div id="${el}-purge_period-criteria-container" class="purge_period-criteria-container">
                <br /><br /><label>${msg("label.menu.period")}</label><br /><br />
                <label>${msg("label.menu.from")}</label><span class="period-fields"><input type="text" id="${el}-period-from" name="period-from-field" value="" /></span><label>   </label>
                <label>${msg("label.menu.to")}</label><span class="period-fields"><input type="text" id="${el}-period-to" name="period-to-field" value="" /></span><label>   </label>
                <input id="${el}-purge_all-criteria" type="checkbox"></input><label for="${el}-purge_all-criteria">${msg("label.menu.purgeall")}</label>
              </div>

              <br /><br /><@stats.renderPurgeButton el />
            </div>
          </div>
        </div>
        <br /><br /><@stats.renderAtolFooter />
      </div>
   </@>
</@>