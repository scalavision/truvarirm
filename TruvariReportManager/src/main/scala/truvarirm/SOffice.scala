package truvarirm

import com.sun.star.uno._
import com.sun.star.lang._
import com.sun.star.frame._
import com.sun.star.beans._
import com.sun.star.sheet._
import com.sun.star.table._
import com.sun.star.util._
import scala.util._
import com.sun.star.container._

/** SOffice
 * 
 *  More or less copied directly from Open Office documentation and examples 
 *  (needs to be way more Scala idiomatic to be useful in the long run)
 *  
 *  Some useful resources with examples:
 * 
 *  - https://fivedots.coe.psu.ac.th/~ad/jlop/
 *  - https://fivedots.coe.psu.ac.th/~ad/jlop/chaps/19.%20Calc%20API%20Overview.pdf
 *  - https://wiki.openoffice.org/wiki/Calc/API/Sheet_Operations  
 *  - https://wiki.openoffice.org/wiki/API/Samples/Java
 *  - https://api.libreoffice.org/examples/java/Spreadsheet/
 *  - https://api.libreoffice.org/examples/examples.html#Java_examples
 *  - https://wiki.openoffice.org/wiki/Documentation/DevGuide/ProUNO/Java/Java_Language_Binding
 * 
 *  More general resources and background information:
 * 
 *  - https://wiki.openoffice.org/wiki/Documentation/DevGuide/OpenOffice.org_Developers_Guide
 *  - https://www.libreoffice.org/community/developers/
 *  - https://cgit.freedesktop.org/libreoffice
*   - https://www.openoffice.org/api/docs/common/ref/com/sun/star/module-ix.html
 *  - https://api.libreoffice.org/docs/idl/ref/index.html
 *  - https://api.libreoffice.org/docs/java/ref/index.html
 *  - https://www.openoffice.org/udk/common/man/concept/uno_contexts.html
 *  - https://wiki.openoffice.org/wiki/Documentation/DevGuide/ProUNO/Component_Context
 *  - https://wiki.openoffice.org/wiki/Documentation
 *  - https://wiki.openoffice.org/wiki/OpenOffice_NetBeans_Integration#NetBeans_8.x_and_Apache_OpenOffice_4.1.x
 * 
 *  As a sidenote I'd say that LibreOffice seems to be way more up to date than OpenOffice
 */

object SOffice {

  val UNKNOWN = 0;
  val WRITER = 1;
  val BASE = 2;
  val CALC = 3;
  val DRAW = 4;
  val IMPRESS = 5;
  val MATH = 6;

  // docType strings
  val UNKNOWN_STR = "unknown";
  val WRITER_STR = "swriter";
  val BASE_STR = "sbase";
  val CALC_STR = "scalc";
  val DRAW_STR = "sdraw";
  val IMPRESS_STR = "simpress";
  val MATH_STR = "smath";

  // docType service names
  val UNKNOWN_SERVICE = "com.sun.frame.XModel";
  val WRITER_SERVICE = "com.sun.star.text.TextDocument";
  val BASE_SERVICE = "com.sun.star.sdb.OfficeDatabaseDocument";
  val CALC_SERVICE = "com.sun.star.sheet.SpreadsheetDocument";
  val DRAW_SERVICE = "com.sun.star.drawing.DrawingDocument";
  val IMPRESS_SERVICE = "com.sun.star.presentation.PresentationDocument";
  val MATH_SERVICE = "com.sun.star.formula.FormulaProperties";

  // connect to locally running Office via port 8100
  val SOCKET_PORT = 8100;

  // CLSIDs for Office documents
  // defined in <OFFICE>\officecfg\registry\data\org\openoffice\Office\Embedding.xcu
  val WRITER_CLSID = "8BC6B165-B1B2-4EDD-aa47-dae2ee689dd6";
  val CALC_CLSID = "47BBB4CB-CE4C-4E80-a591-42d9ae74950f";
  val DRAW_CLSID = "4BAB8970-8A3B-45B3-991c-cbeeac6bd5e3";
  val IMPRESS_CLSID = "9176E48A-637A-4D1F-803b-99d9bfac1047";
  val MATH_CLSID = "078B7ABA-54FC-457F-8551-6147e776a997";
  val CHART_CLSID = "12DCAE26-281F-416F-a234-c3086127382e";

  private def isOpenable(fnm: String): Boolean =
  // convert a file path to URL format
  {
     val f = new java.io.File(fnm);
     if (!f.exists()) {
       println(fnm + " does not exist");
       throw new Exception()
     }
     if (!f.isFile()) {
       println(fnm + " is not a file");
       throw new Exception()
     }
     if (!f.canRead()) {
       System.out.println(fnm + " is not readable");
       throw new Exception()
     }

     true
  } // end of isOpenable()

  private def fnmToURL(
    fnm: String
  ): String = {

    var url: String = null

    try {
      var sb: StringBuffer = new StringBuffer("")
      val path: String = new java.io.File(fnm).getCanonicalPath()
      sb = new StringBuffer("file:///")
      sb.append(path.replace('\\', '/'))
      url = sb.toString()
    }
    catch {
      case  e: java.io.IOException  =>
        println("Could not access " + fnm + " with following error: " + e);
        throw new Exception()
    } 

    url

  }

  private def getContextLoader(
    xContext: XComponentContext
  ): XComponentLoader = {

    val sm = xContext.getServiceManager()

    val desktop = sm.createInstanceWithContext(
      "com.sun.star.frame.Desktop", xContext
    )

    UnoRuntime.queryInterface(
      classOf[XComponentLoader], desktop
    )

  }

  def createContext(
    pathToOOExecutable: String = "/nix/store/ixxh6s1j1xqn7pbby4ijjl08ga7lcxyk-libreoffice-6.0.7.3/bin/soffice"
  ): XComponentContext = Try(
    ooo.connector.BootstrapSocketConnector.bootstrap(pathToOOExecutable) 
  ).get
  
  def openEmptyDoc(
    xContext: XComponentContext
  ): XSpreadsheetDocument = {
    
    val xLoader = getContextLoader(xContext)
    val strDoc = "private:factory/scalc"
    val xComp = xLoader.loadComponentFromURL(strDoc, "_blank", 0, Array.empty[PropertyValue])
    UnoRuntime.queryInterface(classOf[XSpreadsheetDocument], xComp)

  }

  def openDoc(
    fnm: String,
    xContext: XComponentContext,
    props: Array[PropertyValue] = Array.empty[PropertyValue]
  ): XSpreadsheetDocument = {

    val loader = getContextLoader(xContext)

    assert(fnm != null, "Filename must not be empty")
    var doc: XComponent = null

    if(isOpenable(fnm)) {

      try {
        doc = loader.loadComponentFromURL(fnmToURL(fnm), "_blank", 0, props)
      }
      catch {
        case e: java.net.MalformedURLException  => 
          println("MaformedURLException" + e.toString())
        case e: java.net.URISyntaxException =>
          println("MaformedURLException" + e.toString())
      }

    }

    UnoRuntime.queryInterface(classOf[XSpreadsheetDocument], doc)

  }

  def save(path: String, odoc: XSpreadsheetDocument): Unit = {

    val xStorable: XStorable = UnoRuntime.queryInterface(classOf[XStorable], odoc)
    
    val sourceFile = new java.io.File(path)

    sourceFile.setWritable(true)

    val sSaveUrl: StringBuffer = new StringBuffer("file:///");

    sSaveUrl.append(sourceFile.getCanonicalPath().replace('\\', '/'));
    xStorable.storeAsURL( sSaveUrl.toString(), Array.empty[PropertyValue]);

  }

  def saveDoc(odoc: Any, path: String): Unit = {
    val store: XStorable = UnoRuntime.queryInterface(classOf[XStorable], odoc)
    store.storeToURL(path, Array.empty[PropertyValue])
  }

  def insertSheet(doc: XSpreadsheetDocument, name: String, index: Short): XSpreadsheet = 
    UnoRuntime.queryInterface(
      classOf[XSpreadsheet], 
      doc.getSheets().insertNewByName(name, index)
    )

  def closeDoc(doc: XSpreadsheetDocument): Unit = {
    val xStorable: XStorable = UnoRuntime.queryInterface(classOf[XStorable], doc)
    val closable = UnoRuntime.queryInterface(classOf[XCloseable], xStorable)
    if(null != closable){
      val xComp = UnoRuntime.queryInterface(classOf[XComponent], closable)
      xComp.dispose()
    } else {
      throw new Exception("Not able to find the closable object")
    }
  }
  
  def getXModel(doc: XSpreadsheetDocument): XModel = {
    UnoRuntime.queryInterface(classOf[XModel], doc)
  }

  def getController(doc: XSpreadsheetDocument): XController = {
    getXModel(doc).getCurrentController()
  }

  def getView(doc: XSpreadsheetDocument): XSpreadsheetView =
    UnoRuntime.queryInterface(classOf[XSpreadsheetView], getController(doc))

  def getSheetByName(
    doc: XSpreadsheetDocument,
    name: String
  ) : XSpreadsheet = 
    UnoRuntime.queryInterface(classOf[XSpreadsheet], doc.getSheets.getByName(name))

  def getXNamed(
    name: String,
    doc: XSpreadsheetDocument
  ) : XNamed = {
    UnoRuntime.queryInterface(classOf[XNamed], doc.getSheets().getByName(name))
  }

  def renameSheet(
    name: String,
    newName: String,
    doc: XSpreadsheetDocument
  ): Unit = 
    getXNamed( name, doc).setName(newName)
  
  def copySheet(
    origSheetName: String,
    newSheetName: String,
    index: Short,
    doc: XSpreadsheetDocument
  ): Unit = {
    doc.getSheets().copyByName(origSheetName, newSheetName, index)
  }

  def deleteSheet(
    sheetName: String,
    doc: XSpreadsheetDocument
  ): Unit = {
    doc.getSheets.removeByName(sheetName)
  }

  def setActiveSheet(doc: XSpreadsheetDocument, sheet: XSpreadsheet): Unit =
    getView(doc).setActiveSheet(sheet)

  def getActiveSheet(doc: XSpreadsheetDocument) : XSpreadsheet = 
    getView(doc).getActiveSheet()

  def getViewData(doc: XSpreadsheetDocument) : String = 
    getController(doc).getViewData().asInstanceOf[String]

  def setViewData(doc: XSpreadsheetDocument, viewData: String) : Unit = 
    getController(doc).restoreViewData(viewData)
 
  def insertRow(sheet: XSpreadsheet, index: Int) : Unit = {
    val crRange: XColumnRowRange = UnoRuntime.queryInterface(classOf[XColumnRowRange], sheet)
    val rows: XTableRows = crRange.getRows()
    rows.insertByIndex(index, 1)
  }
  
  def insertColumn(sheet: XSpreadsheet, idx: Int): Unit = 
  {
    val crRange: XColumnRowRange = UnoRuntime.queryInterface(classOf[XColumnRowRange], sheet)
    val cols: XTableColumns = crRange.getColumns()
    cols.insertByIndex(idx, 1)   // add 1 column at idx position
  }

  def getAddress(cellRange: XCellRange):  CellRangeAddress = 
  {
    val addr: XCellRangeAddressable = UnoRuntime.queryInterface(classOf[XCellRangeAddressable], cellRange)
    addr.getRangeAddress();
  }  // end of getAddress()

  
  def insertCells(sheet: XSpreadsheet ,  cellRange: XCellRange,isShiftRight: Boolean): Unit = 
  { 
    val mover: XCellRangeMovement = UnoRuntime.queryInterface(classOf[XCellRangeMovement], sheet);
    val addr: CellRangeAddress = getAddress(cellRange);
    if (isShiftRight)
      mover.insertCells(addr, CellInsertMode.RIGHT);
    else   // move old cells down
      mover.insertCells(addr, CellInsertMode.DOWN);
  }  // end of insertCells()

    
  def insertIntoCell(cellX: Int, cellY: Int, value: String, spreadSheet: XSpreadsheet, flag: String): Unit = {

    var xCell: XCell = null
     
    try {
       xCell = spreadSheet.getCellByPosition(cellX, cellY) 
    } catch {
      case ex: com.sun.star.lang.IndexOutOfBoundsException =>
        println(s"could not get Cell $cellX, $cellY")
        println(s"$ex")
        throw new Exception()
    }

    if(flag.equals("V")) {
      xCell.setValue(value.toDouble)
    } else
      xCell.setFormula(value)

  }

}
