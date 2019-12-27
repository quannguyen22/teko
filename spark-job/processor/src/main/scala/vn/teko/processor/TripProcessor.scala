package vn.teko.processor

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object TripProcessor {
  def mapping(df: DataFrame)(implicit session: SparkSession): DataFrame = {

    val esTimeStampFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    df.withColumn("timestamp", date_format(col("pickup_datetime"), esTimeStampFormat))
      .withColumn("pickup_week_day", date_format(col("pickup_datetime"), "EEEE"))
      .withColumn("pickup_hour", hour(col("pickup_datetime")))
      .withColumn("duration", (unix_timestamp(col("dropoff_datetime")) - unix_timestamp(col("pickup_datetime"))) / 60)
  }
}
