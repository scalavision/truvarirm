package truvarirm

/**
 */
object Main {

    val report = "report"
    val soffice = "soffice"
    val truvariTemplate = "truvariTemplate"
    val outputPath = "out"

    val helpMessage = s"""
    | Truvari Report Manager (truvarirm) Help message!
    |
    | Merges results from several truvari runs into one Spreadsheet document. Each spreadsheet tab
    | has to be named uniquely.
    |
    | truvarirm command example:
    |
    | truvarirm \\
    |  --$soffice </absolute/path/to/open office executable> \\
    |  --$truvariTemplate /path/to/Truvari Report Template.ods \\
    |  --$outputPath /absolute/path/to/output/fileName.ods \\
    |  --$report /absolute/path/to/truvari/result_folder_1:NameOfSheet1 \\
    |            /absolute/path/to/truvari/result_folder_2:NameOfSheet2 \\
    |            /absolute/path/to/truvari/result_folder_3:NameOfSheet3
    |
    |The "NameOfSheet" must be unique for each truvari result
    |
    |The report parameters is a list of tuples, separated by colon :
    |
    |- path to the folder where the giab_report.txt file is found
    |- Unique name of the Sheet for this truvari result (use simple names without funny characters, 
    |  there are no input validation other than whats supported by OpenOffice / LibreOffice / Excel)
    |
    |Thank You!
    """.stripMargin
    
    def infoMessage(args: Array[String]): String = s"""
    |
    |Running Truvari Report Manager to assemble all results into one Spreadsheet.
    |  
    |The Following arguments where given to the report manager:
    |
    |  ${args.mkString("\n  ")}
    |
    |* Processing *
    |
    """.stripMargin

    def main(args: Array[String]): Unit = 

        if(
            args.size < 8 || 
            args(0).trim() != s"--$soffice" || 
            args(2).trim() != s"--$truvariTemplate" ||
            args(4).trim() != s"--$outputPath" ||
            args(6).trim() != s"--$report") 

            { println(helpMessage) } 
        
        else {

            println(infoMessage(args))

            val reportMetaData = TruvariReportMetaData(
                sOfficeBinaryPath = args(1),
                truvariTemplatePath = args(3),
                outputPath = args(5),
                reportSheetPairs = args.dropWhile(_ != s"--$report")
                    .drop(1)
                    .map(_.split(":"))
                    .map(s => ReportSheetPair(s.head, s.last))
            )

            ReportManager.createReport(reportMetaData)
        }

}