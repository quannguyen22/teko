FROM teko/spark:latest

ENV HADOOP_HOME /opt/hadoop-$HADOOP_VERSION

WORKDIR /opt

COPY target/dumper-1.0-jar-with-dependencies.jar /opt/job.jar
COPY submit.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/submit.sh

ENTRYPOINT ["entrypoint.sh"]

CMD ["bash"]