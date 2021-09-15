<!--[if gt IE 8]>
  <!-- D3 and C3 loading -->
  <@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/C3/d3.js"></@script>
  <@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/C3/c3.js"></@script>
  <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/atolcd/statistics/C3/c3.css" />

  <!-- Canvg loading -->
  <@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/canvg/rgbcolor.js"></@script>
  <@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/canvg/StackBlur.js"></@script>
  <@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/canvg/canvg.js"></@script>

  <!-- select2 loading -->
  <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/atolcd/statistics/select2/select2.css" />
  <@script type="text/javascript" src="${page.url.context}/res/js/lib/jquery-1.11.1/jquery-1.11.1.min.js"></@script>
  <@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/select2/select2.js"></@script>

  <#assign shortLocale = "en" />
  <#if locale?? && locale?contains("fr")>
    <#assign shortLocale = locale[0..1] />
  </#if>
  <@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/select2/i18n/${shortLocale!'en'}.js" />
<![endif]-->

<!-- Admin Console for Share Stats Tool -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/atolcd/statistics/statistics-tool.css" />
<@script type="text/javascript" src="${page.url.context}/res/yui/calendar/calendar.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/statistics-tool.js"></@script>