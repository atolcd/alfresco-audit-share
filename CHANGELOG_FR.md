# Change Log
Tous les changements notables de ce projet sont documentés dans ce fichier.
SGBD supportés :
- MySQL
- PostgreSQL
- Oracle
- SQL Server
- DB2

## [Unreleased]
### Added
- Traduction brésiliennes

## [2.2.0] - 2024-01-09
### Changed
- Migration SDK 4.6 et compatibilité Alfresco 7.4.1
- Passage à log4j2 - compatibilité ACS 7.4
- Mise à jour des modules "ootbee-support-tools" - compatibilité ACS 7.4
- Suppression des repo internes AtolCD

## [2.1.3] - 2023-04-14
### Changed
- Migration SDK 4.5 et compatibilité Alfresco 7.3.1

## [2.1.2] - 2023-02-22
### Changed
- Migration SDK 4.4 et compatibilité Alfresco 7.2.0
- Suppression des fichiers context.xml inutiles

### Fixed
- Correction dépendance à la version de jQuery

## [2.1.1] - 2022-05-10
### Fixed
- Correction des dialects Oracle et SQLServer

## [2.1.0] - 2022-02-01
### Changed
- Migration SDK 4.3 et compatibilité Alfresco 7.1.0
- Suppression box vagrant inutilisée

### Fixed
- Correction des dépendances pour les pom docker

## [2.0.0] - 2021-09-15
### Changed
- Migration SDK 4.2 et compatibilité Alfresco 7.0.0
- Migration SDK 4.1

## [1.6.1] - 2020-12-08
### Fixed
- Correction problème de compilation

## [1.6.0] - 2020-06-03
### Changed
- Compatibilité Alfresco 6.1.X
- Modification de l'expression CRON du calcul de volumétrie (tous les jours à 06:05:04)
- Compatibilité Alfresco 6.2.X


## [1.5.9] - 2018-11-20
### Fixed
- Correctifs mineurs


## [1.5.8] - 2018-08-07
### Fixed
- Correction de l'affichage des graphiques de volumétrie avec des données qui ont un format différent (Ko / Mo / Go / To)
- Correction de l'affichage du graphique de volumétrie en mode "courbe"
- Correction du retour JSON du webscript "select-audits"


## [1.5.7] - 2018-04-13
### Changed
- Utilisation de "web fragments" (le fichier web.xml n'est plus écrasé par le module) (https://github.com/atolcd/alfresco-audit-share/issues/14)


## [1.5.6] - 2018-03-29
### Changed
- Optimisation : utilisation de caches pour la vérification d'appartenance aux groupes


## [1.5.5] - 2018-03-09
### Added
- Enrichissement API Javascript

### Changed
- Modification de l'expression CRON du calcul de volumétrie (toutes les 4 heures)
- Compatibilité Alfresco 5.2.x

### Fixed
- Correction "Erreur 414" (Request-URI Too Large) lorsqu'il y a beaucoup de sites créés
- Correction bug sur le calcul de la volumétrie
- Correction NPE si le referer est null (https://github.com/atolcd/alfresco-audit-share/issues/4)
- Corrections vulnérabilités/bugs mineurs


## [1.5.4] - 2017-03-15
### Added
- Filtrage des graphiques par typologie des documents dans les sites
- Filtrage des connexions utilisateurs en fonction d'un ou plusieurs groupes
- Affichage de la version du module dans les interfaces d'AuditShare

### Changed
- Gestion des valeurs nulles dans les graphiques de volumétrie

### Fixed
- Correction de l'affichage par site de la volumétrie (option "Afficher les résultats par site")


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
