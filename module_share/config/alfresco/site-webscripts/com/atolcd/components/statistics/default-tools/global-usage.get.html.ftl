<#assign el = args.htmlid?html />

<script type="text/javascript">//<![CDATA[
  new AtolStatistics.GlobalUsage("${args.htmlid?js_string}").setOptions({
      pathToSwf: "${page.url.context}/res/components/console/open_flash_chart/open-flash-chart.swf"
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

          <input type="button" class="criterias-button" id="module-criteria" name="${el}-module-criteria-button" value="${msg("label.documentlibrary")}">
          <select id="module-criteria-select" name="${el}-module-criteria-select">
            <option value="document">${msg("label.documentlibrary")}</option>
            <option value="wiki">${msg("label.wiki")}</option>
            <option value="blog">${msg("label.blog")}</option>
            <option value="discussions">${msg("label.discussion")}</option>
          </select>

          <input type="button" class="criterias-button" id="action-criteria" name="${el}-action-criteria-button" value="${msg("label.menu.action")}${msg("label.read")}">
          <select id="action-criteria-select" name="${el}-action-criteria-select">
            <option value="read">${msg("label.menu.action")}${msg("label.read")}</option>
            <option value="created">${msg("label.menu.action")}${msg("label.created")}</option>
            <option value="deleted">${msg("label.menu.action")}${msg("label.deleted")}</option>
            <option value="updated">${msg("label.menu.action")}${msg("label.updated")}</option>
          </select>

          <span id="site-criteria-container"></span>
        </div>

        <div class="utility-buttons">
          <span class="yui-button yui-push-button criterias-button" id="${el}-export-button">
            <span class="first-child"><button>${msg("button.export")}</button></span>
          </span>
        </div>

        <div class="yui-u separator dates-filter">
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
        </div>
      </div>
    </div>

    <div id="${el}-chart-body" class="main-chart">
      <div class="browsing">
        <div id="chart-prev" class="img-prev-arrow"></div>
        <div id="chart-next" class="img-next-arrow"></div>
      </div>
      <div id="${el}-chart-container" class="chart-container">
        <div class="chart" id="${el}-chart"></div>
      </div>
    </div>

    <table id="popularity-table">
      <tr>
        <td class="table-bottom">
          <div id="${el}-mostread-container">
            <div  id="${el}-mostread"></div>
          </div>
        </td>
        <td class="table-bottom">
          <div id="${el}-mostupdated-container">
            <div id="${el}-mostupdated"></div>
          </div>
        </td>
      </tr>
    </table>
  </div>
</div>