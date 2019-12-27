package vn.teko.runner

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.elasticsearch.spark.sql._
import vn.teko.processor.TripProcessor

object TaxiTripProcessor {

  def main(args: Array[String]): Unit = {
    val appName = "Taxi trip processor"

    val sparkConf = new SparkConf()
      .set("hive.exec.dynamic.partition", "true")
      .set("hive.exec.dynamic.partition.mode", "nonstrict")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.sql.hive.convertMetastoreParquet", "true")
      .set("spark.sql.parquet.filterPushdown", "true")
      .set("spark.sql.cbo.enabled", "true")
      .set("spark.es.nodes", "elasticsearch")
      .set("spark.es.port", "9200")
      .set("spark.es.nodes.wan.only","true")
      .setAppName(appName)

    implicit val session: SparkSession = SparkSession.builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()

    val df = session.table("taxi_trips")

    val output = TripProcessor.mapping(df)

    val conf = Map("es.mapping.exclude" -> "date")
    output.saveToEs("taxi-{date}", conf)
  }
}
