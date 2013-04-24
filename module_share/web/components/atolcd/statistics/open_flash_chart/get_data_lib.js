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
        labels[i] = Alfresco.thirdparty.dateFormat(new Date(parseInt(slicedDates[i], 10)), AtolStatistics.dateFormatMasks.fullMonth); // default: mmmm
      }
      break;

    case "months":
      for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
        labels[i] = Alfresco.thirdparty.dateFormat(new Date(parseInt(slicedDates[i], 10)), AtolStatistics.dateFormatMasks.shortDay); // default: dd/mm
      }
      break;

    case "weeks":
      for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
        labels[i] = Alfresco.thirdparty.dateFormat(new Date(parseInt(slicedDates[i], 10)), AtolStatistics.dateFormatMasks.mediumDay); // default: dddd dd/mm
      }
      break;

    case "days":
      for (var i = 0, ii = slicedDates.length - 1; i < ii; i++) {
        var timestamp = parseInt(slicedDates[i], 10),
            h1 = Alfresco.thirdparty.dateFormat(new Date(timestamp), AtolStatistics.dateFormatMasks.shortHour), // default: HH'h'
            h2 = Alfresco.thirdparty.dateFormat(new Date(timestamp + (2 * 60 * 60 * 1000)), AtolStatistics.dateFormatMasks.shortHour); // + 2 hours

        labels[i] = h1 + " - " + h2;
      }
      break;
  }
  return labels;
}

/**
 * Configure la rotation des labels sur l'axe des X
 * @param o
 * @param params
 */
function addRotation(o, params){
  // Filtre par heures de la journée. Les labels de chevauchent sur les "petits" écrans.
  if (params.currentFilter == "days"){ o.rotate = -45; }
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
      from = new Date(parseInt(slicedDates[0], 10));

  switch (timeType) {
    case "years":
      title = getMessage(timeType, "graph.title.date.", from.getFullYear());
      break;

    case "months":
      var m = Alfresco.thirdparty.dateFormat(from, AtolStatistics.dateFormatMasks.fullMonth);
      title = getMessage(timeType, "graph.title.date.", m, from.getFullYear());
      break;

    case "weeks":
      title = getMessage(timeType, "graph.title.date.", from.getWeek(), from.getFullYear());
      break;

    case "days":
      title = getMessage(timeType, "graph.title.date.", Alfresco.thirdparty.dateFormat(from, AtolStatistics.dateFormatMasks.shortDate));
      break;
  }

  return title;
}

// Anciennes couleurs
// "#0077BF" => Bleu - Lectures
// "#7CBC28" => Vert - Créations
// "#EC9304" => Orange - Updates
// "#EE1C2F" => Rouge - Suppressions
var red = "#EE1C2F",
  blue = "#19ABEA",lightBlue = "#1B9EFC",darkBlue = "#1B5AF9",
  green = "#7CBC28", darkGreen = "#0A9200",
  orange = "#FF9900", lightOrange = "#FFC600", darkOrange = "#FF692B",
  gray = "#C1C1C1", mediumGray = "#DFDFDF";

// Couleurs utilisés par les graphiques
var barChartColors = [],
  gridColors = [];

// Blog
barChartColors["blog.postview"] = blue;
barChartColors["blog.blog-create"] = darkGreen;
barChartColors["blog.blog-delete"] = red;
barChartColors["blog.blog-update"] = orange;

// Espace document
barChartColors["document.details"] = blue;
barChartColors["document.download"] = darkBlue;
barChartColors["document.create"] = green;
barChartColors["document.file-added"] = darkGreen;
barChartColors["document.file-deleted"] = red;
barChartColors["document.file-updated"] = darkOrange;
barChartColors["document.inline-edit"] = lightOrange;
barChartColors["document.update"] = orange;

// Wiki
barChartColors["wiki.page"] = blue;
barChartColors["wiki.create-post"] = darkGreen;
barChartColors["wiki.delete-post"] = red;
barChartColors["wiki.update-post"] = orange;

// Discussions
barChartColors["discussions.topicview"] = blue;
barChartColors["discussions.discussions-create"] = darkGreen;
barChartColors["discussions.discussions-deleted"] = red;
barChartColors["discussions.discussions-update"] = orange;


barChartColors["volumetry"] = blue;
barChartColors["users"] = blue;
barChartColors["most-popular"] = red;
barChartColors["less-popular"] = blue;

// Grilles
gridColors["x-axis"] = gray;
gridColors["y-axis"] = gray;
gridColors["x-grid"] = mediumGray;
gridColors["y-grid"] = mediumGray;


var barStackedChartColors = {};
barStackedChartColors.defaultColors = ["#FF6201", "#75C7BB", "#D6191F", "#7CA900", "#373121", "#EB9B00", "#58C3F0", "#7D7B6A", "#EA2673", "#BCA8D0",
                                       "#8D625B", "#FFD370", "#009285", "#1B9EFC", "#0A9200", "#FF9900", "#C6E56F", "#755A04", "#80001B", "#291309"];