package truvarirm

import com.sun.star.uno._

case class TruvariReportMetaData(
    sOfficeBinaryPath: String,
    truvariTemplatePath: String,
    outputPath: String,
    reportSheetPairs: Array[ReportSheetPair]
)

case class ReportSheetPair(
    path: String, 
    sheetName: String
)

object ReportManager {

    def createReport(
        info: TruvariReportMetaData
    ): Unit = {
        
        assert(os.exists(os.Path(info.sOfficeBinaryPath)), s"Was not able to find open office binary at : ${info.sOfficeBinaryPath}")
        assert(os.exists(os.Path(info.truvariTemplatePath)), s"Was not able to find Truvari Template path at : ${info.truvariTemplatePath}")

        val ctx: XComponentContext = SOffice.createContext(info.sOfficeBinaryPath)
        val doc = SOffice.openDoc(info.truvariTemplatePath, ctx)

        info.reportSheetPairs.zipWithIndex.foreach { 

            case (rsPair, index) =>

                val rawSheetName = ("raw" + rsPair.sheetName).map(_.toLower)

                Truvari.duplicateReportTemplate(
                    rawSheetName,
                    rsPair.sheetName.map(_.toLower),
                    index.toShort,
                    doc
                )

                Truvari.insertData(
                    rsPair.path + "/giab_report.txt",
                    rawSheetName,
                    doc
                )
        }

        doc.getSheets().removeByName(Truvari.RAW_DATA)
        doc.getSheets().removeByName(Truvari.FORMATTED)

        println("\n")
        println(s"saving results to ${info.outputPath}")
        SOffice.save(info.outputPath, doc)
        
        println(s"closing template")
        SOffice.closeDoc(doc)

        println(s"opening results in new Spreadsheet, loaded from ${info.outputPath}")
        SOffice.openDoc(info.outputPath, ctx)
        println("All reports from Giab are merged.")
        println("Thank you for using Truvari Report Manager!")
        println("Have a nice day, and goodbye :-)")
        System.exit(0)
    }

}