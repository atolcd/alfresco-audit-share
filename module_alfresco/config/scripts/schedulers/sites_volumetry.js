try {
  // On parcourt tous les sites
  var sitesNode =  search.luceneSearch('+PATH:"/app:company_home/st:sites/."');
  if (sitesNode && sitesNode.length == 1) {
    sitesNode = sitesNode[0];

    for (var i=0, ii=sitesNode.children.length ; i<ii ; i++) {
      var siteNode = sitesNode.children[i];
      if (siteNode.typeShort == "st:site") {
        var siteShortName = siteNode.name,
            site = siteService.getSite(siteShortName);
        if (site) {
          var documentLibrary = site.getContainer("documentLibrary");
          if (documentLibrary) {
            // Calcul de la volumétrie
            // var volumetry = calculateVolumetry(documentLibrary);

            // TODO : Stocker en base
            // var timestamp = new Date().getTime();
            // sharestats.insert(timestamp, siteShortName, volumetry.foldersCount, volumetry.documentsCount, volumetry.siteSize);

            // logger.log("Le site '" + siteShortName + "' possède " + volumetry.documentsCount + " document(s), " + volumetry.foldersCount + " dossier(s) et une volumétrie de : " + Math.round(volumetry.siteSize / 1024 / 1024) + " Mo.");
          }
          else {
            logger.log("Le site " + siteShortName + " ne possède pas d'espace documentaire.");
          }
        }
        else {
          logger.log("Impossible de récupérer le site : " + siteShortName);
        }
      }
    }
  }
  else {
    logger.log("Impossible de trouver l'espace 'Sites'");
  }
}
catch(e) {
  throw e;
}



function calculateVolumetry(node) {
  var res = {
    "documentsCount": 0,
    "foldersCount": 0,
    "siteSize": 0
  };

  if (node.isContainer) {
    for (var i=0, ii=node.children.length ; i<ii ; i++) {
      calculateNodeSize(node.children[i], res);
    }
  }
  else {
    logger.log("Erreur paramètre : le noeud doit être un dossier.");
  }

  return res;
}

function calculateNodeSize(node, counters) {
  if (node.isContainer) {
    for (var i=0, ii=node.children.length ; i<ii ; i++) {
      calculateNodeSize(node.children[i], counters);
    }
    counters.foldersCount ++;
  }
  else {
    counters.documentsCount ++;
    counters.siteSize += getDocumentSize(node);
  }
}

function getDocumentSize(node) {
  if (node && node.properties.content) {
    return node.properties.content.size;
  }
  return 0;
}