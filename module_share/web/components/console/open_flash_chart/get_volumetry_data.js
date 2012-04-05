function getVolumetryFlashData(param) {
  var params = YAHOO.lang.JSON.parse(unescape(param)),
    jsonChart = null;

  jsonChart = buildChart(params);

  return YAHOO.lang.JSON.stringify(jsonChart);
};

function buildTitle(params) {
  var title = "",
    site = params.additionalsParams.site;

  if (site) {
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
      "style": "{font-size: 16px; color:#515D6B; font-family: Arial,sans-serif; font-weight: bold; text-align: center; margin-bottom: 15px;}"
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
  } else {
    bars.elements = buildStackedBarChartElements(params, x_labels.labels);
  }

  bars["y_axis"] = {
    "steps": params.max / 10,
    "colour": gridColors["y-axis"],
    "grid-colour": gridColors["y-grid"],
    "offset": 0,
    "max": params.max
  };

  return bars;
}

function buildBarChartElements(params, labels) {
  var elements = [],
    pItems = params.values,
    pItemsLength = pItems.length,
    max = 0,
    values = [],
    label = getMessage("volumetry", "graph.label.");

  // Boucle sur les éléments par date
  for (var i = 0; i < pItemsLength; i++) {
    var item = pItems[i];
    item = roundNumber(item / (1024 * 1024), 2);
    value_obj = {};
    value_obj.top = item;
    value_obj.tip = label + " : " + item + " Mo"; // Voir pour un meilleur label ? Formattage de #val#?
    value_obj.tip += "\n" + labels[i];
    values.push(value_obj);

    max = max > item ? max : item;
  }
  // Mise à jour du maximum
  params.max = max ? roundMax(max) : 10;


  elements.push({
    "type": "bar_glass",
    "alpha": 0.75,
    "colour": barChartColors["volumetry"],
    "text": label,
    "font-size": 10,
    "values": values
  });
  return elements;
}

function buildStackedBarChartElements(params, labels) {
  var max = 0,
    values = [],
    label = getMessage("volumetry", "graph.label.");

  // Boucle sur les éléments par date
  for (var i=0, ii=params.stackedValues.length ; i < ii; i++) {
    var stackedValue = params.stackedValues[i]
        valueTab = [];

    for (var j=0, jj=stackedValue.length ; j<jj ; j++) {
      var item = roundNumber(stackedValue[j] / (1024 * 1024), 2),
          value_obj = {};

      if (item > 0) {
        // value_obj.tip = labels[i];
        value_obj.tip = "Site : " + params.sites[j];
        value_obj.tip += "\n" +label + " : " + item + " Mo";
        value_obj.tip += "\n\nTotal : #total# Mo";
      }
      else {
        value_obj.tip = "";
        // HACK : les tooltips se cumulent lors de l'affichage quand
        // toutes les valeurs sont vides
        if (params.values[i] == 0 && j == 0) {
           value_obj.tip = getMessage("label.no-data");
        }
      }

      value_obj.val = item;
      valueTab.push(value_obj);
    }

    values.push(valueTab);

    var total = roundNumber(params.values[i] / (1024 * 1024), 2);
    max = max > total ? max : total;
  }

  // Mise à jour du maximum
  params.max = max ? roundMax(max) : 10;

  var sites = [];
  for (var i=0, ii=params.sites.length ; i < ii; i++) {
    sites.push({
      "colour": barStackedChartColors.defaultColors[i%barStackedChartColors.defaultColors.length],
      "text": params.sites[i],
      "font-size": 10
    });
  }

  return [{
    "type": "bar_stack",
    "alpha": 0.9,
    "colours" : barStackedChartColors.defaultColors,
    "text": label,
    "font-size": 10,
    "values": values,
    "keys": sites
  }];
}

function buildXAxisLabels(params) {
  var steps = params.values.length >= 30 ? Math.round(params.values.length / 15) : 1;
  var labelConfiguration = {
    "labels": buildBarChartXLabels(params),
    "steps": steps
  }

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
  var res = Alfresco.util.message.call(null, msg, "Alfresco.ConsoleSitesVolumetry", Array.prototype.slice.call(arguments).slice(2));
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