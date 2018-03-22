<#include "/org/alfresco/components/component.head.inc">

<#--
 * Copyright (C) 2018 Atol Conseils et Développements.
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

<#assign addGroupToDependencies = (serverVersion?? && (serverVersion gte 4.2)) />


<@myscript type="text/javascript" src="${url.context}/res/components/atolcd/upload/insert-audit-request.lib.js" group="documentlibrary" addGrp=addGroupToDependencies />

<#if url.templateArgs.uploadType??>
  <#if url.templateArgs.uploadType == "html">
    <@myscript type="text/javascript" src="${url.context}/res/components/atolcd/upload/custom_html_onUploadSuccess.js" group="upload" addGrp=addGroupToDependencies />
  <#elseif url.templateArgs.uploadType == "flash">
    <@myscript type="text/javascript" src="${url.context}/res/components/atolcd/upload/custom_flash_onUploadCompleteData.js" group="upload" addGrp=addGroupToDependencies />
  <#elseif url.templateArgs.uploadType == "dnd">
    <@myscript type="text/javascript" src="${url.context}/res/components/atolcd/upload/custom_dnd_onFileUploadComplete.js" group="upload" addGrp=addGroupToDependencies />
  </#if>
<#elseif url.templateArgs.component?? && url.templateArgs.component == "document-details-dependencies">
  <@myscript type="text/javascript" src="${url.context}/res/components/atolcd/document-details/custom_onNewVersionUploadCompleteCustom.js" group="documentlibrary" addGrp=addGroupToDependencies />
  <@myscript type="text/javascript" src="${url.context}/res/components/atolcd/document-details/custom_onActionDeleteConfirm.js" group="documentlibrary" addGrp=addGroupToDependencies />
<#elseif url.templateArgs.component?? && url.templateArgs.component == "doclib-dependencies">
  <@myscript type="text/javascript" src="${url.context}/res/components/atolcd/documentlibrary/fileDeleted.js" group="documentlibrary" addGrp=addGroupToDependencies />
</#if>

<#-- JavaScript minimisation via YUI Compressor -->
<#macro myscript type src group addGrp>
  <#if addGrp>
    <@script type=type src=src group=group />
  <#else>
    <@script type=type src=src />
  </#if>
</#macro>