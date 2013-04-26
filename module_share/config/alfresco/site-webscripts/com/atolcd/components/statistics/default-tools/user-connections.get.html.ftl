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
  new AtolStatistics.UserConnections("${args.htmlid?js_string}").setOptions({
      pathToSwf: "${page.url.context}/res/components/atolcd/statistics/open_flash_chart/open-flash-chart.swf",
      siteId: "${page.url.templateArgs.site!""}",
      currentUser: "${user.name?js_string}",
      isAdmin: ${user.isAdmin?string}
    }
  ).setMessages(${messages});
//]]></script>

<div id="${el}-body" class="statistic-tool">
  <div id="${el}-user-connections">
    <div class="yui-g">
      <div class="yui-u first">
        <div class="title">
          ${msg("label.title")}
        </div>
      </div>
      <div class="yui-u align-right">&nbsp;</div>
    </div>
    <div class="yui-g separator">
      <div class="header">
        <div id="criterias" class="criterias">
          <label>${msg("label.menu.site")}</label><span class="criterias-button"><input type="button" id="site-criteria" name="${el}-site-criteria-button" value="" /></span>

          <span class="criterias-button">
            <input type="button" id="chart-type-criteria" name="${el}-chart-type-criteria-button" value="${msg('graph.type.bar')}" />
            <select id="chart-type-criteria-select" name="${el}-chart-type-criteria-select">
              <option value="bar">${msg("graph.type.bar")}</option>
              <option value="line">${msg("graph.type.line")}</option>
            </select>
          </span>
        </div>
      </div>
    </div>

    <div id="${el}-chart-body" class="main-chart">
      <div class="separator browsing">
          <div id="chart-prev" class="img-prev-arrow"></div>

          <div class="yui-u filters">
            <span id="home">
              <span class="home-img" title="${msg('label.home')}"></span>
            </span>
            <span id="${el}-by-days">
              <a href="#">${msg("label.byDay")}</a>
            </span>
            <span class="vb"> | </span>
            <span id="${el}-by-weeks">
              <a href="#">${msg("label.byWeek")}</a>
            </span>
            <span class="vb"> | </span>
            <span id="${el}-by-months">
              <a href="#">${msg("label.byMonth")}</a>
            </span>
            <span class="vb"> | </span>
            <span id="${el}-by-years">
              <a href="#">${msg("label.byYear")}</a>
            </span>

            <span class="export-button">
              <span class="yui-button yui-push-button" id="${el}-export-button">
                <span class="first-child"><button>${msg("button.export")}</button></span>
              </span>
            </span>
          </div>

          <div id="chart-next" class="img-next-arrow"></div>
      </div>
      <div id="${el}-chart-container" class="chart-container">
        <div class="chart" id="${el}-chart"></div>
      </div>
    </div>

    <div class="users-table-container">
      <table id="users-table">
        <tr>
          <th id="${el}-users-recently-connected-header">${(msg("label.users.recently-connected"))}</th>
          <th id="${el}-users-connected-header">${(msg("label.users.connected"))}</th>
          <th id="${el}-users-never-connected-header" class="last">${(msg("label.users.never-connected"))}</th>
        </tr>
        <tr>
          <td id="${el}-users-recently-connected" class="users-list">
          </td>
          <td id="${el}-users-connected" class="users-list">
          </td>
          <td id="${el}-users-never-connected" class="users-list last">
          </td>
        </tr>
      </table>
    </div>
  </div>
</div>