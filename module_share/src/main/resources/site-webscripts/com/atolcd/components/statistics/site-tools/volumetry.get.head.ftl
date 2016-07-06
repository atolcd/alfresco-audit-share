<#include "/org/alfresco/components/component.head.inc">

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

<!-- D3 and C3 loading -->
<script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/C3/d3.js"></script>
<script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/C3/c3.js"></script>
<link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/atolcd/statistics/C3/c3.css" >

<!-- Admin Console for Share Stats Tool -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/atolcd/statistics/statistics-tool.css" />
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/atolcd/statistics/volumetry.css" />

<@script type="text/javascript" src="${page.url.context}/res/yui/calendar/calendar.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/statistics-tool.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/volumetry.js"></@script>

<!-- Open Flash Chart -->
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/open_flash_chart/swfobject.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/open_flash_chart/open_flash_chart.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/open_flash_chart/ofc.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/open_flash_chart/get_volumetry_data.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/open_flash_chart/get_data_lib.js"></@script>