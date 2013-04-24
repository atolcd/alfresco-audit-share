function getVolumetryFlashData(param) {
  var params = YAHOO.lang.JSON.parse(unescape(param)),
    jsonChart = null;

  jsonChart = buildChart(params);

  return YAHOO.lang.JSON.stringify(jsonChart);
};

function buildTitle(params) {
  var title = "",
      site = params.additionalsParams.site;

  if (site && site.indexOf(',') == -1) {
    title = getMessage("site", "graph.title.", site);
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
      "style": "{font-size: 16px; color:#515D6B; font-family: Arial,sans-serif; font-weight: bold; text-align: center; margin-top: 5px; margin-bottom: 10px;}"
    },

    "bg_colour": "#FFFFFF",

    "x_axis": {
      "colour": gridColors["x-axis"],
      "grid-colour": gridColors["x-grid"],
      "labels": x_labels
    }
  };

  if (params.additionalsParams.chartType == "vbar") {
    bars.elements = buildBarChartElements(params, x_labels.labels);
  } else if (params.additionalsParams.chartType == "line") {
    bars.elements = buildLineChartElements(params, x_labels.labels);
  } else if (params.additionalsParams.chartType == "lines") {
    bars.elements = buildLinesChartElements(params, x_labels.labels);
  } else {
    bars.elements = buildStackedBarChartElements(params, x_labels.labels);
    bars.tooltip = { "mouse": 2 };
  }

  bars["y_axis"] = {
    "steps": params.max / 10,
    "colour": gridColors["y-axis"],
    "grid-colour": gridColors["y-grid"],
    "offset": 0,
    "max": params.max
  };

  bars["y_legend"] = {
    "text": getMessage("volumetry", "graph.label.", getMessage("size.megabytes")),
    "style": "{font-size: 12px; color: #778877}"
  };

  return bars;
}

function buildBarChartElements(params, labels) {
  var max = 0,
      elements = [],
      values = [];

  // Boucle sur les éléments par date
  for (var i=0, ii=params.values.length ; i<ii ; i++) {
    var value = roundNumber(params.values[i] / (1024 * 1024), 2);

    var tip = labels[i] + "\n" + getMessage("volumetry", "graph.label.", getMessage("size.megabytes")) + " : " + value + " " + getMessage("size.megabytes");
    if (params.sites && params.sites.length == 1) {
      tip += "\n" + getMessage("label.menu.site") + " " + params.sites[0];
    }

    values.push({
      top: value,
      tip: tip
    });

    max = max > value ? max : value;
  }
  // Mise à jour du maximum
  params.max = max ? roundMax(max) : 10;

  elements.push({
    "type": "bar_glass",
    "alpha": 0.75,
    "colour": barChartColors["volumetry"],
    "font-size": 10,
    "values": values
  });

  return elements;
}

function buildStackedBarChartElements(params, labels) {
  var max = 0, values = [];

  // Boucle sur les éléments par date
  for (var i=0, ii=params.stackedValues.length ; i < ii; i++) {
    var stackedValue = params.stackedValues[i]
        valueTab = [];

    for (var j=0, jj=stackedValue.length ; j<jj ; j++) {
      var value = roundNumber(stackedValue[j] / (1024 * 1024), 2);
      var value_obj = {
        val: value
      };

      if (value > 0) {
        value_obj.tip = labels[i] + "\n\n";
        value_obj.tip += getMessage("volumetry", "graph.label.", getMessage("size.megabytes")) + " : " + value + " " + getMessage("size.megabytes") + "\n";
        value_obj.tip += getMessage("label.menu.site") + " " + params.sites[j] + "\n\n";
        value_obj.tip += getMessage("graph.label.global.volumetry") + " #total# Mo";
      }
      else {
        // value_obj.tip = "i=" + i + " - j=" + j;
        // HACK : les tooltips se cumulent lors de l'affichage quand toutes les valeurs sont vides
        if (params.values[i] == 0 && j == 0) {
          value_obj.tip = getMessage("label.graph.no-data");
        }
      }

      valueTab.push(value_obj);
    }

    values.push(valueTab);

    var total = roundNumber(params.values[i] / (1024 * 1024), 2);
    max = max > total ? max : total;
  }

  // Mise à jour du maximum
  params.max = max ? roundMax(max) : 10;


  return [{
    "type": "bar_stack",
    "alpha": 0.7,
    "colours" : barStackedChartColors.defaultColors,
    "font-size": 10,
    "values": values/*,
    "tip": "X label [#x_label#], Value [#val#]<br>Total [#total#]"*/
  }];
}

function buildLineChartElements(params, labels) {
  var max = 0,
      elements = [],
      values = [];

  // Boucle sur les éléments par date
  for (var i=0, ii=params.values.length ; i<ii ; i++) {
    var value = roundNumber(params.values[i] / (1024 * 1024), 2);

    var tip = labels[i] + "\n" + getMessage("volumetry", "graph.label.", getMessage("size.megabytes")) + " : " + value + " " + getMessage("size.megabytes");
    if (params.sites && params.sites.length == 1) {
      tip += "\n" + getMessage("label.menu.site") + " " + params.sites[0];
    }

    var value_obj = {
      type: "dot",
      value: value,
      tip: tip
    };

    values.push((value === 0) ? null : value_obj);

    max = max > value ? max : value;
  }
  // Mise à jour du maximum
  params.max = max ? roundMax(max) : 10;

  elements.push({
    "type": "line",
    "alpha": 0.75,
    "font-size": 10,
    "values": values,
    "dot-style": {
      "type": "dot",
      "dot-size": 3,
      "halo-size": 1,
      "colour": barChartColors["volumetry"]
    },
    "width": 2,
    "colour": barChartColors["volumetry"]
  });

  return elements;
}

function buildLinesChartElements(params, labels) {
  var max = 0, lines = [], linesValues = {};

  for (var i=0, ii=params.stackedValues.length ; i < ii; i++) {
    var periodValues = params.stackedValues[i];
    for (var j=0, jj=periodValues.length ; j<jj ; j++) {
      var dotValue = roundNumber(periodValues[j] / (1024 * 1024), 2);
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
        dot.tip += "\n" + getMessage("volumetry", "graph.label.", getMessage("size.megabytes")) + " : " + dotValue + getMessage("size.megabytes");
      } else {
        dot = null;
      }

      linesValues[j].values.push(dot);
    }

    var total = roundNumber(params.values[i] / (1024 * 1024), 2);
    max = max > total ? max : total;
  }

  var cpt = 0;
  for (var siteValues in linesValues) {
    var colour = barStackedChartColors.defaultColors[cpt%barStackedChartColors.defaultColors.length];
    lines.push({
      "type": "line",
      "values": linesValues[siteValues].values,
      "dot-style": {
        "type": "dot",
        "dot-size": 5,
        "halo-size": 1,
        "colour": colour
      },
      "width": 4,
      "colour": colour,
      "font-size": 10
    });

    cpt ++;
  }

  // Mise à jour du maximum
  params.max = max ? roundMax(max) : 10;

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

/**
 * Retourne la traduction du message donné. Peut être prefixé.
 * @method getMessage
 * @param messageId Identifiant du message à traduire
 * @prefix Optionnel - Préfixe du message
 */

function getMessage(messageId, prefix) {
  var msg = (prefix) ? prefix + messageId : messageId;
  var res = Alfresco.util.message.call(null, msg, "AtolStatistics.Volumetry", Array.prototype.slice.call(arguments).slice(2));
  res = (res.search("graph.label") == 0) ? messageId : res;
  return res;
}

/**
 * "Arrondi" la valeur max du graphique pour y mettre des valeurs rondes
 * @method roundMax
 * @param integer max
 */
function roundMax(max) {
  var new_max = max,
    coef = 1;

  while (new_max >= 10) {
    new_max = new_max / 10;
    coef = coef * 10;
  }

  new_max = new_max.toPrecision(2);

  if (new_max > 7.5) {
    new_max = 10;
    step = 1;
  } else if (new_max > 5) {
    new_max = 7.5;
  } else if (new_max > 2.5) {
    new_max = 5;
  } else {
    new_max = 2.5;
  }

  return new_max * coef;
}

/**
 * Arrondit le nombre number avec la précision digits après la virgule
 * @method roundNumber
 * @param number Nombre à arrondir
 * @param digits Nombre de chiffres après la virgule
 */
function roundNumber(number, digits) {
  var multiple = Math.pow(10, digits);
  var rndedNum = Math.round(number * multiple) / multiple;
  return rndedNum;
}