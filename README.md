"AuditShare" for Alfresco
================================

**Audit Share** is an extension totally integrated into the Share UI accessible by **administrators** and **sites managers**.  
This app displays sites and repository usage info: number of created/read/updated/deleted documents displayed by day/week/month/year.   
In sites, others informations are "audited" concerning wiki, blog and discussions components but also users connected and sites volumetry.   

Works with:  
 - Alfresco Community 4.x
 - Alfresco Enterprise 4.x


Building the module
-------------------
Check out the project if you have not already done so 

        git clone https://github.com/atolcd/alfresco-audit-share.git

Ant build scripts are provided to build AMP files containing the custom files.  
Before building, ensure you have edited the `build.properties` files to set the path to your Alfresco SDK.  

To build AMP files, run the following command from the base project directory:

        ant prepare-package-amp


Download AMP modules
-------------------
Don't want to compile sources?  
You can download latest stable version here: http://labs.atolcd.com/projects/auditshare/files 



Installing the module
---------------------
This extension is a standard Alfresco Module, so experienced users can skip these steps and proceed as usual.
  
1. Stop Alfresco
2. Use the Alfresco [Module Management Tool](http://wiki.alfresco.com/wiki/Module_Management_Tool) to install the modules in your Alfresco and Share WAR files:

        java -jar alfresco-mmt.jar install auditshare-module-alfresco-X.X.X.amp $TOMCAT_HOME/webapps/alfresco.war -force
        java -jar alfresco-mmt.jar install auditshare-module-share-X.X.X.amp $TOMCAT_HOME/webapps/share.war -force

3. Delete the `$TOMCAT_HOME/webapps/alfresco/` and `$TOMCAT_HOME/webapps/share/` folders.  
**Caution:** please ensure you do not have unsaved custom files in the webapp folders before deleting.
4. Start Alfresco


Overrides
---------------------
**Caution:** Share **web.xml** file is overridden by the module (to declare new filters).


Using the module
---------------------

**/!\ Only actions performed from Share UI are "audited" /!\**
Be aware that this module does **NOT** use alfresco audit mechanisms.

#### Configuration
This module uses latest Share 4.x extension mechanisms.
You can deploy/undeploy AuditShare menus directly from : `http://server:port/share/page/modules/deploy`  



#### CRON volumetry

Default CRON expression (every hour): `share.stats.sites.volumetry.trigger=0 0 0/1 * * ?`  

This expression can be overridden from alfresco-global.properties file (http://wiki.alfresco.com/wiki/Scheduled_Actions#Cron_Explained).   
Example (at 1:00 PM and 9:00 PM every day): `share.stats.sites.volumetry.trigger=0 0 13,21 * * ?`   


![Header](/documentation/header.png "Header")   
![Module deployment](/documentation/module-deployment.png "Module deployment")   
![Volumetry by site](/documentation/site-volumetry-by-site.png "Volumetry by site")   


LICENSE
---------------------
This extension is licensed under `GNU Library or "Lesser" General Public License (LGPL)`.  
Created by: Alexandre NICOLAS and [Bertrand FOREST] (https://github.com/bforest) 


Our company
---------------------
[Atol Conseils et Développements] (http://www.atolcd.com) is Alfresco [Gold Partner] (http://www.alfresco.com/partners/atol)  
Follow us on twitter [ @atolcd] (https://twitter.com/atolcd)  