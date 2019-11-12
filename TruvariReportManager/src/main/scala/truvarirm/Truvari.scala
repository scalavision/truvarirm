package truvarirm

import com.sun.star.sheet._
import com.sun.star.table._

object Truvari {

  val RAW_DATA = "RawData"
  val FORMATTED = "Formatted"

  def isAllDigits(x: String) = x forall Character.isDigit

  def insertData(
      giabPath: String,
      sheetName: String,
      doc: XSpreadsheetDocument,
  ): Unit = {

    val reportText = os.read(os.Path(giabPath)).lines.toVector
    val templateRaw = SOffice.getSheetByName(doc, sheetName)

    println(s"Adding data to $sheetName")

    // Inserting all data into Spreadsheet
    for (i <- 0 to reportText.size - 1) {

      val spreadSheetLineIndex = i + 2
      val columns = reportText(i).split("\t")

      for (j <- 0 to columns.size - 1) {

        val cellValue = columns(j)

        if (cellValue.isEmpty()) {
          print(".")
        } else if (isAllDigits(cellValue)) {
          SOffice.insertIntoCell(
            j,
            spreadSheetLineIndex,
            cellValue,
            templateRaw,
            "V"
          )
        } else {
          SOffice.insertIntoCell(
            j,
            spreadSheetLineIndex,
            cellValue,
            templateRaw,
            ""
          )
        }

      }
    }

    println(".. Finishing up ..")
    println("\n")

  }

  def duplicateReportTemplate(
    rawDataSheetname: String, 
    formattedSheetname: String,
    index: Short,
    doc: XSpreadsheetDocument
  ) = {

    println("Creating new sheets from template")

    SOffice.copySheet(
      RAW_DATA,
      rawDataSheetname,
      (index + 1).toShort,
      doc
    )

    SOffice.copySheet(
      FORMATTED,
      formattedSheetname,
      index,
      doc
    )

    val formattedSheet: XSpreadsheet = 
      SOffice.getSheetByName(doc, formattedSheetname)
      
    for (j <- 0 to 150) {
      for(i <- 0 to 30){
        val cell: XCell = formattedSheet.getCellByPosition(i,j)
        val formula = cell.getFormula()
        if(formula.contains(RAW_DATA)) {
          val updatedFormula = formula.replace(RAW_DATA, rawDataSheetname)
          SOffice.insertIntoCell(i,j, updatedFormula, formattedSheet, "")
        }
      }
    }

    SOffice.insertIntoCell(
      1, 
      0, 
      s"$formattedSheetname", 
      SOffice.getSheetByName(doc, rawDataSheetname), 
      ""
    )

  }

}
