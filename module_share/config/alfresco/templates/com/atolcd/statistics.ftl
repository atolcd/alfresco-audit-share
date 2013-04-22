<#include "/org/alfresco/include/alfresco-template.ftl" />

<@templateHeader>
  <#if userIsAllowed?? && userIsAllowed>
    <script type="text/javascript">//<![CDATA[
      new Alfresco.widget.Resizer("Statistics").setOptions(
      {
         initialWidth: 190
      });
    //]]></script>
  </#if>
</@>

<@templateBody>
   <div id="alf-hd">
      <@region id="header" scope="global" />
      <#if page.url.templateArgs.site?? && page.url.templateArgs.site?has_content>
        <@region id="site-title" scope="template" />
        <@region id="navigation" scope="template" />
      <#else>
        <@region id="title" scope="template" />
      </#if>
   </div>
   
   <div id="bd">
      <#if userIsAllowed?? && userIsAllowed>
      <div class="yui-t1" id="alfresco-statistics">
         <div id="yui-main">
            <div class="yui-b" id="alf-content">
               <@region id="myctool" scope="page" />
            </div>
         </div>
         <div class="yui-b" id="alf-filters">
            <@region id="tools" scope="template" />
         </div>
      </div>
      <#else>
        <@region id="unauthorized" scope="template" />
      </#if>
   </div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global" />
   </div>
</@>