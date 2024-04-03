[![Build Status](https://travis-ci.org/atolcd/alfresco-audit-share.svg?branch=master)](https://travis-ci.org/atolcd/alfresco-audit-share)
[![HitCount](http://hits.dwyl.io/atolcd/alfresco-audit-share.svg)](http://hits.dwyl.io/atolcd/alfresco-audit-share)

"AuditShare" for Alfresco
================================

**AuditShare** is an extension totally integrated into the Share UI accessible by **administrators** and **site managers**.   
This app displays sites and repository usage info: number of created/read/updated/deleted documents displayed by day/week/month/year.  
In sites, others informations are "audited" concerning wiki, blog and discussions components but also users connected and sites volumetry.  

Works with:
 - Alfresco Enterprise 4.2.x, 5.0.x, 5.1.x, 5.2.x, 6.1.X, 6.2.x, 7.0.x, 7.1.X, 7.2.X, 7.3.X (should works on Community versions but not tested)


Building the module
-------------------
Check out the project if you have not already done so

        git clone https://github.com/atolcd/alfresco-audit-share.git

Maven build scripts are provided to build AMP files containing the custom files.  
**Before building, ensure you have correctly configured repositories in the parent pom.xml and docker-compose.yml files (alfresco-public for Community, alfresco-private-repository for Enterprise).**  

To build AMP files, run the following command from the base project directory:

        mvn clean package



Installing the module
---------------------
This extension is a standard Alfresco Module, so experienced users can skip these steps and proceed as usual.

1. Stop Alfresco
2. Use the Alfresco [Module Management Tool](http://wiki.alfresco.com/wiki/Module_Management_Tool) to install the modules in your Alfresco and Share WAR files:

        java -jar alfresco-mmt.jar install com.atolcd.alfresco.auditshare-XXX-repo-X.X.X.amp $TOMCAT_HOME/webapps/alfresco.war -force
        java -jar alfresco-mmt.jar install com.atolcd.alfresco.auditshare-XXX-share-X.X.X.amp $TOMCAT_HOME/webapps/share.war -force

3. Delete the `$TOMCAT_HOME/webapps/alfresco/` and `$TOMCAT_HOME/webapps/share/` folders.  
**Caution:** please ensure you do not have unsaved custom files in the webapp folders before deleting.
4. Start Alfresco

Inside the project, you will find the `run.bat` and `run.sh` scripts. These are convenience scripts to quickly compile / test / run the project.
In the terminal window, use:

- `./run.sh build_start` for Mac OS X or Linux.
- `run.bat build_start` for Windows.

If this is the first time you are doing this, it will take a while for Maven to download all the required dependencies and for Docker to download all the required images.

For more information about how to work with SDK project, see the [online documentation](https://docs.alfresco.com/content-services/latest/develop/sdk/#workingwithprojects).


Using the module
---------------------

**/!\ Only actions performed from Share UI are "audited" /!\\**  
Be aware that this module does **NOT** use alfresco audit mechanisms.

#### Configuration
This module uses latest Share extension mechanisms.  
You can deploy/undeploy AuditShare menus directly from : `http://server:port/share/page/modules/deploy`



#### CRON volumetry

Default CRON expression (every day at 06:05:04): `share.stats.sites.volumetry.trigger=4 5 6 * * ?`

This expression can be overridden from alfresco-global.properties file (http://wiki.alfresco.com/wiki/Scheduled_Actions#Cron_Explained).  
Example (at 1:00 PM and 9:00 PM every day): `share.stats.sites.volumetry.trigger=0 0 13,21 * * ?`  


Screeshots
---------------------

![Sites stats](/screenshots/auditshare-01.jpg "Sites stats")  

![User connections](/screenshots/auditshare-02.jpg "User connections")  

![Volumetry](/screenshots/auditshare-03.jpg "Sites volumetry")  



Contributors
-------------------

Created by: [Alexandre NICOLAS](https://github.com/alexandre-nicolas) and [Bertrand FOREST](https://github.com/bforest)

Contributors:
- [Alexandre PINTO](https://github.com/apinto-atolcd)
- [Anicet PRAO](https://github.com/anicet-prao)
- [Antoine HEITZMANN](https://github.com/aheitzmann-atolcd)
- [Benjamin BRUDO](https://github.com/BenBrudo)
- [Julien BERTHOUX](https://github.com/jberthoux)
- [Stéphane PROUVEZ](https://github.com/sprouvez)
- [Laurent MEUNIER](https://github.com/lmeunier)
- [Zylknet](https://www.zylk.net)
- [Etienne PERRIER](https://github.com/etienneperrier)
- [Fabien CHENE](https://github.com/CheneFabien)
- [Édnei Cristian](https://github.com/edneiCristian)



LICENSE
---------------------
This extension is licensed under `GNU Library or "Lesser" General Public License (LGPL)`.  



Our company
---------------------
[Atol Conseils et Développements](http://www.atolcd.com) is Alfresco [Strategic Partner](http://www.alfresco.com/partners/atol)
Follow us on Twitter [@atolcd](https://twitter.com/atolcd)
