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
      "stroke": 3,
      "tick_height": 10,
      "colour": "#5fab34",
      "grid-colour": "#c0f0b0",
      "labels": x_labels
    },

    "y_axis": {
      "stroke": 3,
      "steps": Math.floor(params.max / 10),
      "tick_length": 3,
      "colour": "#5fab34",
      "grid-colour": "#ddf1d1",
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

  var type = "bar_glass",
    color = "#0077BF"; // "#EC9304", "#7CBC28", "#EE1C2F"];

  elements.push({
    "type": type,
    "colour": color,
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

function buildBarChartXLabels(params) {
  var labels = [],
    timeType = params.currentFilter,
    slicedDates = params.additionalsParams.tsString.split(",");
  var padzero = function (n) {
      return n < 10 ? '0' + n.toString() : n.toString();
    };

  switch (timeType) {
  case "years":
    for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
      labels[i] = getMonth((new Date(parseInt(slicedDates[i], 10)).getMonth()).toString());
    }
    break;
  case "months":
    for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
      var d = new Date(parseInt(slicedDates[i], 10));
      labels[i] = padzero(d.getDate()) + "/" + padzero(d.getMonth() + 1);
    }
    break;
  case "weeks":
    for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
      var d = new Date(parseInt(slicedDates[i], 10));
      labels[i] = getDay(d.getDay()) + " " + padzero(d.getDate()) + "/" + padzero(d.getMonth() + 1);
    }
    break;
  case "days":
    for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
      var d = new Date(parseInt(slicedDates[i], 10));
      labels[i] = padzero(d.getHours()) + "h00";
    }
    break;
  }
  return labels;
}

function buildDateTitle(params) {
  var title = "",
    timeType = params.currentFilter,
    slicedDates = params.additionalsParams.tsString.split(","),
    from, to;

  var padzero = function (n) {
      return n < 10 ? '0' + n.toString() : n.toString();
    };

  from = new Date(parseInt(slicedDates[0], 10));

  switch (timeType) {
  case "years":
    title = getMessage(timeType, "graph.title.date.", from.getFullYear());
    break;
  case "months":
    title = getMessage(timeType, "graph.title.date.", getMonth(from.getMonth()), from.getFullYear());
    break;
  case "weeks":
    title = getMessage(timeType, "graph.title.date.", from.getWeek(), from.getFullYear());
    break;
  case "days":
    title = getMessage(timeType, "graph.title.date.", padzero(from.getDate()), padzero(from.getMonth() + 1), from.getFullYear());
    break;
  }
  return title;
}


/**
 * Retourne la traduction d'un mois
 * @method getMonth
 * @param integer month
 */

function getMonth(month) {
  return getMessage(month, "label.month.");
}

function getDay(day) {
  return getMessage(day, "label.day.");
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