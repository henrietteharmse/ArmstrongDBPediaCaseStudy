package org.armstrong.poc.dbpedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DetermineS extends ProcessStep {
  private static Logger logger = LoggerFactory.getLogger(DetermineS.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String RDF_TYPE_IRI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  public static final String SHEET_S = "S";
  
  
  public DetermineS(String strFile) {
    super(strFile);
  }

  @Override
  public void read() {
    Workbook workbook = null;
    try {
      FileInputStream inputStream = new FileInputStream(new File(strFile));
      workbook = WorkbookFactory.create(inputStream);
      Sheet sheetC = workbook.getSheetAt(0);
      
      Iterator<Row> rowIterator = sheetC.iterator();
      if (!rowIterator.hasNext()) {
        logger.error("First row of first sheet of " + strFile + " is empty.");
      } else {
        Row row = rowIterator.next();
        
        Iterator<Cell> cellIterator = row.cellIterator();
        if (!cellIterator.hasNext()) {
          logger.error("First cell of first sheet of " + strFile + " is empty.");
        } else {
          Cell cell = cellIterator.next();
          
          strC = cell.getStringCellValue();
          
          logger.debug("strC = " + strC);  
        }
      }
      
      Sheet sheetS = workbook.getSheet(SHEET_S);
      if (sheetS != null) {
        isResultReady = true;
      }
      workbook.close();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
      if (workbook != null)
        try {
          workbook.close();
        } catch (IOException e) {
          logger.error(WTF_MARKER, e.getMessage(), e);
        }
    }
  }
  
  @Override
  public void write() {
    writeSet(SHEET_S, setS);
  }
  
  private ParameterizedSparqlString determineQuery() {
    ParameterizedSparqlString pss = new ParameterizedSparqlString();
    
    StringBuilder query = new StringBuilder();
    
    query.append("SELECT DISTINCT ?p WHERE {");
    query.append("?s ?type ?t");
    query.append(" .");
    query.append("?s ?p ?o . }");
    
    logger.trace("query = " + query);
    
    pss.setCommandText(query.toString());
    pss.setIri("type", RDF_TYPE_IRI);
    pss.setIri("t", strC) ;

    return pss;      
  }
  
  public boolean resultReady() {
    if (isProcessed || isResultReady) {
      return true;
    } else {
      return false;
    }
  }
  
  @Override
  public void process() {
    if (resultReady()) {
      return;
    }
    
    QueryExecution qe = QueryExecutionFactory.sparqlService(ACTUAL_SPARQL_ENDPOINT, determineQuery().asQuery());

    setS = new HashSet<String>();
    for (ResultSet results = qe.execSelect(); results.hasNext();) {
      QuerySolution qs = results.next();
      String strValue = qs.get("?p").toString();
      if (!strValue.equals(RDF_TYPE_IRI))
        setS.add(strValue);
      logger.trace("value = " + strValue);
    }
 
    qe.close();
    isProcessed = true;
  }

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Please specify input .xlsx file.");
      return;
    }
    
    Path path = Paths.get(".").toAbsolutePath().normalize();
    String strFile  = path.toFile().getAbsolutePath() + LOCATION + args[0];
    
    DetermineS determineS = new DetermineS(strFile);
    try {
      determineS.execute();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  }
}
