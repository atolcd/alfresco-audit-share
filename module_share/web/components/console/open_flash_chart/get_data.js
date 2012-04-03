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

function buildTitle(params) {
  var title = getMessage(params.additionalsParams.type, "graph.title.");
  title += buildDateTitle(params);
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
      "colour": gridColors["x-axis"],
      "grid-colour": gridColors["x-grid"],
      "labels": x_labels
    },

    "y_axis": {
      "steps": params.step,
      "colour": gridColors["y-axis"],
      "grid-colour": gridColors["y-grid"],
      "offset": 0,
      "max": params.max + params.max / 10 //Petite marge
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
    //Pas
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
      "text": getMessage(params.additionalsParams.type, "graph.label."),
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
      if (s.length > 25) {
        s = s.substr(0, 25) + " ...";
      }
      return s;
    };
  var elements = null,
    values = [],
    value_obj, item, module = params.additionalsParams.module,
    urlTemplate = params.additionalsParams.urlTemplate;

  pItems = params.items, pItemsLength = pItems.length, i = 0;
  for (; i < pItemsLength; i++) {
    item = pItems[i];
    value_obj = {};
    value_obj.tip = item.displayName + " : #val#";
    value_obj.right = item.popularity;
    value_obj.left = 0;
    value_obj.colour = i ? barChartColors["less-popular"] : barChartColors["most-popular"];
    if (module == "document") {
      value_obj["on-click"] = YAHOO.lang.substitute(urlTemplate, {
        site: item.site,
        nodeRef: item.nodeRef
      });
    } else if (module == "wiki" || module == "blog" || module == "discussions") {
      value_obj["on-click"] = YAHOO.lang.substitute(urlTemplate, {
        site: item.site,
        id: item.name
      });
    }
    values.push(value_obj);
    labels.push(crop(pItems[pItemsLength - 1 - i].displayName));
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