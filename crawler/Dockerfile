FROM teko/hadoop:latest

ENV HADOOP_HOME /opt/hadoop-$HADOOP_VERSION

WORKDIR /opt

COPY gdown.pl /usr/local/bin/
RUN chmod +x /usr/local/bin/gdown.pl

COPY download.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/download.sh

COPY entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/entrypoint.sh

ENTRYPOINT ["entrypoint.sh"]

CMD ["bash"]