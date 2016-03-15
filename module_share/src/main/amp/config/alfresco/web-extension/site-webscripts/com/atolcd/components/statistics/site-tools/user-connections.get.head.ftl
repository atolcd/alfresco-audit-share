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

<!-- Admin Console for Share Stats Tool -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/atolcd/statistics/statistics-tool.css" />
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/atolcd/statistics/user-connections.css" />

<@script type="text/javascript" src="${page.url.context}/res/yui/calendar/calendar.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/statistics-tool.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/user-connections.js"></@script>

<!-- Open Flash Chart -->
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/open_flash_chart/swfobject.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/open_flash_chart/open_flash_chart.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/open_flash_chart/ofc.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/open_flash_chart/get_user_data.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/atolcd/statistics/open_flash_chart/get_data_lib.js"></@script>