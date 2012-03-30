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
