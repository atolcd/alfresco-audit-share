function getFlashData(param) {
  var params = YAHOO.lang.JSON.parse(unescape(param)),
    jsonChart = null;
  //Plus rapide que recherche de "motif" by-week ?
  var size = params.type.split("_").length;
  // if(params.type === "by-month" || params.type === "by-week" || params.type === "by-day") {
  if (size > 2) {
    jsonChart = buildBarChart(params);
    // jsonChart = buildLineChart(params);
  } else {
    jsonChart = buildPieChart(params);
  }
  return YAHOO.lang.JSON.stringify(jsonChart);
};

//Faire une colorisation Custom ?


function buildColorArray(param) {
  //"#46B549" vert foncé
  var color_list = ["#0077BF", "#EC9304", "#7CBC28", "#EE1C2F"];
  var color_array = [];
  //OFC applique les couleurs les unes après les autres. Possibilité de
  // redondance entre la première et la dernière ....
  for (var i = 0, ii = param.totalResults; i < ii; i++) {
    if ((i == param.totalResults - 1) && (i % color_list.length == 0)) {
      color_array.push(color_list[1]);
    } else {
      color_array.push(color_list[i % color_list.length]);
    }
  }
  return color_array;
}

function buildTitle(param) {
  var title = null;
  if (param.totalResults == 0 || param.totalResults == undefined) {
    title = "Aucun audit n'a été trouvé";
  } else {
    title = getMessage(param.type, "graph.title.");
  }
  return title;
}

/**
 * @method buildPie
 * @param params JSON Parameters from query
 * @return JSON Pie Chart Data
 */

function buildPieChart(params) {
  var pie = {
    "title": {
      "text": buildTitle(params),
      "style": "{font-size: 16px; color:#526A53; font-family: Arial,sans-serif; font-weight: bold; text-align: center;}"
    },
    "bg_colour": "#FFFFFF",
    "elements": [{
      "type": "pie",
      "colours": buildColorArray(params),
      "alpha": 0.6,
      "gradient-fill": true,
      "border": 2,
      //"start-angle": 45,
      "values": buildPieChartArray(params)
    }]
  }
  return pie;
}

function buildPieChartArray(param) {
  var pie_array = [],
    isSiteRequest = (param.type.search("sites") == 0) ? true : false,
    label = "";
  for (var i = 0, total = param.totalResults; i < total; i++) {
    label = isSiteRequest ? getSiteTitle(param.items[i].target) : getMessage(param.items[i].target, "graph.label.");
    pie_array[i] = {
      "value": param.items[i].count,
      "tip": label + " : " + param.items[i].count,
      "label": label
    };
  }
  return pie_array;
}

function buildLineChart(params) {
  max = 0;
  var lines = {
    "title": {
      "text": buildTitle(params),
      "style": "{font-size: 16px; color:#526A53; font-family: Arial,sans-serif; font-weight: bold; text-align: center;}"
    },

    "y_legend": {
      "text": buildText(params),
      "style": "{color: #736AFF; font-size: 12px;}"
    },
    "bg_colour": "#FFFFFF",

    "elements": buildLineChartElements(params),

    "x_axis": {
      "stroke": 3,
      "tick_height": 10,
      "colour": "#5fab34",
      "grid-colour": "#c0f0b0",
      "labels": buildXAxisLabels(params)
    },

    "y_axis": {
      "stroke": 3,
      "steps": Math.floor(max / 10),
      "tick_length": 3,
      "colour": "#5fab34",
      "grid-colour": "#ddf1d1",
      "offset": 0,
      "max": max + 2
    }
  };

  return lines;
}

function buildLineChartElements(params) {
  var elements = [],
    pItems = params.items,
    isSiteRequest = (params.type.search("sites") == 0) ? true : false;

  var treatedElements = [];
  //Boucle sur les éléments par date
  for (var i = 0, ii = pItems.length; i < ii; i++) {
    var items = pItems[i];
    if (items.totalResults > 0) {
      //Boucles sur les différents éléments d'une date précise
      for (var j = 0, jj = items.totalResults; j < jj; j++) {
        var target = (items.items[j].target == "") ? "view" : items.items[j].target;
        var count = items.items[j].count;

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
  //Modifier values
  for (key in treatedElements) {
    var values = [],
      label = isSiteRequest ? getSiteTitle(key) : getMessage(key, "graph.label.");

    for (var i = 0; i < 7; i++) {
      values[i] = 0;
    }
    for (var i = 0, ii = treatedElements[key].length; i < ii; i++) {
      if (treatedElements[key][i] == undefined) {
        // values.push(0);//null ?
        values[i] = 0; //null ?
      } else {
        // values.push(
        // treatedElements[key][i]
        // {
        // "top": treatedElements[key][i],
        // "tip": label + " : #val#"
        // }
        // );
        values[i] = treatedElements[key][i]; //null ?
      }
    }
    elements.push({
      "type": "line",
      //bar_filled
      "alpha": 0.7,
      "colour": get_random_color(),
      "text": label,
      "tip": label + " : #val#",
      "font-size": 10,
      "values": values
    });
  }

  return elements;
}

/**
 * @method buildBarChart
 * @param params JSON Parameters from query
 * @return JSON Bar Chart Data
 */

function buildBarChart(params) {
  max = 0;
  var bars = {
    "title": {
      "text": buildTitle(params),
      "style": "{font-size: 16px; color:#526A53; font-family: Arial,sans-serif; font-weight: bold; text-align: center;}"
    },

    "y_legend": {
      "text": buildText(params),
      "style": "{color: #736AFF; font-size: 12px;}"
    },
    "bg_colour": "#FFFFFF",

    "elements": buildBarChartElements(params),

    "x_axis": {
      "stroke": 3,
      "tick_height": 10,
      "colour": "#5fab34",
      "grid-colour": "#c0f0b0",
      "labels": buildXAxisLabels(params)
    },

    "y_axis": {
      "stroke": 3,
      "steps": Math.floor(max / 10),
      "tick_length": 3,
      "colour": "#5fab34",
      "grid-colour": "#ddf1d1",
      "offset": 0,
      "max": max + 2
    }
  };

  return bars;
}

function buildBarChartElements(params) {
  var elements = [],
    pItems = params.items,
    isSiteRequest = (params.type.search("sites") == 0) ? true : false;

  var treatedElements = [];
  //Boucle sur les éléments par date
  for (var i = 0, ii = pItems.length; i < ii; i++) {
    var items = pItems[i];
    if (items.totalResults > 0) {
      //Boucles sur les différents éléments d'une date précise
      for (var j = 0, jj = items.totalResults; j < jj; j++) {
        var target = (items.items[j].target == "") ? "view" : items.items[j].target;
        var count = items.items[j].count;

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

  //Modifier values
  for (key in treatedElements) {
    var values = [],
      label = isSiteRequest ? getSiteTitle(key) : getMessage(key, "graph.label.");
    for (var i = 0, ii = treatedElements[key].length; i < ii; i++) {
      if (treatedElements[key][i] == undefined) {
        values.push(undefined); //null ?
      } else {
        values.push({
          "top": treatedElements[key][i],
          "tip": label + " : #val#"
        });
      }
    }
    elements.push({
      "type": "bar_glass",
      //bar_filled
      "alpha": 0.7,
      "colour": get_random_color(),
      "text": label,
      "font-size": 10,
      "values": values
    });
  }

  return elements;
}


function buildXAxisLabels(params) {
  var steps = params.totalResults >= 20 ? Math.round(params.totalResults / 10) : 1;
  var rotationAngle = params.totalResults > 5 ? "-45" : "0";
  var labelConfiguration = {
    "labels": buildBarChartXLabels(params),
    "steps": steps,
    "rotate": rotationAngle
  }

  return labelConfiguration;
}

function buildBarChartXLabels(params) {
  var labels = [];
  //On récupère l'information de découpage dans le type : Month / week / day
  var timeType = params.type.split("_").reverse()[0];
  if (timeType == "month") {
    var slicedDates = params.slicedDates.split(",");
    for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
      labels[i] = getMonth((new Date(parseInt(slicedDates[i], 10)).getMonth() + 1).toString());
    }
  } else if (timeType == "day") {
    var slicedDates = params.slicedDates.split(",");
    for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
      var d = new Date(parseInt(slicedDates[i], 10));
      labels[i] = d.getDate().toString() + "/" + (d.getMonth() + 1).toString();
    }
  } else if (timeType == "week") {
    var slicedDates = params.slicedDates.split(",");
    for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
      var from = new Date(parseInt(slicedDates[i], 10));
      var to = new Date(parseInt(slicedDates[i + 1], 10));

      //Suppression du dimanche
      to.setDate(to.getDate() - 2);

      from = "Du lundi " + from.getDate().toString() + "/" + (from.getMonth() + 1).toString() + "/" + from.getFullYear().toString();
      to = "<br> au samedi " + to.getDate().toString() + "/" + (to.getMonth() + 1).toString() + "/" + to.getFullYear().toString();
      labels.push(from + to);
    }
  }
  return labels;
}

function buildText(params) {
  var res = "",
    type = params.type;
  if (type.search("view") >= 0) {
    res = getMessage("graph.ylabel.view");
  } else if (type.search("comment") >= 0) {
    res = getMessage("graph.ylabel.comment");
  } else if (type.search("file") >= 0) {
    res = getMessage("graph.ylabel.file");
  } else if (type.search("action") >= 0) {
    res = getMessage("graph.ylabel.action");
  }

  return res;
}

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
/**
 * Retourne le "joli" nom d'un site
 * @method getSiteTitle
 * @param shortName Identifiant du site
 */

function getSiteTitle(shortName) {
  //Variable de retour. ShortName par défaut.
  var res = shortName;
  for (var i = 0, ii = GLOBALS_sites.length; i < ii; i++) {
    var tmpName = GLOBALS_sites[i].name;
    if (tmpName == shortName) {
      res = GLOBALS_sites[i].title;
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

/**
 * Retourne la traduction du message donné. Peut être prefixé.
 * @method getMessage
 * @param messageId Identifiant du message à traduire
 * @prefix Optionnel - Préfixe du message
 */

function getMessage(messageId, prefix) {
  var msg = ( !! prefix) ? prefix + messageId : messageId;
  var res = Alfresco.util.message.call(null, msg, "Alfresco.ConsoleAudit", Array.prototype.slice.call(arguments).slice(1));
  res = (res.search("graph.label") == 0) ? messageId : res;
  return res;
}