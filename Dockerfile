FROM tomcat:7-jre8
MAINTAINER  <dockers@logistimo.com>

ARG warname

ENV TOMCAT_HOME /usr/local/tomcat

RUN rm -rf $TOMCAT_HOME/webapps/*

ADD modules/web/target/$warname $TOMCAT_HOME/webapps/

RUN unzip -o $TOMCAT_HOME/webapps/$warname \
        -d $TOMCAT_HOME/webapps/ROOT/ \
        && rm -rf $TOMCAT_HOME/webapps/$warname

ENV MYSQL_HOST=localhost \
        MYSQL_USER=logistimo \
        MYSQL_PASS=logistimo \
        HADOOP_HOST=localhost \
        REDIS_HOST=localhost \
        LOGI_HOST=localhost \
        TASK_SERVER=true \
        TASK_EXPORT=true \
        EMAIL_HOST=localhost \
        EMAIL_PORT=25 \
        EMAIL_FROMADDRESS=dockers@logsitimo.com \
        EMAIL_FROMNAME=Logistimo\ Service \
        TASK_PORT=8080 \
        CALLISTO_HOST=localhost \
        CALLISTO_PORT=8090 \
        LOCAL_ENV=true


COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN chmod +x /docker-entrypoint.sh

EXPOSE 8080-8090

CMD ["/docker-entrypoint.sh"]
