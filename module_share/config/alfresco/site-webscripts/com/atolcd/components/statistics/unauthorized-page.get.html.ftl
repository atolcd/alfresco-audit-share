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