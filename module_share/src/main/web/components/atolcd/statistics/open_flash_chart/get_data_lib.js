/*
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
 */

function get_random_color() {
  var letters = '0123456789ABCDEF'.split('');
  var color = '#';
  for (var i = 0; i < 6; i++) {
    color += letters[Math.round(Math.random() * 15)];
  }
  return color;
}

function buildBarChartXLabels(params, currentSizeMin) {
  var labels = [],
      timeType = params.currentFilter,
      slicedDates = params.additionalsParams.tsString.split(","),
      truncateLabels = false;

  if (currentSizeMin && params.chartDomId) {
    var chartElt = document.getElementById(params.chartDomId);
    if (chartElt && chartElt.clientWidth <= currentSizeMin) {
      truncateLabels = true;
    }
  }

  switch (timeType) {
    case "years":
      for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
        labels[i] = Alfresco.thirdparty.dateFormat(new Date(parseInt(slicedDates[i], 10)), AtolStatistics.dateFormatMasks.fullMonth); // default: mmmm
        if (truncateLabels) {
          labels[i] = labels[i].substring(0,3);
        }
      }
      break;

    case "months":
      for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
        labels[i] = Alfresco.thirdparty.dateFormat(new Date(parseInt(slicedDates[i], 10)), AtolStatistics.dateFormatMasks.shortDay); // default: dd/mm
        if (truncateLabels) {
          labels[i] = labels[i].substring(0,2);
        }
      }
      break;

    case "weeks":
      for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
        labels[i] = Alfresco.thirdparty.dateFormat(new Date(parseInt(slicedDates[i], 10)), AtolStatistics.dateFormatMasks.mediumDay); // default: dddd dd/mm
        if (truncateLabels) {
          labels[i] = labels[i].substring(0,3);
        }
      }
      break;

    case "days":
      for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
        var timestamp = parseInt(slicedDates[i], 10),
            h1 = Alfresco.thirdparty.dateFormat(new Date(timestamp), AtolStatistics.dateFormatMasks.shortHour), // default: HH'h'
            h2 = Alfresco.thirdparty.dateFormat(new Date(timestamp + (2 * 60 * 60 * 1000)), AtolStatistics.dateFormatMasks.shortHour); // + 2 hours

        labels[i] = h1 + " - " + h2;
      }
      break;
  }
  return labels;
}

function addRotation(o, params){
  // labels rotation (45 degrees)
  if (params.currentFilter == "days"){ o.rotate = -45; }
}

function buildDateTitle(params) {
  var title = "",
      timeType = params.currentFilter,
      slicedDates = params.additionalsParams.tsString.split(","),
      from = new Date(parseInt(slicedDates[0], 10));

  switch (timeType) {
    case "years":
      title = getMessage(timeType, "graph.title.date.", from.getFullYear());
      break;

    case "months":
      var m = Alfresco.thirdparty.dateFormat(from, AtolStatistics.dateFormatMasks.fullMonth);
      title = getMessage(timeType, "graph.title.date.", m, from.getFullYear());
      break;

    case "weeks":
      title = getMessage(timeType, "graph.title.date.", from.getWeek(), from.getFullYear());
      break;

    case "days":
      title = getMessage(timeType, "graph.title.date.", Alfresco.thirdparty.dateFormat(from, AtolStatistics.dateFormatMasks.shortDate));
      break;
  }

  return title;
}

var red = "#EE1C2F",
    blue = "#19ABEA",lightBlue = "#1B9EFC",darkBlue = "#1B5AF9",
    green = "#7CBC28", darkGreen = "#0A9200",
    orange = "#FF9900", lightOrange = "#FFC600", darkOrange = "#FF692B",
    gray = "#C1C1C1", mediumGray = "#DFDFDF";

// Colors used by charts
var barChartColors = [],
    gridColors = [];

// Blog
barChartColors["blog.postview"] = blue;
barChartColors["blog.blog-create"] = darkGreen;
barChartColors["blog.blog-delete"] = red;
barChartColors["blog.blog-update"] = orange;

// Document Library
barChartColors["document.details"] = blue;
barChartColors["document.download"] = darkBlue;
barChartColors["document.create"] = green;
barChartColors["document.file-added"] = darkGreen;
barChartColors["document.file-deleted"] = red;
barChartColors["document.file-updated"] = darkOrange;
barChartColors["document.inline-edit"] = lightOrange;
barChartColors["document.update"] = orange;

// Wiki
barChartColors["wiki.page"] = blue;
barChartColors["wiki.create-post"] = darkGreen;
barChartColors["wiki.delete-post"] = red;
barChartColors["wiki.update-post"] = orange;

// Discussions
barChartColors["discussions.topicview"] = blue;
barChartColors["discussions.discussions-create"] = darkGreen;
barChartColors["discussions.discussions-deleted"] = red;
barChartColors["discussions.discussions-update"] = orange;


barChartColors["volumetry"] = blue;
barChartColors["users"] = blue;
barChartColors["most-popular"] = red;
barChartColors["less-popular"] = blue;

// Grids
gridColors["x-axis"] = gray;
gridColors["y-axis"] = gray;
gridColors["x-grid"] = mediumGray;
gridColors["y-grid"] = mediumGray;


var barStackedChartColors = {};
barStackedChartColors.defaultColors = ["#FF6201", "#75C7BB", "#D6191F", "#7CA900", "#373121", "#EB9B00", "#58C3F0", "#7D7B6A", "#EA2673", "#BCA8D0",
                                       "#8D625B", "#FFD370", "#009285", "#1B9EFC", "#0A9200", "#FF9900", "#C6E56F", "#755A04", "#80001B", "#291309"];