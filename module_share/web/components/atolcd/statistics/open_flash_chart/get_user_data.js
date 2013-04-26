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

function getUserFlashData(param) {
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

/**
 * @method buildBarChart
 * @param params JSON Parameters from query
 * @return JSON Bar Chart Data
 */
function buildChart(params) {
  params.max = 0;
  var x_labels = buildXAxisLabels(params);
  var bars = {
    "title": {
      "text": buildTitle(params),
      "style": "{font-size: 16px; color:#515D6B; font-family: Arial,sans-serif; font-weight: bold; text-align: center; margin-top: 5px;}"
    },

    "bg_colour": "#FFFFFF",

    "elements": buildChartElements(params, x_labels.labels),

    "x_axis": {
      "colour": gridColors["x-axis"],
      "grid-colour": gridColors["x-grid"],
      "labels": x_labels
    },

    "y_axis": {
      "steps": params.step,
      "colour": gridColors["y-grid"],
      "grid-colour": gridColors["y-grid"],
      "offset": 0,
      "max": params.max + params.max / 10 // Petite marge
    }
  };

  return bars;
}

function buildChartElements(params, labels) {
  var max = 0,
      values = [],
      label = getMessage("connection", "graph.label.");

  // Boucle sur les éléments par date
  for (var i=0, ii=params.values.length ; i<ii ; i++) {
    var value = params.values[i];

    var elt = {
      top: value,
      tip: label + " : " + value + "\n" + labels[i]
    };

    if (params.additionalsParams.chartType && params.additionalsParams.chartType == "line") {
      elt.type = "dot";
      elt.value = value;
    }

    values.push(elt);

    max = max > value ? max : value;
  }

  // Mise à jour du maximum
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
    // Pas
    params.step = (new_max < 5 && new_max > 1 && coef > 1) ? coef / 2 : coef;
    // Maximum trop importante pour les valeurs proche de 1x ou 2x.
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

  var element = {
    'type': "bar_glass",
    'alpha': 0.75,
    'font-size': 10,
    'colour': barChartColors["users"],
    'text': label,
    'values': values
  };

  if (params.additionalsParams.chartType && params.additionalsParams.chartType == "line") {
    element.type = "line";
    element.width = 2;
    element["dot-style"] = {
      "type": "dot",
      "dot-size": 3,
      "halo-size": 1,
      "colour": barChartColors["users"]
    };
  }

  return [element];
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

/**
 * Retourne la traduction du message donné. Peut être prefixé.
 * @method getMessage
 * @param messageId Identifiant du message à traduire
 * @prefix Optionnel - Préfixe du message
 */
function getMessage(messageId, prefix) {
  var msg = (prefix) ? prefix + messageId : messageId;
  var res = Alfresco.util.message.call(null, msg, "AtolStatistics.UserConnections", Array.prototype.slice.call(arguments).slice(2));
  res = (res.search("graph.label") == 0) ? messageId : res;
  return res;
}