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

function getFlashData(param) {
  var params = YAHOO.lang.JSON.parse(unescape(param)),
      jsonChart = null;

  switch (params.additionalsParams.chartType) {
    case "vbar":
      jsonChart = buildBarChart(params);
      break;

    case "hbar":
      jsonChart = buildHBarChart(params);
      break;
  }

  return YAHOO.lang.JSON.stringify(jsonChart);
};

function buildTitle(params) {
  return getMessage(params.additionalsParams.type, "graph.title.") + buildDateTitle(params);
}

function buildBarChart(params) {
  params.max = 0;
  var x_labels = buildXAxisLabels(params);
  var bars = {
    "title": {
      "text": buildTitle(params),
      "style": "{font-size: 16px; color:#515D6B; font-family: Arial,sans-serif; font-weight: bold; text-align: center; margin-bottom: 15px;}"
    },

    "bg_colour": "#FFFFFF",
    "elements": buildBarChartElements(params, x_labels.labels),

    "x_axis": {
      "colour": gridColors["x-axis"],
      "grid-colour": gridColors["x-grid"],
      "labels": x_labels
    },

    "y_axis": {
      "steps": params.step,
      "colour": gridColors["y-axis"],
      "grid-colour": gridColors["y-grid"],
      "offset": 0,
      "max": params.max + params.max / 10
    }
  };

  bars.tooltip = { "mouse": 2 };

  return bars;
}

function buildBarChartElements(params, labels) {
  var elements = [],
      treatedElements = [],
      pItemsLength = params.items.length,
      max = 0;

  for (var i=0 ; i<pItemsLength ; i++) {
    var items = params.items[i];
    if (items.totalResults > 0) {
      for (var j = 0, jj = items.totalResults; j < jj; j++) {
        var target = items.items[j].target,
            count = items.items[j].count;

        if (treatedElements[target] == undefined) {
          treatedElements[target] = [];
          treatedElements[target][i] = count;
          if (treatedElements[target][i] > max) {
            max = treatedElements[target][i];
          }
        } else {
          treatedElements[target][i] = count;
          if (treatedElements[target][i] > max) {
            max = treatedElements[target][i];
          }
        }
      }
    }
  }

  // Update "max" value
  var new_max = max,
      coef = 1;

  if (max == 0) {
    params.max = 9;
    params.step = 1;
  } else {
    while (max > 10) {
      max = max / 10;
      coef = coef * 10;
    }

    new_max = Math.ceil(max);
    // step
    params.step = (new_max < 5 && new_max > 1 && coef > 1) ? coef / 2 : coef;
    if (coef > 1) {
      if (max > 1 && max < 1.5) {
        params.max = new_max * coef * 0.75;
      } else if (max > 2 && max < 2.5) {
        params.max = Math.round(new_max * coef * (5 / 6));
      } else {
        params.max = new_max * coef;
      }
    } else {
      params.max = new_max;
      params.step = coef;
    }
  }

  for (key in treatedElements) {
    var values = [],
        value_obj = {},
        label = getMessage(key, "graph.label.");

    for (var j = 0; j < pItemsLength; j++) {
      if (!treatedElements[key][j]) {
        treatedElements[key][j] = 0;
      }
    }

    for (var i = 0, ii = treatedElements[key].length; i < ii; i++) {
      value_obj = {};
      value_obj.top = treatedElements[key][i];
      value_obj.tip = label + " : #val#";
      value_obj.tip += "\n" + labels[i];
      values.push(value_obj);
    }

    elements.push({
      "type": "bar_glass",
      "alpha": 0.75,
      "colour": barChartColors[key],
      "text": label,
      "font-size": 10,
      "values": values
    });
  }

  return elements;
}


function buildXAxisLabels(params) {
  var steps = params.totalResults >= 30 ? Math.round(params.totalResults / 15) : 1;
  var labelConfiguration = {
    "labels": buildBarChartXLabels(params),
    "steps": steps
  }
  addRotation(labelConfiguration, params);

  return labelConfiguration;
}


function buildHBarChart(params) {
  var y_labels = [],
      max = 0;

  if (params.items) {
    max = params.items[0].popularity + 1;
  }

  var bars = {
    "title": {
      "text": getMessage(params.additionalsParams.type, "graph.label.", (params.items.length == 1) ? '' : params.items.length),
      "style": "{font-size: 16px; color:#515D6B; font-family: Arial,sans-serif; font-weight: bold; text-align: center;}"
    },
    "bg_colour": "#FFFFFF",
    "elements": buildHBarChartElements(params, y_labels),
    "x_axis": {
      "colour": gridColors["x-axis"],
      "grid-colour": gridColors["x-grid"],
      "offset": false,
      "max": max,
      "steps": (max >= 15) ? Math.round(max / 10) : 1
    },
    "y_axis": {
      "colour": gridColors["y-axis"],
      "grid-colour": gridColors["y-grid"],
      "offset": true,
      "labels": y_labels
    },
    "tooltip": {
      "mouse": 2
    }
  };

  return bars;
}

function buildHBarChartElements(params, labels) {
  var crop = function (s) {
    if (s.length > 25) { s = s.substr(0, 25) + " ..."; }
    return s;
  };

  var elements = null,
      values = [],
      urlTemplate = params.additionalsParams.urlTemplate;

  for (var i=0, ii=params.items.length ; i<ii ; i++) {
    var item = params.items[i];
    var value_obj = {
      tip: item.displayName + " : #val#" + ((item.siteTitle) ? "\n" + getMessage("label.menu.site") + item.siteTitle : ''),
      right: item.popularity,
      left: 0,
      colour: i ? barChartColors["less-popular"] : barChartColors["most-popular"]
    };

    item.url = YAHOO.lang.substitute(urlTemplate[item.siteComponent] || "", {
      site: item.site,
      nodeRef: item.nodeRef,
      id: encodeURIComponent(item.name)
    });

    value_obj["on-click"] = "displayNodeDetailsPopup('" + escape(YAHOO.lang.JSON.stringify(item)) + "')";

    values.push(value_obj);
    labels.push(crop(params.items[ii - 1 - i].displayName));
  }

  elements = [{
    "type": "hbar",
    "colour": "#EC9304",
    "text": "",
    "font-size": 10,
    "values": values
  }];

  return elements;
}

function displayNodeDetailsPopup(itemStr) {
  var item = YAHOO.lang.JSON.parse(unescape(itemStr));

  // TODO: make something cleaner?
  var body = '<div class="node-details-popup">';
  body += '<p><label>' + getMessage("label.popup.filename") + '</label>' + item.displayName + '</p>';
  body += (item.siteTitle) ? '<p><label>' + getMessage("label.popup.type") + '</label>' + getMessage("site.component." + item.siteComponent) + '</p>' : '';
  body += (item.siteTitle) ? '<p><label>' + getMessage("label.menu.site") + '</label>' + item.siteTitle + '</p>' : '';
  body += '<p><label>' + getMessage("label.popup.hits") + '</label>' + item.popularity + '</p>';
  body += '</div>';

  Alfresco.util.PopupManager.displayPrompt({
    title: item.displayName,
    text: body,
    close: true,
    noEscape: true,
    buttons: [{
      text: getMessage("button.go-to-node-page"),
      handler:{
        fn: function(e, param) {
          window.open(param.url);
          this.destroy();
        },
        obj: item
      }
    }, {
      text: getMessage("button.cancel"),
      handler: function () {
        this.destroy();
      },
      isDefault: true
    }]
  });
}


function getMessage(messageId, prefix) {
  var msg = (prefix) ? prefix + messageId : messageId;
  var res = Alfresco.util.message.call(null, msg, "AtolStatistics.GlobalUsage", Array.prototype.slice.call(arguments).slice(2));
  res = (res.search("graph.label") == 0) ? messageId : res;
  return res;
}