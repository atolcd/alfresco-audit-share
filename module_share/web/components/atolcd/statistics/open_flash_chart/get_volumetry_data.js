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

function getVolumetryFlashData(param) {
  var params = YAHOO.lang.JSON.parse(unescape(param)),
      jsonChart = null;

  jsonChart = buildChart(params);

  return YAHOO.lang.JSON.stringify(jsonChart);
};

function buildTitle(params) {
  var title = "",
      site = params.additionalsParams.site,
      siteTitle = params.additionalsParams.siteTitle || '';

  if (site && site.indexOf(',') == -1) {
    var opt = '<i>"' + ((siteTitle != "") ? siteTitle : site) + '"</i>';
    title = getMessage("site", "graph.title.", opt);
  } else {
    title = getMessage("all", "graph.title.");
  }

  title += buildDateTitle(params);
  return title;
}

function buildChart(params) {
  params.max = 0;
  var x_labels = buildXAxisLabels(params);
  var bars = {
    "title": {
      "text": buildTitle(params),
      "style": "{font-size: 16px; color:#515D6B; font-family: Arial,sans-serif; font-weight: bold; text-align: center; margin-top: 5px; margin-bottom: 10px;}"
    },

    "bg_colour": "#FFFFFF",

    "x_axis": {
      "colour": gridColors["x-axis"],
      "grid-colour": gridColors["x-grid"],
      "labels": x_labels
    }
  };

  var max = params.maxCount,
      steps = 10;

  if (params.additionalsParams.chartType == "vbar") {
    bars.elements = buildSingleChartElements(params, x_labels.labels);
  } else if (params.additionalsParams.chartType == "line") {
    bars.elements = buildSingleChartElements(params, x_labels.labels, params.additionalsParams.chartType);
  } else if (params.additionalsParams.chartType == "lines") {
    max = params.maxLocal;
    bars.elements = buildLinesChartElements(params, x_labels.labels);
  } else {
    bars.elements = buildStackedBarChartElements(params, x_labels.labels);
    bars.tooltip = { "mouse": 2 };
  }

  // Max value ?
  var maxInfo = AtolStatistics.util.formatFileSize(max);
  max = maxInfo.value ? roundMax(maxInfo.value, steps) : 10;

  bars["y_axis"] = {
    "steps": Math.ceil(max / steps),
    "colour": gridColors["y-axis"],
    "grid-colour": gridColors["y-grid"],
    "offset": 0,
    "max": max
  };

  bars["y_legend"] = {
    "text": getMessage("volumetry", "graph.label.", maxInfo.message),
    "style": "{font-size: 12px; color: #778877}"
  };

  return bars;
}

function buildSingleChartElements(params, labels, type) {
  var values = [],
      maxInfo = AtolStatistics.util.formatFileSize(params.maxCount);

  for (var i=0, ii=params.values.length ; i<ii ; i++) {
    var value = roundNumber(params.values[i] / maxInfo.unitValue, 2);

    var tip = labels[i] + "\n" + getMessage("volumetry", "graph.label.", maxInfo.message) + " : " + value + " " + maxInfo.message;
    if (params.sites && params.sites.length == 1) {
      tip += "\n" + getMessage("label.menu.site") + " " + params.sites[0];
    }

    var elt = {
      top: value,
      tip: tip
    };

    if (type && type == "line") {
      if (value == 0) {
        elt = null;
      } else {
        elt.type = "dot";
        elt.value = value;
      }
    }

    values.push(elt);
  }

  var element = {
    "type": "bar_glass",
    "alpha": 0.75,
    "colour": barChartColors["volumetry"],
    "font-size": 10,
    "values": values
  };

  if (type && type == "line") {
    element.type = "line";
    element.width = 3;
    element["dot-style"] = {
      "type": "dot",
      "dot-size": 4,
      "halo-size": 1,
      "colour": barChartColors["volumetry"]
    };
  }

  return [element];
}

function buildStackedBarChartElements(params, labels) {
  var values = [],
      maxInfo = AtolStatistics.util.formatFileSize(params.maxCount);

  for (var i=0, ii=params.stackedValues.length ; i < ii; i++) {
    var stackedValue = params.stackedValues[i]
        valueTab = [];

    for (var j=0, jj=stackedValue.length ; j<jj ; j++) {
      var value = roundNumber(stackedValue[j] / maxInfo.unitValue, 2);
      var value_obj = {
        val: value
      };

      if (value > 0) {
        value_obj.tip = labels[i] + "\n\n";
        value_obj.tip += getMessage("volumetry", "graph.label.", maxInfo.message) + " : " + value + " " + maxInfo.message + "\n";
        value_obj.tip += getMessage("label.menu.site") + " " + params.sites[j] + "\n\n";
        value_obj.tip += getMessage("graph.label.global.volumetry") + " #total# " + maxInfo.message;
      }
      else {
        // HACK: resolves "tooltips" display problems
        if (params.values[i] == 0 && j == 0) {
          value_obj.tip = getMessage("label.graph.no-data");
        }
      }

      valueTab.push(value_obj);
    }

    values.push(valueTab);
  }
  return [{
    "type": "bar_stack",
    "alpha": 0.7,
    "colours" : barStackedChartColors.defaultColors,
    "font-size": 10,
    "values": values
  }];
}

function buildLinesChartElements(params, labels) {
  var lines = [],
      linesValues = {},
      maxInfo = AtolStatistics.util.formatFileSize(params.maxLocal);

  for (var i=0, ii=params.stackedValues.length ; i < ii; i++) {
    var periodValues = params.stackedValues[i];
    for (var j=0, jj=periodValues.length ; j<jj ; j++) {
      var dotValue = roundNumber(periodValues[j] / maxInfo.unitValue, 2);
      if (!linesValues[j]) {
        linesValues[j] = {
          siteTitle: params.sites[j],
          values: []
        };
      }

      var dot = {};
      dot.type = "dot";

      if (dotValue > 0) {
        dot.value = dotValue;
        dot.tip = labels[i] + "\n\n";
        dot.tip += getMessage("label.menu.site") + " " + params.sites[j];
        dot.tip += "\n" + getMessage("volumetry", "graph.label.", maxInfo.message) + " : " + dotValue + " " + maxInfo.message;
      } else {
        dot = null;
      }

      linesValues[j].values.push(dot);
    }
  }

  var cpt = 0;
  for (var siteValues in linesValues) {
    var colour = barStackedChartColors.defaultColors[cpt%barStackedChartColors.defaultColors.length];
    lines.push({
      "type": "line",
      "values": linesValues[siteValues].values,
      "dot-style": {
        "type": "dot",
        "dot-size": 4,
        "halo-size": 1,
        "colour": colour
      },
      "width": 3,
      "colour": colour,
      "font-size": 10
    });

    cpt ++;
  }

  return lines;
}

function buildXAxisLabels(params) {
  var steps = params.values.length >= 30 ? Math.round(params.values.length / 15) : 1;
  var labelConfiguration = {
    "labels": buildBarChartXLabels(params),
    "steps": steps
  }
  addRotation(labelConfiguration, params);
  return labelConfiguration;
}

function roundMax(max, steps) {
  var step = max / steps,
      ceil = 1;

  if (max < steps) {
    ceil = roundNumber(step, 2);
    return roundNumber((ceil * steps) + roundNumber(ceil, 2), 1);
  } else if (step < steps) {
    ceil = Math.ceil(step);
    return (ceil * steps);
  } else {
    ceil = Math.ceil(step);
    return (ceil * steps) + (Math.ceil(ceil / steps) * 5);
  }

}

function roundNumber(number, digits) {
  var multiple = Math.pow(10, digits);
  var rndedNum = Math.round(number * multiple) / multiple;
  return rndedNum;
}


function getMessage(messageId, prefix) {
  var msg = (prefix) ? prefix + messageId : messageId;
  var res = Alfresco.util.message.call(null, msg, "AtolStatistics.Volumetry", Array.prototype.slice.call(arguments).slice(2));
  res = (res.search("graph.label") == 0) ? messageId : res;
  return res;
}