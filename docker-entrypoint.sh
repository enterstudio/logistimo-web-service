#!/bin/bash

set -e

sed -ri "$ a logi.host.server=$LOGI_HOST" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "s/localhost/$HADOOP_HOST/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/core-site.xml \
        && sed -ri "/db.url=/d" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "/reportsdb.url=/d" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "$ a db.url=jdbc:mariadb://$MYSQL_HOST:3306/logistimo" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "/jdbc:mariadb:/ s/localhost/$MYSQL_HOST/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/META-INF/jdoconfig.xml \
        && sed -ri "s/db.user=logistimo/db.user=$MYSQL_USER/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "s/db.password=logistimo/db.password=$MYSQL_PASS/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "s/name="javax.jdo.option.ConnectionUserName" value="logistimo"/name="javax.jdo.option.ConnectionUserName" value=$MYSQL_USER/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/META-INF/jdoconfig.xml \
        && sed -ri "s/name="javax.jdo.option.ConnectionPassword" value="logistimo"/name="javax.jdo.option.ConnectionPassword" value=$MYSQL_PASS/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/META-INF/jdoconfig.xml \
        && sed -ri "s/localhost:50070/$HADOOP_HOST:50070/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
	&& sed -ri "s/redis.server=localhost/redis.server=$REDIS_HOST/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "s/task.server=true/task.server=$TASK_SERVER/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "s/export.route.start=true/export.route.start=$TASK_EXPORT/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "/task.url/ s/localhost:8080/localhost:$TASK_PORT/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "s/mail.smtp.host=localhost/mail.smtp.host=$EMAIL_HOST/g;s/mail.smtp.port=25/mail.smtp.port=$EMAIL_PORT/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "s/email.fromaddress=logistimo@gmail.com/email.fromaddress=$EMAIL_FROMADDRESS/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "s/email.fromname=Logistimo Service/email.fromname=$EMAIL_FROMNAME/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "s/local.environment=true/local.environment=$LOCAL_ENV/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/samaanguru.properties \
        && sed -ri "s/, CONSOLE/, datalog/g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/log4j.properties \
        && sed -ri "s~lgweb.log~logs\/lgweb.log~g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/log4j.properties \
        && sed -ri "s~mobile.log~logs\/mobile.log~g" $TOMCAT_HOME/webapps/ROOT/WEB-INF/classes/log4j.properties \
        && sed -ri "/Jad\/Jar download servlet/,+4d" $TOMCAT_HOME/webapps/ROOT/WEB-INF/web.xml \
	&& sed -ri "s/8080/$TASK_PORT/g" $TOMCAT_HOME/conf/server.xml


exec $TOMCAT_HOME/bin/catalina.sh run
