package org.armstrong.poc.dbpedia;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DetermineX extends ProcessStep {
  private static Logger logger = LoggerFactory.getLogger(DetermineX.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String RDF_TYPE_IRI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  public static final String SHEET_X = "X";
  
  
  public DetermineX(String strFile) {
    super(strFile);
  }

  @Override
  public void read() {
    setS = readSet(DetermineS.SHEET_S, SHEET_X, setS);
  }
  
  @Override
  public void write() {
    writeSet(SHEET_X, setX);
  }
  
  private ParameterizedSparqlString determineQuery(String predicate) {
    ParameterizedSparqlString pss = new ParameterizedSparqlString();
    
    StringBuilder query = new StringBuilder();
    
    query.append("ASK WHERE {");
    query.append("?s ?type ?t");
    query.append(" .");
    query.append("OPTIONAL {?s ?p ?o . }");
    query.append("FILTER(!BOUND(?o)) }");
    
    logger.trace("query = " + query);
    
    pss.setCommandText(query.toString());
    pss.setIri("type", RDF_TYPE_IRI);
    pss.setIri("t", strC) ;
    pss.setIri("p", predicate);

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
    
    setX = new HashSet<String>(setS);
    Set<String> setH = new HashSet<String>();
    
    for (String component : setS) {
      QueryExecution qe = QueryExecutionFactory.sparqlService(ACTUAL_SPARQL_ENDPOINT, determineQuery(component).asQuery());
      boolean bFound = qe.execAsk();
      if (bFound) {
        setH.add(component);
      }
      qe.close();
    }

    setX.removeAll(setH);
    isProcessed = true;
  }

  @Override
  public void execute() {
    DetermineS determineS = new DetermineS(strFile);
    determineS.execute();
    
    strC = determineS.getStrC();
    setS = determineS.getSetS();
    
    super.execute();
  }
  
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Please specify input .xlsx file.");
      return;
    }
    
    Path path = Paths.get(".").toAbsolutePath().normalize();
    String strFile  = path.toFile().getAbsolutePath() + LOCATION + args[0];
    
    DetermineX determineX = new DetermineX(strFile);
    try {
      determineX.execute();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  }
}
