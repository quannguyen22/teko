package vn.teko.output

import com.typesafe.scalalogging.LazyLogging
import org.apache.calcite.sql.SqlDialect.DatabaseProduct
import org.apache.calcite.sql.util.SqlBuilder
import org.apache.spark.sql.types.{StringType, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.lit
import org.apache.spark.storage.StorageLevel

class HiveTable(dbName: String, tableName: String)(implicit session: SparkSession) extends LazyLogging with Serializable {
  private val partitionColumns = Seq[String](
    "date"
  )

  def write(df: DataFrame): Unit = {
    logger.info(s"Processing table $tableName")

    val inputSchema = df.schema
    val outputSchema = if (!session.catalog.tableExists(dbName, tableName)) {
      createTable(inputSchema)
    } else {
      alterTable(inputSchema)
    }

    val inputColumns = inputSchema.fieldNames.map(f => (f.toLowerCase, f)).toMap
    val selectedColumns = outputSchema.fields.map { field =>
      val name = field.name.toLowerCase()
      if (inputColumns.contains(name)) {
        df(inputColumns(name))
      } else {
        lit(null: String).cast(field.dataType)
      }
    }

    // Coalese using 32 partition to speed up
    val finalOutput = df.select(selectedColumns: _*)
      .coalesce(32)
      .persist(StorageLevel.DISK_ONLY)
    // Trigger output calculation
    val outputSize = finalOutput.count()

    logger.info(s"Prepare to save $outputSize records into $tableName")

    if (logger.underlying.isDebugEnabled) {
      finalOutput.show(10)
    }

    if (outputSize > 0) {
      // Coalesce again to write only 1 output file
      finalOutput
        .coalesce(1)
        .createOrReplaceTempView(s"${tableName}_tmp")
      logger.info(s"Writing data into $dbName.$tableName")

      session.sql(s"INSERT OVERWRITE TABLE $dbName.$tableName " +
        s"PARTITION(date) SELECT * from ${tableName}_tmp")

    } else {
      logger.warn("No result calculated")
    }

    session.catalog.dropTempView(s"${tableName}_tmp")
    finalOutput.unpersist(false)
  }

  private def createTable(inputSchema: StructType): StructType = {
    val builder = new SqlBuilder(DatabaseProduct.HIVE.getDialect)
    builder.append("create table if not exists ")
      .identifier(dbName, tableName).append("(\n")
    val dataColumns = inputSchema.fields
      .filter { f =>
        !partitionColumns.contains(f.name)
      }
    val inputSchemaCount = dataColumns.length

    // Ignore date_hour column
    dataColumns
      .zipWithIndex
      .foreach { case (field, index) =>
        builder.append("\t").identifier(field.name).append(" ")
          .append(field.dataType.sql)
        if (index < inputSchemaCount - 1) {
          builder.append(",\n")
        }
      }

    builder.append("\n)\n")

    // Append partition info
    builder.append("PARTITIONED BY (\n")

    partitionColumns.zipWithIndex.foreach { case (field, index) =>
      builder.append("\t").identifier(field).append(" ").append(StringType.sql)
      if (index < partitionColumns.length - 1) {
        builder.append(",")
      }
      builder.append("\n")
    }
    builder.append(")\n")

    // Append storage configuration
    builder.append("STORED AS PARQUET").append("\n")
      .append("TBLPROPERTIES(\"parquet.compression\"=\"SNAPPY\")")

    val query = builder.toString

    logger.info(s"Creating table $dbName.$tableName")
    logger.info(s"Executing: $query")

    session.sql(query)

    session.table(s"$dbName.$tableName").schema
  }

  private def alterTable(inputSchema: StructType): StructType = {
    val outputSchema = session.table(s"$dbName.$tableName").schema

    val existColumn = outputSchema.fieldNames.map(_.toLowerCase).toSet

    val newColumns = inputSchema.fields.filter { field =>
      val fieldName = field.name.toLowerCase()
      !partitionColumns.contains(fieldName) && !existColumn.contains(fieldName)
    }

    val newColumnsCount = newColumns.length
    if (newColumnsCount > 0) {
      val builder = new SqlBuilder(DatabaseProduct.HIVE.getDialect)
      builder.append("ALTER TABLE ")
        .identifier(dbName, tableName).append(" ADD COLUMNS (\n")

      newColumns.zipWithIndex.foreach { case (col, index) =>
        builder.append("\t").identifier(col.name).append(" ")
          .append(col.dataType.sql)
        if (index < newColumnsCount - 1) {
          builder.append(",\n")
        }
      }

      builder.append("\n)\n")
      val query = builder.toString
      logger.info(s"Altering table $dbName.$tableName")
      logger.info(s"Executing: $query")

      session.sql(query)

      // Query the new schema
      session.table(s"$dbName.$tableName").schema
    } else {
      outputSchema
    }
  }
}
