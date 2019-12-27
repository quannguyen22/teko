package vn.teko.mapping

import java.sql.Timestamp

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.junit.jupiter.api.Test
import vn.teko.processor.TripProcessor
import org.assertj.core.api.Assertions.assertThat

object TestMapping {

  case class Trip(vendor_id: String, pickup_datetime: Timestamp, dropoff_datetime: Timestamp, passenger_count: Int, trip_distance: Double, pickup_longitude: Double, pickup_latitude: Double, rate_code: Int, store_and_fwd_flag: String, dropoff_longitude: Double, dropoff_latitude: Double, payment_type: String, fare_amount: Double, surcharge: Double, mta_tax: Double, tip_amount: Double, tolls_amount: Double, total_amount: Double, date: String)

  implicit lazy val session: SparkSession = SparkSession.builder()
    .master("local[2]")
    .config("spark.ui.enabled", value = false)
    .config("spark.ui.showConsoleProgress", value = false)
    .getOrCreate()

  import session.implicits._

  val sourceDf = Seq(Trip("VTS", Timestamp.valueOf("2014-01-31 04:09:00"), Timestamp.valueOf("2014-01-31 04:25:00"), 1, 5.19, -73.963352, 40.711287, 1, null, -73.95489, 40.6993, "CSH", 17.5, 0.5, 0.5, 0.0, 0.0, 18.5, "2014-01-31"),
    Trip("CMT", Timestamp.valueOf("2014-01-31 00:56:12"), Timestamp.valueOf("2014-01-31 00:58:57"), 1, 0.7, -73.972699, 40.764493, 1, "N", -73.982041, 40.762678, "CRD", 4.5, 0.5, 0.5, 0.75, 0.0, 6.25, "2014-01-31"),
    Trip("VTS", Timestamp.valueOf("2014-01-31 04:29:00"), Timestamp.valueOf("2014-01-31 04:35:00"), 2, 1.13, -73.962362, 40.760587, 1, null, -73.973742, 40.75613, "CRD", 6.0, 0.5, 0.5, 1.62, 0.0, 8.62, "2014-01-31"),
    Trip("CMT", Timestamp.valueOf("2014-01-31 00:30:12"), Timestamp.valueOf("2014-01-31 00:35:07"), 1, 1.4, -73.963331, 40.77485, 1, "N", -73.98143, 40.774473, "CRD", 7.0, 0.5, 0.5, 1.0, 0.0, 9.0, "2014-01-31"),
    Trip("VTS", Timestamp.valueOf("2014-01-31 04:15:00"), Timestamp.valueOf("2014-01-31 04:34:00"), 2, 6.72, -74.00495, 40.728927, 1, null, -73.952832, 40.783375, "CRD", 22.0, 0.5, 0.5, 4.5, 0.0, 27.5, "2014-01-31")
  ).toDF()

  @Test
  def testWeekDay(): Unit = {
    val output = TripProcessor.mapping(sourceDf)
    output.show(5)

    val actual = output.filter(col("pickup_week_day") === "Friday").count()
    assertThat(actual).isEqualTo(5)
  }
}
