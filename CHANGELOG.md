# Change Log
Tous les changements notables de ce projet sont documentés dans ce fichier.
SGBD supportés :
- MySQL
- PostgreSQL
- Oracle
- SQL Server
- DB2


## [1.5.3] - 2017-02-09
### Fixed
- Compatibilité Oracle


## [1.5.2] - 2016-11-23
### Added
- Export CSV des documents les plus lus/modifiés
- Ajout d'un index sur la table de volumétrie
- Ajout de librairies JS pour le rendu des graphes (D3.js / C3.js)
- Ajout de librairies JS pour l'export sous forme d'image (canvg.js / rgbcolor.js / StackBlur.js)

### Changed
- Optimisation du calcul de la volumétrie (traitement Java qui utilise une requête CMIS)
- Modification des requêtes de récupération et d'insertion des donnés de volumétrie

### Removed
- Suppression d'OpenFlashChart (*.js / *.swf)
- Suppression compatibilité IE8 (affichage d'une popup)


## [1.5.1] - 2016-07-27
### Added
- Compatibilité Alfresco 5.1.x
- Compatibilité DB2

### Fixed
- Ajout de l'alias du module Alfresco manquant (42X)

### Changed
- Modification du script de calcul de la volumétrie des sites (utilisation de "selectNodes")


## [1.5.0] - 2016-01-13
### Added
- Compatibilité Alfresco 5.0.x

### Changed
- Mavenisation des modules

### Fixed
- Correction authentification SSO en NTLM
- Correction du script de calcul de la volumétrie des sites (utilisation de la fonction "isSubType")
- Suppression de l'onglet "Statistiques" sur la page de recherche

### Removed
- Exclusion de nombreuses dépendances dans le module Share
- Suppression des impressions d'écran obsolètes


The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).