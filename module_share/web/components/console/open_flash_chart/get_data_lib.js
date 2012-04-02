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
 * 
 */
/*  
// En vue d'une utilisation future, 
// Supprimer la variable globale qui sera passé par "params"
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

*/

/**
 * Construit les labels de l'axe des abscisses à partir des paramètres
 * @method buildBarChartXLabels
 * @param object params
 */
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
 * Construit un intitulé de date suivant les paramètres
 * @method buildDateTitle
 * @param object params
 */
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

/**
 * Retourne la traduction d'un jour
 * @method getDay
 * @param integer day
 */
function getDay(day) {
  return getMessage(day, "label.day.");
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