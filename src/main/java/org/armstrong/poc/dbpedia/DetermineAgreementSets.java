package org.armstrong.poc.dbpedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
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

import com.google.common.collect.Sets;

public class DetermineAgreementSets extends ProcessStep {
  
  private static Logger logger = LoggerFactory.getLogger(DetermineAgreementSets.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String SHEET_AGREEMENT_SETS = "AgreementSets";
  
  
  public DetermineAgreementSets(String strFile) {
    super(strFile);
  }  
  
  @Override
  public void read() {
    setS = readSet(DetermineS.SHEET_S, SHEET_AGREEMENT_SETS, setS);
  }
  
  @Override
  public void write() {
    writeSetOfSets(SHEET_AGREEMENT_SETS, agreementSets);
  }  
  
  @Override
  public void process() {
    if (resultReady()) {
      return;
    }
    
    Set<Set<String>> powersetOfS = Sets.powerSet(setS);
    Map<String, String> iriToQueryParamMap = QueryUtils.generateQueryParameters(setS);
    agreementSets = new HashSet<Set<String>>();
    
    int nProgressTracker = 0;
    for (Set<String> set : powersetOfS) {  
      if (!set.isEmpty()) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("ASK WHERE {");    
        QueryUtils.querySubjectPredicateObject("c1", QueryUtils.RDF_TYPE, QueryUtils.TYPE, queryBuilder);
        QueryUtils.querySubjectPredicateObject("c2", QueryUtils.RDF_TYPE, QueryUtils.TYPE, queryBuilder);
        pss.setIri(QueryUtils.RDF_TYPE, DetermineS.RDF_TYPE_IRI);
        pss.setIri(QueryUtils.TYPE, strC);
        queryBuilder.append("FILTER (?c1 != ?c2)");
        for (String component : set) {
          String componentQueryVar = iriToQueryParamMap.get(component);
          String componentIRIQueryVar = componentQueryVar.concat("IRI");
          queryC1CompEqC2(componentIRIQueryVar, componentQueryVar, queryBuilder);
          pss.setIri(componentIRIQueryVar, component);
          logger.trace("componentIRIQueryVar = " + componentIRIQueryVar);
          logger.trace("component = " + component);
          logger.trace("componentQueryVar = " + componentQueryVar);
          
        }      
        queryBuilder.append("} LIMIT 1");
        pss.setCommandText(queryBuilder.toString()); 
        
        QueryExecution qe = QueryExecutionFactory.sparqlService(ACTUAL_SPARQL_ENDPOINT, pss.asQuery());
        logger.debug("About to run query: " + pss.toString());
        long nStartTime = System.currentTimeMillis();  
        
        qe.setTimeout(-1, -1);
        boolean bFound = qe.execAsk();
        
        long nStopTime = System.currentTimeMillis();
        logger.debug("query done");        
        logger.debug("query duration = " + (nStopTime - nStartTime)/1000/60 + " minutes or " + (nStopTime - nStartTime)/1000 + " seconds.");
        
        if (bFound) {
          agreementSets.add(set);
        }
        
        qe.close();
      }
      nProgressTracker++;
      logger.debug("ProgressTracker = " + nProgressTracker);
    }
    isProcessed = true;
  }
  

  private void queryC1CompEqC2(String componentIRI, String component, StringBuilder queryBuilder) {
    queryBuilder.append("?c1 ?");
    queryBuilder.append(componentIRI);
    queryBuilder.append(" ?");
    queryBuilder.append(component);
    queryBuilder.append(" .");
    queryBuilder.append("?c2 ?");
    queryBuilder.append(componentIRI);
    queryBuilder.append(" ?");
    queryBuilder.append(component);
    queryBuilder.append(" .");
  }
  
  @Override
  public void execute() {
    DetermineX determineX = new DetermineX(strFile);
    determineX.execute();
    
    strC = determineX.getStrC();
    setS = determineX.getSetS();
    setX = determineX.getSetX();
    
    super.execute();
  }
    
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Please specify input .xlsx file.");
      return;
    }    
    Path path = Paths.get(".").toAbsolutePath().normalize();
    String strFile  = path.toFile().getAbsolutePath() + LOCATION + args[0];

    try {
      DetermineAgreementSets determineAgreementSets = new DetermineAgreementSets(strFile);
      determineAgreementSets.execute();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  }
}
