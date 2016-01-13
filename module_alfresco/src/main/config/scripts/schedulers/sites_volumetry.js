/*
 * Copyright (C) 2013 Atol Conseils et Développements.
 * http://www.atolcd.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

try {
  // Find all sites
  var sitesNode = search.luceneSearch('+PATH:"/app:company_home/st:sites/."');
  if (sitesNode && sitesNode.length == 1) {
    sitesNode = sitesNode[0];
    var success;
    for (var i=0, ii=sitesNode.children.length ; i<ii ; i++) {
      var siteNode = sitesNode.children[i];
      if (siteNode.isSubType("st:site")) {
        var siteShortName = siteNode.name,
            site = siteService.getSite(siteShortName);
        if (site) {
          var documentLibrary = site.getContainer("documentLibrary");
          if (documentLibrary) {
            var volumetry = calculateVolumetry(documentLibrary);

            // Insert data into database
            success = sharestats.insertVolumetry(siteShortName, volumetry.siteSize, volumetry.foldersCount, volumetry.documentsCount, new Date().getTime());
            if (success){
              logger.log("Site: '" + siteShortName + "', documents count: " + volumetry.documentsCount + ", folders count: " + volumetry.foldersCount + ", volumetry: " + Math.round(volumetry.siteSize / 1024 / 1024) + " MB.");
            } else {
              logger.log("An error occurred while inserting data from '" + siteShortName + "' site.");
            }
         }
          else {
            logger.log("Document Library does not exist into the '" + siteShortName + "' site.");
          }
        }
        else {
          logger.log("Cannot find site: " + siteShortName);
        }
      }
    }
  }
  else {
    logger.log("'Sites' folder cannot be find");
  }
}
catch(e) {
  logger.log("An error occurred while calculating sites volumetry.");
  logger.log(e);
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
    logger.log("Wrong parameter: the node must be a folder.");
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