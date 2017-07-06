#!/bin/bash

# Copyright © 2017 Logistimo.
#
# This file is part of Logistimo.
#
# Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
# low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
#
# This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
# <http://www.gnu.org/licenses/>.
#
# You can be released from the requirements of the license by purchasing a commercial license. To know more about
# the commercial license, please contact us at opensource@logistimo.com
#

sed -ri "$ a logi.host.server=$LOGI_HOST" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "s/localhost/$HADOOP_HOST/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/core-site.xml \
        && sed -ri "s/, CONSOLE/, datalog/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/log4j.properties \
        && sed -ri "s~lgweb.log~logs\/lgweb.log~g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/log4j.properties \
        && sed -ri "s~mobile.log~logs\/mobile.log~g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/log4j.properties \
        && sed -ri "/Jad\/Jar download servlet/,+4d" $TOMCAT_HOME/webapps/ROOT/WEB-INF/web.xml \
        && sed -ri "$ a tomcat.util.http.parser.HttpParser.requestTargetAllow=|{}" $TOMCAT_HOME/conf/catalina.properties \
        && sed -ri "s/8080/$TASK_PORT/g" $TOMCAT_HOME/conf/server.xml

if [[ "$SENTINEL_HOST" != "" ]]
then
        sed -ri "s~<\!\-\- <Context sessionCookiePath=\"\/\" crossContext=\"true\"> \-\->~<Context sessionCookiePath=\"\/\" crossContext=\"true\">~g;s~<\!\-\- <Valve className~<Valve className~g;s~sentinels=\"\"\/> \-\->~sentinels=\"$SENTINEL_HOST\"\/>~g;s~host=\"localhost\"~host=\"$REDIS_HOST\"~g;s~sentinelMaster=\"\"~sentinelMaster=\"$SENTINEL_MASTER\"~g" $TOMCAT_HOME/conf/context.xml
        sed -ri "/<Context>/d" $TOMCAT_HOME/conf/context.xml
fi

if [[ "$ACTIVEMQ_HOST" != "" ]]
then
  sed -ri "s~\(tcp:\/\/localhost:61616\)~$ACTIVEMQ_HOST~g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/camel-tasks.xml
fi

JAVA_OPTS="-Xms$JAVA_XMS -Xmx$JAVA_XMX \
        -Djavax.jdo.option.ConnectionURL=\"$MYSQL_HOST_URL\" \
  -Djavax.jdo.option.ConnectionUserName=$MYSQL_USER \
        -Djavax.jdo.option.ConnectionPassword=$MYSQL_PASS \
        -Demail.fromaddress=$EMAIL_FROMADDRESS \
        -Demail.fromname=\"$EMAIL_FROMNAME\" \
        -Dlocal.environment=$LOCAL_ENV \
        -Dtask.server=$TASK_SERVER \
        -Dtask.url=$TASK_URL \
        -Dtask.queue.type=$TASK_QUEUE_TYPE \
        -Dexport.route.start=$TASK_EXPORT \
        -Dzoo.servers=$ZKR_HOST \
        -Dredis.server=$REDIS_HOST \
        -Ddb.url=\"$MYSQL_HOST_URL\" \
        -Ddb.schema=$MYSQL_DATABASE \
        -Ddb.user=$MYSQL_USER \
        -Ddb.password=$MYSQL_PASS \
        -Dmedia.servingurllocal=$MEDIA_HOST_URL \
        -Dmail.smtp.host=$EMAIL_HOST \
        -Dmail.smtp.port=$EMAIL_PORT \
        -Dcallisto.url=$CALLISTO_HOST_URL \
        -Dredis.sentinels=$SENTINEL_HOST \
        -Dlocation.service.url=$LOC_URL \
        -Dapproval.url=$APPROVAL_URL \
  -Ddatanucleus.cache.level2.redis.servers=$REDIS_HOST \
  -Ddatanucleus.cache.level2.redis.sentinels=$SENTINEL_HOST \
  -\"javaagent://$TOMCAT_HOME/jmx_prometheus_javaagent-0.7.jar=$JMX_AGENT_PORT:$TOMCAT_HOME/jmx_exporter.json\""

exec $TOMCAT_HOME/bin/catalina.sh run
