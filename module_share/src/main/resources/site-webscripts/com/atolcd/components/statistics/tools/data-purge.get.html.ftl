<#import "/com/atolcd/components/statistics/statistics-tools.lib.ftl" as stats>
<#include "/com/atolcd/components/statistics/chart-ressources.lib.ftl">

<#include "/org/alfresco/components/component.head.inc">

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
                <input id="${el}-repository_purge-criteria" class="repository-checkbox" title="${msg("label.menu.repository_tooltip")}" type="checkbox"></input><span class="repository-label" for="${el}-repository_purge-criteria">${msg("label.menu.repository_purge")}</span>
               <@stats.renderExportButton el />
              </div>
              <div id="${el}-purge_period-criteria-container" class="purge_period-criteria-container">
                <br /><br /><label>${msg("label.menu.period")}</label>
                <div>
                  <br /><label class="from-label">${msg("label.menu.from")}</label>
                  <input type="text" id="${el}-period-from" name="period-from-field" class="field-period-select" maxlength="10" value="" />
                  <a id="${el}-period-from-calendar_img">
                    <img src="/share/res/components/atolcd/statistics/img/calendar.png" class="period-from-calendar" title="${msg("label.menu.calendar_start_title")}">
                  </a>
                  <div id="${el}-from-calendar" class="from-calendar-container"></div>
                  <label class="to-label">${msg("label.menu.to")}</label>
                  <input type="text" id="${el}-period-to" name="period-to-field" class="field-period-select" maxlength="10" value="" />
                  <a id="${el}-period-to-calendar_img">
                    <img src="/share/res/components/atolcd/statistics/img/calendar.png" class="period-to-calendar" title="${msg("label.menu.calendar_end_title")}">
                  </a>
                  <div id="${el}-to-calendar" class="to-calendar-container"></div>
                  <input id="${el}-purge_all-criteria" class="checkbox-all-criteria" type="checkbox"></input><label class="criteria-label" for="${el}-purge_all-criteria">${msg("label.menu.purgeall")}</label>
                </div>
                <div class="period-info">
                  <label class="from-period-format">${msg("label.menu.date_format")}</label>
                  <label class="to-period-format">${msg("label.menu.date_format")}</label>
                </div>
                <br /><br /><@stats.renderPurgeButton el />
              </div>
            </div>
          </div>
        </div>
        <br /><br /><@stats.renderAtolFooter />
      </div>
   </@>
</@>