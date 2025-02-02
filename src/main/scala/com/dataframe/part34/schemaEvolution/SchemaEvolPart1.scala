package com.dataframe.part34.schemaEvolution


//https://stackoverflow.com/questions/56782404/reorder-source-spark-dataframe-columns-to-match-the-order-of-the-target-datafram
//https://medium.com/readme-mic/etl-with-standalone-spark-containers-for-ingesting-small-files-8d6ee2ebda63

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types._

object SchemaEvolPart1 {

  def main(args: Array[String]): Unit = {


    val logger = Logger.getLogger("HbIngestion")
    //Logger.getLogger("org").setLevel(Level.WARN)
    //Logger.getLogger("akka").setLevel(Level.WARN)
    val startTimeMillis = System.currentTimeMillis()

    val spark=SparkSession.builder()
      .appName("Test2").master("local").config("spark.driver.memory","2g").enableHiveSupport().getOrCreate()

    import spark.implicits._

    val df = Seq(("Sri","123"),("Hari","786")).toDF("Name","ID")

    val schemaUntyped = new StructType()
      .add("name1", "string")
      .add("id2", "int")

    val df1 = Seq(("Sri","123","scala"),("Hari","786","spark")).toDF("Name","ID","Skils")

    val schemaUntyped2 = new StructType()
      .add("name1", "string")
      .add("id2", "int")
      .add("skills","string")

    val df2=spark.createDataFrame(df.rdd,schema = schemaUntyped)

    df2.printSchema()

    val df3=spark.createDataFrame(df.rdd,schema = schemaUntyped2)

    df3.printSchema()

    val df2Columns = df2.schema.toList.map(sf => (sf.name.toLowerCase, sf.dataType)).toMap

    val df3Columns = df3.schema.toList.map(sf => (sf.name.toLowerCase, sf.dataType)).toMap

    val differenceColumns = df2Columns.keySet -- df3Columns.keySet

    println("Diff of column names")
    println(differenceColumns)


    for (cName <- differenceColumns) {
      val cType = df3Columns(cName) match {
        case IntegerType => "INTEGER"
        case LongType => "BIGINT"
        case DoubleType => "DOUBLE PRECISION"
        case FloatType => "REAL"
        case ShortType => "INTEGER"
        case ByteType => "SMALLINT" // Redshift does not support the BYTE type.
        case BooleanType => "BOOLEAN"
        case StringType => s"VARCHAR(500)" // "TEXT"
        case TimestampType => "TIMESTAMP"
        case DateType => "DATE"
        case t: DecimalType => s"DECIMAL(10,2)"
        case _ => throw new IllegalArgumentException(s"Don't know how to save $cName to JDBC")
      }

      //val schemaTable = s"${etlConfig.redshiftSchema}.${etlConfig.redshiftTable}"

      println("ALTER TABLE table ADD COLUMN "+cName+" "+ cType +";")

    }



    import org.apache.spark.sql.types.{StructField, StructType}

    val schemaDiff: (StructType, StructType)  => List[StructField] = (schema1, schema2) => {
      val toMap: StructType => Map[String, StructField] = schema => {
        schema.map(sf => {
          val name = s"${sf.name}-${sf.dataType.typeName}-${sf.nullable.toString}"
          (name -> sf)
        }).toMap
      }

      val schema1Set = toMap(schema1).toSet
      val schema2Set = toMap(schema2).toSet
      val commonItems =  schema1Set.intersect(schema2Set)

      (schema1Set ++ schema2Set -- commonItems).toMap.values.toList
    }

    //println(schemaDiff(df2.schema,df3.schema))

    //println(schemaDiff(df3.schema,df2.schema))

    val x = df2.schema.sortBy(x => x.name) // get dataframe 1 schema and sort it base on column name.
    val y = df3.schema.sortBy(x => x.name) // // get dataframe 2 schema and sort it base on column name.

    println(x)
    println(y)

    val out = x.zip(y).filter(x => x._1 != x._2) // zipping 1st column of df1, df2 ...2nd column of df1,df2 and so on for all columns and their datatypes. And filtering if any mismatch is there

    if(out.size == 0) { // size of `out` should be 0 if matching
      println("matching")
    }
    else println("not matching")

    println(out)

    df2.registerTempTable("test")


    val df4 = spark.sql("desc test")

    df4.show()


    //https://stackoverflow.com/questions/56782404/reorder-source-spark-dataframe-columns-to-match-the-order-of-the-target-datafram
    //https://medium.com/readme-mic/etl-with-standalone-spark-containers-for-ingesting-small-files-8d6ee2ebda63


    val nonrdd = spark.sql("desc test").collect().toString


    val rdd = spark.sparkContext.makeRDD(List(nonrdd))

    println(rdd)

    rdd.toDF.show()


    val schemadf = spark.sql("desc test")


    schemadf.show(1000,false)




  }

}
