<#include "/org/alfresco/include/alfresco-template.ftl" />

<@templateHeader>
   <script type="text/javascript">//<![CDATA[
      new Alfresco.widget.Resizer("SiteStatistics").setOptions(
      {
         initialWidth: 190
      });
   //]]></script>
</@>

<@templateBody>
   <div id="alf-hd">
      <@region id="header" scope="global" />
      <@region id="title" scope="template" />
      <@region id="navigation" scope="template" />
   </div>
   <div id="bd">
      <div class="yui-t1" id="alfresco-site-statistics">
         <div id="yui-main">
            <div class="yui-b" id="alf-content">
               <@region id="myctool" scope="page" />
            </div>
         </div>
         <div class="yui-b" id="alf-filters">
            <@region id="tools" scope="template" />
         </div>
      </div>
   </div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global" />
   </div>
</@>