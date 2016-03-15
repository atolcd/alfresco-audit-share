<#--
 * Copyright (C) 2013 Atol Conseils et Développements.
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
<#macro dateFormat date=""><#if date?is_date>${xmldate(date)}</#if></#macro>
<#macro auditCount auditItems>
  <#escape x as jsonUtils.encodeJSONString(x)>
    "totalResults": <#if args.combined??>1<#else>${auditItems?size?c}</#if>,
    "items" :
    [
      <#if args.combined??>
        {
          <#assign count = 0 />
          <#list auditItems as auditItem><#assign count = (count + auditItem.count) /></#list>
          "count": ${count},
          "target": "${args.type!""}"
        }
      <#else>
        <#list auditItems as auditItem>
          {
            "count": ${auditItem.count?c},
            "target": "${auditItem.target}"
          }<#if auditItem_has_next>,</#if>
        </#list>
      </#if>
    ]
  </#escape>
</#macro>

<#escape x as jsonUtils.encodeJSONString(x)>
  
    {
      "items" :
      [
      <#if popularity?exists>
    	<#-- By popularity -->
    	<#if popularity?size !=0>
    	<#assign version = "1.0">
        <#list popularity as auditItem>
        <#assign node = companyhome.nodeByReference[auditItem.auditObject]>
          {
            "version": <#if node.hasAspect("cm:versionable")>"${node.properties["cm:versionLabel"]}"<#else>"${version}"</#if>,       
	       	"nodeType": "${node.typeShort}",
	       	"type": "${node.type}",
	       	"mimetype": "${node.mimetype!""}",
	       	"isFolder": ${node.isContainer?string},
	       	"fileName": "${node.name}",
	       	"title": <#escape x as jsonUtils.encodeJSONString(x)> "${node.properties.title!""}"</#escape>,
	       	"description":  <#escape x as jsonUtils.encodeJSONString(x)> "${node.properties.description!""}"</#escape>,
	       	"author": "${node.properties.author!""}",
	       	"createdOn": "<@dateFormat node.properties.created />",
	       	"createdBy": "${node.properties.creator}",
	       	"createdByUser": "${node.properties.creator}",
	       	"modifiedOn": "<@dateFormat node.properties.modified />",
	       	"modifiedBy": "${node.properties.modifier}",
	       	"modifiedByUser": "${node.properties.modifier}",      
	       	"categories":  <#if node.hasAspect("cm:generalclassifiable")>[<#list node.properties.categories![] as c>["${c.name}", "${c.displayPath?replace("/categories/General","")}"]<#if c_has_next>,</#if></#list>]<#else>[]</#if>,
	       	"tags":  <#if node.tags?exists>[<#list node.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>]<#else>[]</#if>,
	       	"size": "${node.size?c}",  
	       	"contentUrl": "api/node/content/${node.storeType}/${node.storeId}/${node.id}/${node.name?url}",
	       	"webdavUrl": "${node.webdavUrl}",
            "popularity": ${auditItem.popularity?c},
            "average": ${auditItem.average},
            "auditUser": "${auditItem.auditUser}",
            "nodeRef": "${auditItem.auditObject}",            
            "name": "${auditItem.objectName}",
            "displayName": "${auditItem.objectDisplayName}",
             "location":
			   {
			      "repositoryId": "${(node.properties["trx:repositoryId"])!(server.id)}",
			      "site": "${auditItem.auditSite!""}",
			      "siteTitle": "${shareStatsutils.getSiteTitle(auditItem.auditSite)!""}",
			      "container": "${auditItem.siteComponent!""}",			      
			      "file": "${node.name!""}"
			      
			   }
          }<#if auditItem_has_next>,</#if>
        </#list>
        </#if>
      ]
    }
    
  <#elseif dates?exists>
    <#-- By dates -->
    <#if dates?size !=0>
    {
      "totalResults": ${dates?size?c},
      "items":
      [
        <#list dates as date>
        {
          <@auditCount auditItems=date />
        }<#if date_has_next>,</#if>
        </#list>
      ]
    }
    </#if>
  </#if>
</#escape>