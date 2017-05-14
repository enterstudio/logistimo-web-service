Logistimo
=========

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](http://www.gnu.org/licenses/agpl-3.0)

Logistimo is a software for managing supply-chains in low-resource environments. 
It is designed to help manage every stage in a supply-chain from procurement to delivery. Please visit [www.logistimo.com](www.logistimo.com) for further details 

Latest Release
------------------

The most recent release is Logistimo [2.4.0](https://github.com/logistimo/logistimo-web/tree/2.4.0), released on 2017 May 13th.

How to use Logistimo SCM
-------------------------

[Knowledge-base ](https://logistimo.freshdesk.com)

Pre-requisites
------------------

* JDK 1.8
* MariaDB 10.1.23
* Redis 
* ActiveMQ 5.14
* Hadoop CDH5
* Tomcat 7
* [Fusion charts](http://www.fusioncharts.com/) - This application utilises Fusion charts heavily for all charting needs. 
Fusion charts, however is not open source. You need to purchase the appropriate license from fusion charts, and copy the
files to "modules/api/src/main/webapp/v2/js/fusioncharts-3.11.2.6/" folder and uncomment the script include tags in
"modules/api/src/main/webapp/v2/index.html". Also note that we have begun the work to migrate to nvd3 which is apache 2.0
licensed. So even with out fusion charts graphs and charts will work.
* [Glyphicons](http://glyphicons.com) - Glyphicons pro version is required to ensure all icons work in the project. Once
 purchased, css should be included in the "modules/api/src/main/webapp/v2/css/3rdparty" and fonts should be included in 
 "modules/api/src/main/webapp/v2/fonts/". Finally, include the following line in index.html
 ```
 <link rel="stylesheet" href="css/3rdparty/glyphicons-1.9.2.1.css">
 ```
 * [Google maps](https://developers.google.com/maps/pricing-and-plans/) - Google maps is not free when the application is password protected and not freely accessible by the public.
 Before taking this to production purchase the license.

Tech stack
----------------

* [Angular JS](http://angularjs.org)
* Spring MVC
* [Datanucleus JDO](http://datanucleus.org)
* Apache Camel
* D3.js
* Apache POI
* [Bootstrap](http://getbootstrap.com/)
* [Glyphicons](http://glyphicons.com)
* [Angular UI bootstrap](https://angular-ui.github.io/bootstrap/)
 * nvd3.js

Modules
-------
1. Accounting
2. Assets
3. Authentication and Authorisation
4. Activity
5. Bulk Uploads
6. Communication
7. Configuration
8. Conversation
9. Custom Reports
10. Dashboards
11. Domains
12. Stores / Entities
13. Events
14. Exports
15. Inventory
16. Material Catalog
17. Media
18. Orders
19. Reports
20. User Accounts

Mailing Lists
-------------

For broad, opinion based, ask for external resources, debug issues, bugs, contributing to the project, and scenarios, it is recommended you use the community@logistimo.com mailing list.

community@logistimo.com  is for usage questions, help, and announcements.
[subscribe](https://groups.google.com/a/logistimo.com/d/forum/community/join) [unsubscribe](mailto:unsubscribe+community@logistimo.com)

developers@logistimo.com  is for people who want to contribute code to Logistimo.
[subscribe](https://groups.google.com/a/logistimo.com/d/forum/developers/join) [unsubscribe](mailto:unsubscribe+community@logistimo.com)

License Terms
---------------------------

This program is part of Logistimo SCM. Copyright © 2017 Logistimo.

Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL). 

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.

You can be released from the requirements of the license by purchasing a commercial license. To know more about the commercial license, please contact us at opensource@logistimo.com

Trademarks
----------

Logistimo, Logistimo.com, and the Logistimo logo are trademarks and/or service marks. Users may not make use of the trademarks or service marks without prior written permission. Other trademarks or trade names displayed on this website are the property of their respective trademark owners and subject to the respective owners’ terms of use.
