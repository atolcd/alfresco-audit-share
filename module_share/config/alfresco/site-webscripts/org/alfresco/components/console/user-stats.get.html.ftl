<!--[if IE]>
<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe>
<![endif]-->
<input id="yui-history-field" type="hidden" />

<#assign el=args.htmlid?html>
<script type="text/javascript">//<![CDATA[
   new Alfresco.ConsoleUserAudit("${el}").setMessages(${messages});
//]]></script>

<div id="${el}-body" class="audit">
  <!-- Tools panel class="hidden" -->
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
          <label for="site-criteria-container">${msg("label.menu.site")}</label><div id="site-criteria-container"></div>
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

    <table id="users-table">
      <tr>
        <th>${(msg("label.users.recently-connected"))}</th>
        <th>${(msg("label.users.connected"))}</th>
        <th>${(msg("label.users.never-connected"))}</th>
      </tr>
      <tr>
        <td id="${el}-users-recently-connected" class="users-list">
        </td>
        <td id="${el}-users-connected" class="users-list">
        </td>
        <td id="${el}-users-never-connected" class="users-list">
        </td>
      </tr>
    </table>
  </div>
</div>