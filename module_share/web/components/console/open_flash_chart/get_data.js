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

/**
 * Retourn une couleur aléatoirement
 * @method get_random_color
 */

function get_random_color() {
  var letters = '0123456789ABCDEF'.split('');
  var color = '#';
  for (var i = 0; i < 6; i++) {
    color += letters[Math.round(Math.random() * 15)];
  }
  return color;
}

function buildTitle(params) {
  var title = getMessage(params.additionalsParams.type, "graph.title."),
    timeType = params.currentFilter,
    slicedDates = params.additionalsParams.tsString.split(","),
    from, to;


  var padzero = function (n) {
      return n < 10 ? '0' + n.toString() : n.toString();
    };

  from = new Date(parseInt(slicedDates[0], 10));

  switch (timeType) {
  case "years":
    title += getMessage(timeType, "graph.title.date.", from.getFullYear());
    break;
  case "months":
    title += getMessage(timeType, "graph.title.date.", getMonth(from.getMonth()), from.getFullYear());
    break;
  case "weeks":
    title += getMessage(timeType, "graph.title.date.", from.getWeek(), from.getFullYear());
    break;
  case "days":
    title += getMessage(timeType, "graph.title.date.", padzero(from.getDate()), padzero(from.getMonth() + 1), from.getFullYear());
    break;
  }
  return title;
}

/**
 * @method buildBarChart
 * @param params JSON Parameters from query
 * @return JSON Bar Chart Data
 */

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
      "max": params.max + 2
    }
  };

  return bars;
}

function buildBarChartElements(params, labels) {
  var elements = [],
    pItems = params.items,
    pItemsLength = pItems.length,
    max = 0;
  //  isSiteRequest = (params.type.search("sites") == 0) ? true : false;

  var treatedElements = [];
  //Boucle sur les éléments par date
  for (var i = 0; i < pItemsLength; i++) {
    var items = pItems[i];
    if (items.totalResults > 0) {
      //Boucles sur les différents éléments d'une date précise
      for (var j = 0, jj = items.totalResults; j < jj; j++) {
        var target = items.items[j].target,
          count = items.items[j].count;

        //Test si l'élément a déjà été traité
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
  //Mise à jour du maximum
  params.max = max ? max : 8;

  var type = "bar_glass",
    color_list = ["#0077BF", "#EC9304", "#7CBC28", "#EE1C2F"],
    color_idx = 0;

  //Modifier values
  for (key in treatedElements) {
    var values = [],
      value_obj = {};
    // label = isSiteRequest ? getSiteTitle(key,params.currentSites) : getMessage(key, "graph.label.");
    label = getMessage(key, "graph.label.");

    //Vérification des valeurs non remplies
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
    // TODO : Stacked
    elements.push({
      "type": type,
      "colour": color_list[color_idx],
      "text": label,
      "font-size": 10,
      "values": values
    });
    color_idx++;
  }

  return elements;
}


function buildXAxisLabels(params) {
  var steps = params.totalResults >= 30 ? Math.round(params.totalResults / 15) : 1;
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


/**
 * Retourne le "joli" nom d'un site
 * @method getSiteTitle
 * @param shortName Identifiant du site
 */

function getSiteTitle(shortName, sites) {
  var res = shortName,
    i = 0,
    ii = sites.lenght,
    currentSite;
  for (; i < ii; i++) {
    var currentSite = sites[i];
    if (currentSite.name == shortName) {
      res = GLOBALS_sites[i].title;
      break;
    }
  }
  return res;
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
  var res = Alfresco.util.message.call(null, msg, "Alfresco.ConsoleAudit", Array.prototype.slice.call(arguments).slice(2));
  res = (res.search("graph.label") == 0) ? messageId : res;
  return res;
}



function buildHBarChart(params) {
  var y_labels = [],
    max = 0;
  if (params.items) {
    max = params.items[0].popularity + 1;
  }
  var bars = {
    "title": {
      "text": getMessage(params.additionalsParams.type, "graph.label."),
      "style": "{font-size: 16px; color:#515D6B; font-family: Arial,sans-serif; font-weight: bold; text-align: center;}"
    },
    "bg_colour": "#FFFFFF",
    "elements": buildHBarChartElements(params, y_labels),
    "x_axis": {
      "colour": "#5fab34",
      "grid-colour": "#c0f0b0",
      "offset": false,
      "max": max,
      "steps" : (max >= 15) ? Math.round(max / 10) : 1
    },
    "y_axis": {
      "colour": "#5fab34",
      "grid-colour": "#c0f0b0",
      "offset": true,
      "labels": y_labels
    },
    "tooltip": {
      "mouse": 1
    }
  };

  return bars;
}

function buildHBarChartElements(params, labels) {

  var elements = null,
    values = [],
    value_obj, item, module = params.additionalsParams.module,
    urlTemplate = params.additionalsParams.urlTemplate;

  pItems = params.items, pItemsLength = pItems.length, i = 0;
  for (; i < pItemsLength; i++) {
    item = pItems[i];
    value_obj = {};
    value_obj.tip = item.name + " : #val#";
    value_obj.right = item.popularity;
    value_obj.left = 0;
    value_obj.colour = i ? "#0077BF" : "#EE1C2F";
    if (module == "document") {
      value_obj["on-click"] = YAHOO.lang.substitute(urlTemplate, {
        site: item.site,
        nodeRef: item.nodeRef
      });;
    }
    values.push(value_obj);
    labels.push(pItems[pItemsLength - 1 - i].name);
  }
  elements = [{
    "type": "hbar",
    "colour": "#EC9304",
    "text": "",
    "font-size": 10,
    "values": values
  }];
  console.log(elements);
  return elements;
}