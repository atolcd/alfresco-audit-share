<#import "/com/atolcd/components/statistics/statistics-tools.lib.ftl" as stats>

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

<script type="text/javascript">//<![CDATA[
  new AtolStatistics.Volumetry("${args.htmlid?js_string}").setOptions({
      siteId: "${page.url.templateArgs.site!""}",
      currentUser: "${user.name?js_string}",
      isAdmin: ${user.isAdmin?string},
      currentDateFilter: "months",
      chartLabelSizeMin: 850
    }
  ).setMessages(${messages});
//]]></script>

<div id="${el}-body" class="statistic-tool">
  <div id="${el}-volumetry">
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
            <input type="button" id="${el}-chart-type-criteria" name="chart-type-criteria-button" value="${msg('graph.type.line')}" />
            <select id="${el}-chart-type-criteria-select" name="chart-type-criteria-select">
              <option value="line">${msg("graph.type.line")}</option>
              <option value="bar">${msg("graph.type.bar")}</option>
            </select>
          </span>
        </div>

        <@stats.renderExportButton el />
      </div>
      <div id="${el}-bar-stack-criteria-container" class="bar-stack-criteria-container">
        <input id="${el}-bar_stack-criteria" type="checkbox"></input><label for="${el}-bar_stack-criteria">${msg("label.menu.use.bar_stack")}</label>
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
  </div>
  <@stats.renderAtolFooter />
</div>