<#assign el = args.htmlid?html />

<#if showMenu?? && showMenu>
  <script type="text/javascript">//<![CDATA[
    new AtolStatistics.module.Menu("${el}").setOptions({
      currentUser: "${user.name?js_string}",
      isAdmin: ${user.isAdmin?string}
    }).setMessages(${messages});
  //]]></script>
</#if>