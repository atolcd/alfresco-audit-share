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

<#macro renderDateFiltersMenu el>
  <div class="yui-u separator filters">
    <span id="${el}-home" class="home">
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
  </div>
</#macro>

<#macro renderExportButton el>
  <input type="button" class="criterias-button" id="${el}-export-button" name="export-button" value="${msg('button.export')}" />
  <select id="${el}-export-button-select" name="export-button-select">
    <option value="onCSVExport">${msg("label.export.csv")}</option>
    <option value="onIMGExport">${msg("label.export.img")}</option>
  </select>
</#macro>

<#macro renderMainChartContainer el>
  <div id="${el}-chart-container" class="chart-container">
    <div class="chart" id="${el}-chart"></div>
  </div>
</#macro>

<#macro renderPopularyTable el>
  <table class="popularity-table">
    <tr>
      <td class="table-bottom">
        <div id="${el}-mostread-container">
          <div id="${el}-mostread" class="my-popularity-chart"></div>
        </div>
      </td>
      <td class="table-bottom">
        <div id="${el}-mostupdated-container">
          <div id="${el}-mostupdated" class="my-popularity-chart"></div>
        </div>
      </td>
    </tr>
  </table>
</#macro>

<#macro renderAtolFooter>
  <div class="yui-g footer-atol">
    <a href="http://www.atolcd.com" target="_blank" title="Atol Conseils & Développements">
      <img src="${page.url.context}/res/components/atolcd/statistics/img/atolcd.png" alt="Atol C&D" class="footer-atol-img" />&nbsp;<span class="footer-atol-text">${msg("developped.by.atolcd")}</span>
    </a>
  </div>
</#macro>