#!/bin/bash

gdown.pl $1
hdfs dfsadmin -safemode leave
hdfs dfs -mkdir /data
echo "Put nyc_taxi_data_2014.csv into hdfs"
hdfs dfs -put nyc_taxi_data_2014.csv /data
rm -f nyc_taxi_data_2014.csv