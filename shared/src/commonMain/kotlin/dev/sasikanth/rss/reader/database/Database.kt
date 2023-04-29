package dev.sasikanth.rss.reader.database

fun createDatabase(driverFactory: DriverFactory): ReaderDatabase {
  val driver = driverFactory.createDriver()
  return ReaderDatabase(driver)
}
