#!/bin/bash

export SPARK_MASTER_URL=${SPARK_MASTER}

spark-submit \
    --class "vn.teko.runner.TaxiTripDumper" \
    --master ${SPARK_MASTER_URL} \
    /opt/job.jar

