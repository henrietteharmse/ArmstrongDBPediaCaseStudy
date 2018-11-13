package org.armstrong.poc.dbpedia;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DetermineDisagreementSets extends ProcessStep {
  
  private static Logger logger = LoggerFactory.getLogger(DetermineDisagreementSets.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String SHEET_DISAGREEMENT_SETS = "DisagreementSets";
  
  public DetermineDisagreementSets(String strFile) {
    super(strFile);
  } 
  
  @Override
  public void read() {
    maximalAgreementSets = readSetOfSets(DetermineMaximalAgreementSets.SHEET_MAXIMAL_AGREEMENT_SETS, 
        SHEET_DISAGREEMENT_SETS, maximalAgreementSets);
  }  
  
  @Override
  public void process() {
    if (resultReady()) {
      return;
    }
    
    disagreementSets = new HashSet<Set<String>>();
    for (Set<String> agreementSet : maximalAgreementSets) {
      Set<String> disagreementSet = new HashSet<String>();
      disagreementSet.addAll(setS);
      disagreementSet.removeAll(agreementSet);
      disagreementSets.add(disagreementSet);
    }
    isProcessed = true;
  }
  
  @Override
  public void write() {
    writeSetOfSets(SHEET_DISAGREEMENT_SETS, disagreementSets);
  }
  
  @Override
  public void execute() {
    DetermineMaximalAgreementSets determineMaximalAgreementSets = new DetermineMaximalAgreementSets(strFile);
    determineMaximalAgreementSets.execute();
    
    strC = determineMaximalAgreementSets.getStrC();
    setS = determineMaximalAgreementSets.getSetS();
    agreementSets = determineMaximalAgreementSets.getAgreementSets();
    maximalAgreementSets = determineMaximalAgreementSets.getMaximalAgreementSets();
    
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
      DetermineDisagreementSets determineDisagreementSets = new DetermineDisagreementSets(strFile);
      determineDisagreementSets.execute();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  }
}
