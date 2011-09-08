<!--[if IE]>
<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe>
<![endif]-->
<input id="yui-history-field" type="hidden" />

<#assign el=args.htmlid?html>
<script type="text/javascript">//<![CDATA[
   new Alfresco.ConsoleAudit("${el}").setMessages(${messages});

//]]></script>

<div id="${el}-body" class="audit">
   <!-- Tools panel -->
        <div id="${el}-audit" class="hidden">
          <div class="yui-g">
            <div class="yui-u first">
              <div class="title">
                ${msg("label.title")}
              </div>
            </div>
            <div class="yui-u align-right">&nbsp;</div>
          </div>
          <div class="yui-ge separator">
            <div class="yui-u first">
              <div class="title">
                ${msg("label.title.criterias")}
              </div>
              <div id="criterias" class="criterias">
                <input type="button" id="module-criteria" name="${el}-module-criteria-button" value="${msg("label.menu.module")}${msg("label.menu.all")}">
                <select id="module-criteria-select" name="${el}-module-criteria-select">
                  <option value="">${msg("label.menu.module")}${msg("label.menu.all")}</option>
                  <option value="wiki">${msg("label.menu.module")}${msg("label.wiki")}</option>
                  <option value="blog">${msg("label.menu.module")}${msg("label.blog")}</option>
                  <option value="document">${msg("label.menu.module")}${msg("label.documentlibrary")}</option>
                  <option value="discussions">${msg("label.menu.module")}${msg("label.discussion")}</option>
                  <option value="dashboard">${msg("label.menu.module")}${msg("label.dashboard")}</option>
                  <option value="calendar">${msg("label.menu.module")}${msg("label.calendar")}</option>
                  <option value="links">${msg("label.menu.module")}${msg("label.links")}</option>
                </select>
                <span class="vb"> | </span>
                <!-- ${msg("label.auditActionName")} -->
                <input type="button" id="action-criteria" name="${el}-action-criteria-button" value="${msg("label.menu.action")}${msg("label.menu.all")}">
                <select id="action-criteria-select" name="${el}-action-criteria-select">
                  <option value="">${msg("label.menu.action")}${msg("label.menu.all")}</option>
                  <option value="views">${msg("label.menu.action")}${msg("label.views")}</option>
                  <option value="comment">${msg("label.menu.action")}${msg("label.comments")}</option>
                  <option value="file">${msg("label.menu.action")}${msg("label.files")}</option>
                </select>
                <span class="vb"> | </span>
                <input type="button" id="site-criteria" name="${el}-site-criteria-button" value="${msg("label.menu.site")}${msg("label.menu.all")}">
                 
              </div>
            </div>
          </div>
          <div class="yui-ge separator">
            <div class="yui-u first">
              <div class="title">
                ${msg("label.title.time-criterias")}
              </div>
              <div id="time-criterias" class="criterias">  
                  <input type="text" id="input-date-from" value="" class="date-entry"/>
                  <img id="icon-from" class="datepicker-icon" src="/share/res/components/form/images/calendar.png"/>
                  <div id="calendar-date-to" class="calendar" style="display:none"></div>
                <span class="vb"> | </span>
                  <input type="text" id="input-date-to" value="" class="date-entry"/>
                  <img id="icon-to" class="datepicker-icon" src="/share/res/components/form/images/calendar.png"/>
                  <div id="calendar-date-from" class="calendar" style="display:none"></div>
                <span class="vb"> | </span>
                
                <input type="button" id="date-criteria" name="${el}-date-criteria-button" value="${msg("label.menu.date")}${msg("label.menu.none")}">
                <select id="date-criteria-select" name="${el}-date-criteria-select">
                  <option value="">${msg("label.menu.date")}${msg("label.menu.none")}</option>
                  <option value="-by-month">${msg("label.menu.date")}${msg("label.byMonth")}</option>
                  <option value="-by-week">${msg("label.menu.date")}${msg("label.byWeek")}</option>
                  <option value="-by-day">${msg("label.menu.date")}${msg("label.byDay")}</option>
                </select>
              </div>
            </div>
          </div>
          <div class="yui-g separator">
            <div class="yui-u first">
              <div class="search-text">
                <div class="search-button">
                  <span class="yui-button yui-push-button" id="${el}-search-button">
                     <span class="first-child"><button>${msg("button.search")}</button></span>
                  </span>
                  <span class="vb"> | </span>
                  
                  <span class="yui-button yui-push-button" id="${el}-export-button">
                     <span class="first-child"><button>${msg("button.export")}</button></span>
                  </span>
                </div>

              </div>
            </div>
            <div class="yui-u align-right">&nbsp;</div>
          </div>
          <!-- div class="results" id="${el}-datatable"></div-->
          <div id="${el}-chart-container">
            <div class="chart" id="${el}-chart"></div>
          </div>
      </div>


</div>