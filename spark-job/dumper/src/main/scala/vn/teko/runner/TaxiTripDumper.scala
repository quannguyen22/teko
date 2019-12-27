package vn.teko.runner

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import vn.teko.output.HiveTable

object TaxiTripDumper {

  def main(args: Array[String]): Unit = {
    val appName = "Taxi trip dumper"

    val sparkConf = new SparkConf()
      .set("hive.exec.dynamic.partition", "true")
      .set("hive.exec.dynamic.partition.mode", "nonstrict")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.sql.hive.convertMetastoreParquet", "true")
      .set("spark.sql.parquet.filterPushdown", "true")
      .set("spark.sql.cbo.enabled", "true")
      .set("spark.es.nodes","<IP-OF-ES-NODE>")
      .set("spark.es.port","<ES-PORT>")
      .setAppName(appName)

    implicit val session: SparkSession = SparkSession.builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()

    val schema = new StructType()
      .add("vendor_id", StringType, nullable = true)
      .add("pickup_datetime", TimestampType, nullable = true)
      .add("dropoff_datetime", TimestampType, nullable = true)
      .add("passenger_count", IntegerType, nullable = true)
      .add("trip_distance", DoubleType, nullable = true)
      .add("pickup_longitude", DoubleType, nullable = true)
      .add("pickup_latitude", DoubleType, nullable = true)
      .add("rate_code", IntegerType, nullable = true)
      .add("store_and_fwd_flag", StringType, nullable = true)
      .add("dropoff_longitude", DoubleType, nullable = true)
      .add("dropoff_latitude", DoubleType, nullable = true)
      .add("payment_type", StringType, nullable = true)
      .add("fare_amount", DoubleType, nullable = true)
      .add("surcharge", DoubleType, nullable = true)
      .add("mta_tax", DoubleType, nullable = true)
      .add("tip_amount", DoubleType, nullable = true)
      .add("tolls_amount", DoubleType, nullable = true)
      .add("total_amount", DoubleType, nullable = true)

    val df = session.read.format("csv")
      .option("header", "true").schema(schema)
      .load("/data/nyc_taxi_data_2014.csv")

    val output = df.withColumn("date", date_format(col("pickup_datetime"), "yyyy-MM-dd").as("format"))

    val hiveTable = new HiveTable("default", "taxi_trips")
    hiveTable.write(output)
  }
}
