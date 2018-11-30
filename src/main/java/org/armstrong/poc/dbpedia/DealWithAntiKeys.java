package org.armstrong.poc.dbpedia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DealWithAntiKeys extends ProcessStep {
  private static Logger logger = LoggerFactory.getLogger(DealWithAntiKeys.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String ANTI_KEYS_STATEMENTS_FILE = "/src/main/resources/AntiKeyStatements.ttl";
  
  public DealWithAntiKeys(String strFile) {
    super(strFile);
  }

  @Override
  public void read() {
    antiKeys = readSetOfSets(DetermineAntiKeys.SHEET_ANTI_KEYS, antiKeys); 
  }
  
  
  @Override
  public void process() {
    if (resultReady()) {
      return;
    }
    
    Map<String, String> iriToQueryParamMap = QueryUtils.generateQueryParameters(setS);
    try {
      for (Set<String> antiKey : antiKeys) {  
        if (!antiKey.isEmpty()) {
          ParameterizedSparqlString pss = new ParameterizedSparqlString();
          StringBuilder queryBuilder = new StringBuilder();
          queryBuilder.append("CONSTRUCT {");    
          QueryUtils.querySubjectPredicateObject("c1", QueryUtils.RDF_TYPE, QueryUtils.TYPE, queryBuilder);
          QueryUtils.querySubjectPredicateObject("c2", QueryUtils.RDF_TYPE, QueryUtils.TYPE, queryBuilder);          
          QueryUtils.generateQueryForTuple1("c1", setS, iriToQueryParamMap, queryBuilder, pss);
          QueryUtils.generateQueryForTuple2("c2", setS, antiKey, iriToQueryParamMap, queryBuilder, pss);         
          queryBuilder.append("} WHERE { SELECT ?c1 ?c2 ");
          QueryUtils.generateWhereParametersForTuple1("c1", setS, iriToQueryParamMap, queryBuilder);
          QueryUtils.generateWhereParametersForTuple2("c2", setS, antiKey, iriToQueryParamMap, queryBuilder);
          queryBuilder.append("{");
          QueryUtils.querySubjectPredicateObject("c1", QueryUtils.RDF_TYPE, QueryUtils.TYPE, queryBuilder);
          QueryUtils.querySubjectPredicateObject("c2", QueryUtils.RDF_TYPE, QueryUtils.TYPE, queryBuilder);
          queryBuilder.append(" FILTER (?c1 != ?c2) "); 
          QueryUtils.generateOptionalQuery("c1", "c2", setS, antiKey, iriToQueryParamMap, queryBuilder, pss);
          queryBuilder.append("} LIMIT 1 }");
          
          pss.setCommandText(queryBuilder.toString()); 
          
          pss.setIri(QueryUtils.RDF_TYPE, DetermineS.RDF_TYPE_IRI);
          pss.setIri(QueryUtils.TYPE, strC);
          pss.setIri("service", ACTUAL_SPARQL_ENDPOINT);
          
          logger.debug("About to run query: " + pss.toString());
          
          QueryExecution qe = QueryExecutionFactory.sparqlService(ACTUAL_SPARQL_ENDPOINT, pss.asQuery());
          logger.debug("About to run query: " + pss.toString());
          long nStartTime = System.currentTimeMillis();  
          
          qe.setTimeout(-1, -1);
          Dataset dataSet = qe.execConstructDataset();
          long nStopTime = System.currentTimeMillis();
          logger.debug("query done");        
          logger.debug("query duration = " + (nStopTime - nStartTime)/1000/60 + " minutes or " + (nStopTime - nStartTime)/1000 + " seconds.");

          logger.debug("Is DataSet empty  = " + dataSet.isEmpty());
          
          Path path = Paths.get(".").toAbsolutePath().normalize();
          String strOutputFile = path.toFile().getAbsolutePath() + ANTI_KEYS_STATEMENTS_FILE;
          File outputFile = new File(strOutputFile);
          OutputStream outputStream = new FileOutputStream(outputFile, true);

          DatasetGraph dsg = dataSet.asDatasetGraph();

          RDFDataMgr.writeQuads(outputStream, dsg.find());
          outputStream.flush();
          outputStream.close();
        }
      }
    } catch (Throwable t) {      
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
    
    isProcessed = true;  
  }

  @Override
  public void write() {
    // TODO Auto-generated method stub

  }
  
  @Override
  public void execute() {
    DealWithFirstTuple determineFirstTuple = new DealWithFirstTuple(strFile);
    determineFirstTuple.execute();
    
    strC = determineFirstTuple.getStrC();
    setS = determineFirstTuple.getSetS();
    setX = determineFirstTuple.getSetX();
    strongAgreementSets = determineFirstTuple.getStrongAgreementSets();
    maximalStrongAgreementSets = determineFirstTuple.getMaximalStrongAgreementSets();
    weakDisagreementSets = determineFirstTuple.getWeakDisagreementSets();
    necessaryDisagreementSets = determineFirstTuple.getNecessaryDisagreementSets();
    uniquenessConstraints = determineFirstTuple.getUniquenessConstraints();
    antiKeys = determineFirstTuple.getAntiKeys();
    
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
      DealWithAntiKeys dealWithAntiKeys = new DealWithAntiKeys(strFile);
      dealWithAntiKeys.execute();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  } 
}
