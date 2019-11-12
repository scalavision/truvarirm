package truvarirm

import org.specs2.Spec
import org.specs2.matcher.{DisjunctionMatchers, MatchResultCombinators, ResultMatchers, ValidationMatchers}
//import model._
//import zio._
//import svbench._

// Resources:
// https://wiki.openoffice.org/wiki/Calc/API/Sheet_Operations

class SOfficeSpec extends Spec with ValidationMatchers with ResultMatchers with MatchResultCombinators with DisjunctionMatchers { def is =

  s2"""
    SOffice should
      create empty Spreadsheet and save s1
      rename a sheet s2
      copy a sheet s3
      save a OpenOffice document s4
      duplicate the sheets of Truvari Open Office Template s5
      Change Reference of a Cell from one Sheet to another s6
      Clone Truvari Sheet template into a new one $s7
      try to move sheets from template into new Sheet s3
  """

  val tmp2 = "/home/yoda/tmp/"
  val soffice2 = "/nix/store/zv3shimhh16nm7w7raw492ii9vzprl68-libreoffice-6.1.5.2/bin/soffice"
  val template2 = "/home/yoda/tmp/truvari_test.ods"

  def s1 =  {
    
    val ctx = SOffice.createContext()
    val doc = SOffice.openEmptyDoc(ctx)
    SOffice.save("/home/obiwan/tmp/OO.ods", doc)

    ok

  }

  def s2 = {
    val ctx = SOffice.createContext()
    val report = SOffice.openEmptyDoc(ctx)
    SOffice.renameSheet("Sheet1", "MySheet", report)
    ok
  }

  def s3 = {
    val ctx = SOffice.createContext()
    val report = SOffice.openEmptyDoc(ctx)
    SOffice.copySheet("Sheet1", "MySheet", 1, report)
    ok
  }

  def s4 = {
    val ctx = SOffice.createContext(soffice2)
    val report = SOffice.openEmptyDoc(ctx)
    SOffice.copySheet("Sheet1", "MySheet", 1, report)
    SOffice.save(tmp2 + "MySheet.ods", report)
    ok
  }

  def s5 = {
    val ctx = SOffice.createContext(soffice2)
    val template = SOffice.openDoc(template2, ctx)
    SOffice.copySheet(Truvari.RAW_DATA, "RawData2", 2, template)
    SOffice.copySheet(Truvari.FORMATTED, "Formatted2", 3, template)

    SOffice.save(tmp2 + "RawData2.ods", template)
    ok
  }

  def s6 = {
    val ctx = SOffice.createContext(soffice2)
    val rawData2 = SOffice.openDoc(tmp2 + "RawData2.ods", ctx)
    SOffice.insertIntoCell(0, 19, "=$RawData2.A8", SOffice.getSheetByName(rawData2, "Formatted2"), "")
    SOffice.save(tmp2 + "RawData3.ods", rawData2)
    ok
  }

  def s7 = {
    val ctx = SOffice.createContext(soffice2)
    val template = SOffice.openDoc(template2, ctx)
    Truvari.duplicateReportTemplate(
      "RawData2",
      "Formatted2",
      2,
      template
    )
    SOffice.save(tmp2 + "TruvariDup.ods", template)
    ok
  }

  def s14 = {
    // val ctx = SOffice.createContext()

    // val template = SOffice.openDoc("/workspace/templates/truvari_templates.ods", ctx)

    // val report = SOffice.openEmptyDoc(ctx)
    
    // Truvari.insertData(
    //   giabPath = "/home/obiwan/stash/workspace/results/tomato/truvari_results_DEL/manta_Diag-wgs1-HG002C2350bp-400M_diploidSV/giab_report.txt",
    //   reportPath = "/home/obiwan/tmp/report.ods",
    //   template,
    //   report
    // )


    // SOffice.closeDoc(
    //   template
    // )

    // SOffice.closeDoc(
    //   report
    // )

    ok
  }

}
