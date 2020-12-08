# Change Log
Tous les changements notables de ce projet sont document�s dans ce fichier.
SGBD support�s :
- MySQL
- PostgreSQL
- Oracle
- SQL Server
- DB2


## [Unreleased]

## [1.6.1] - 2020-12-08
### Fixed
- Correction probl�me de compilation

## [1.6.0] - 2020-06-03
### Changed
- Compatibilit� Alfresco 6.1.X
- Modification de l'expression CRON du calcul de volum�trie (tous les jours � 06:05:04)
- Compatibilit� Alfresco 6.2.X


## [1.5.9] - 2018-11-20
### Fixed
- Correctifs mineurs


## [1.5.8] - 2018-08-07
### Fixed
- Correction de l'affichage des graphiques de volum�trie avec des donn�es qui ont un format diff�rent (Ko / Mo / Go / To)
- Correction de l'affichage du graphique de volum�trie en mode "courbe"
- Correction du retour JSON du webscript "select-audits"


## [1.5.7] - 2018-04-13
### Changed
- Utilisation de "web fragments" (le fichier web.xml n'est plus �cras� par le module) (https://github.com/atolcd/alfresco-audit-share/issues/14)


## [1.5.6] - 2018-03-29
### Changed
- Optimisation : utilisation de caches pour la v�rification d'appartenance aux groupes


## [1.5.5] - 2018-03-09
### Added
- Enrichissement API Javascript

### Changed
- Modification de l'expression CRON du calcul de volum�trie (toutes les 4 heures)
- Compatibilit� Alfresco 5.2.x

### Fixed
- Correction "Erreur 414" (Request-URI Too Large) lorsqu'il y a beaucoup de sites cr��s
- Correction bug sur le calcul de la volum�trie
- Correction NPE si le referer est null (https://github.com/atolcd/alfresco-audit-share/issues/4)
- Corrections vuln�rabilit�s/bugs mineurs


## [1.5.4] - 2017-03-15
### Added
- Filtrage des graphiques par typologie des documents dans les sites
- Filtrage des connexions utilisateurs en fonction d'un ou plusieurs groupes
- Affichage de la version du module dans les interfaces d'AuditShare

### Changed
- Gestion des valeurs nulles dans les graphiques de volum�trie

### Fixed
- Correction de l'affichage par site de la volum�trie (option "Afficher les r�sultats par site")


## [1.5.3] - 2017-02-09
### Fixed
- Compatibilit� Oracle


## [1.5.2] - 2016-11-23
### Added
- Export CSV des documents les plus lus/modifi�s
- Ajout d'un index sur la table de volum�trie
- Ajout de librairies JS pour le rendu des graphes (D3.js / C3.js)
- Ajout de librairies JS pour l'export sous forme d'image (canvg.js / rgbcolor.js / StackBlur.js)

### Changed
- Optimisation du calcul de la volum�trie (traitement Java qui utilise une requ�te CMIS)
- Modification des requ�tes de r�cup�ration et d'insertion des donn�s de volum�trie

### Removed
- Suppression d'OpenFlashChart (*.js / *.swf)
- Suppression compatibilit� IE8 (affichage d'une popup)


## [1.5.1] - 2016-07-27
### Added
- Compatibilit� Alfresco 5.1.x
- Compatibilit� DB2

### Fixed
- Ajout de l'alias du module Alfresco manquant (42X)

### Changed
- Modification du script de calcul de la volum�trie des sites (utilisation de "selectNodes")


## [1.5.0] - 2016-01-13
### Added
- Compatibilit� Alfresco 5.0.x

### Changed
- Mavenisation des modules

### Fixed
- Correction authentification SSO en NTLM
- Correction du script de calcul de la volum�trie des sites (utilisation de la fonction "isSubType")
- Suppression de l'onglet "Statistiques" sur la page de recherche

### Removed
- Exclusion de nombreuses d�pendances dans le module Share
- Suppression des impressions d'�cran obsol�tes


The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).
