function getUserFlashData(param) {
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

    "elements": buildBarChartElements(params, x_labels.labels),

    "x_axis": {
      "colour": gridColors["x-axis"],
      "grid-colour": gridColors["x-grid"],
      "labels": x_labels
    },

    "y_axis": {
      "steps": Math.floor(params.max / 10),
      "colour": gridColors["y-grid"],
      "grid-colour": gridColors["y-grid"],
      "offset": 0,
      "max": params.max + params.max / 10
    }
  };

  return bars;
}

function buildBarChartElements(params, labels) {
  var elements = [],
    pItems = params.values,
    pItemsLength = pItems.length,
    max = 0,
    values = [],
    label = getMessage("connection", "graph.label.");

  //Boucle sur les éléments par date
  for (var i = 0; i < pItemsLength; i++) {
    var item = pItems[i];
    value_obj = {};
    value_obj.top = item;
    value_obj.tip = label + " : " + item; // Voir pour un meilleur label ? Formattage de #val#?
    value_obj.tip += "\n" + labels[i];
    values.push(value_obj);

    max = max > item ? max : item;
  }
  //Mise à jour du maximum
  params.max = max ? max : 8;

  elements.push({
    "type": "bar_glass",
    "alpha": 0.75,
    "colour": barChartColors["users"],
    "text": label,
    "font-size": 10,
    "values": values
  });
  return elements;
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
  var res = Alfresco.util.message.call(null, msg, "Alfresco.ConsoleUserAudit", Array.prototype.slice.call(arguments).slice(2));
  res = (res.search("graph.label") == 0) ? messageId : res;
  return res;
}