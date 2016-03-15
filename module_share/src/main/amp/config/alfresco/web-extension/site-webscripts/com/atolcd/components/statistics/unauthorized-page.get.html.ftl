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

<div>
  <div class="theme-bg-color-2 theme-border-4" style="margin-top: 0.75em; padding: 0.5em 1em;">
    <h3>${msg("header.statistic.unauthorized.title")}</h3>

    <br>
    <div>
    <#if page.url.templateArgs.site?? && page.url.templateArgs.site?has_content>
      <#assign dashboardUrl = "${page.url.context}/page/site/${page.url.templateArgs.site}/dashboard" />
      ${msg("header.statistic.unauthorized.body.site", dashboardUrl)}
    <#else>
      <#assign dashboardUrl = "${page.url.context}/page/user/${user.id}/dashboard" />
      ${msg("header.statistic.unauthorized.body.user", dashboardUrl)}
    </#if>
    </div>
  </div>
</div>

<script type="text/javascript">//<![CDATA[
  window.setTimeout(function() {window.location = "${dashboardUrl}"}, 4000);
//]]></script>