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

function padzero (n) {
  return n < 10 ? '0' + n.toString() : n.toString();
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

// Anciennes couleurs
//"#0077BF" => Bleu - Lectures
//"#7CBC28" => Vert - Créations
//"#EC9304" => Orange - Updates
//"#EE1C2F" => Rouge - Suppressions
var red = "#EE1C2F",
  blue = "#19ABEA",lightBlue = "#1B9EFC",darkBlue = "#1B5AF9",
  green = "#7CBC28", darkGreen = "#0A9200",
  orange = "#FF9900", lightOrange = "#FFC600", darkOrange = "#FF692B",
  gray = "#C1C1C1", mediumGray = "#DFDFDF";

// Couleurs utilisés par les graphiques
var barChartColors = [],
  gridColors = [];

//Blog
barChartColors["blog.postview"] = blue;
barChartColors["blog.blog-create"] = darkGreen;
barChartColors["blog.blog-delete"] = red;
barChartColors["blog.blog-update"] = orange;

//Espace document
barChartColors["document.details"] = blue;
barChartColors["document.download"] = darkBlue;
barChartColors["document.create"] = green;
barChartColors["document.file-added"] = darkGreen;
barChartColors["document.file-deleted"] = red;
barChartColors["document.file-updated"] = darkOrange;
barChartColors["document.inline-edit"] = lightOrange;
barChartColors["document.update"] = orange;

//Wiki
barChartColors["wiki.page"] = blue;
barChartColors["wiki.create-post"] = darkGreen;
barChartColors["wiki.delete-post"] = red;
barChartColors["wiki.update-post"] = orange;

//Discussions
barChartColors["discussions.topicview"] = blue;
barChartColors["discussions.discussions-create"] = darkGreen;
barChartColors["discussions.discussions-deleted"] = red;
barChartColors["discussions.discussions-update"] = orange;


barChartColors["volumetry"] = blue;
barChartColors["users"] = blue;
barChartColors["most-popular"] = red;
barChartColors["less-popular"] = blue;

//Grilles
gridColors["x-axis"] = gray;
gridColors["y-axis"] = gray;
gridColors["x-grid"] = mediumGray;
gridColors["y-grid"] = mediumGray;