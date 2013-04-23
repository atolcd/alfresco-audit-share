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

<div id="${el}-body" class="audit">
  <div id="${el}-audit">
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
          <label for="site-criteria-container">${msg("label.menu.site")}</label><span id="site-criteria-container"></span>
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
            <span id="by-days">
              <a href="#">${msg("label.byDay")}</a>
            </span>
            <span class="vb"> | </span>
            <span id="by-weeks" class="selected">
              <a href="#">${msg("label.byWeek")}</a>
            </span>
            <span class="vb"> | </span>
            <span id="by-months">
              <a href="#">${msg("label.byMonth")}</a>
            </span>
            <span class="vb"> | </span>
            <span id="by-years">
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