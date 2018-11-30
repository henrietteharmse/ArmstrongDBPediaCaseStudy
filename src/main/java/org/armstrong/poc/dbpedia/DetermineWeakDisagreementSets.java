package org.armstrong.poc.dbpedia;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DetermineWeakDisagreementSets extends ProcessStep {
  
  private static Logger logger = LoggerFactory.getLogger(DetermineWeakDisagreementSets.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String SHEET_DISAGREEMENT_SETS = "WeakDisagreementSets";
  
  public DetermineWeakDisagreementSets(String strFile) {
    super(strFile);
  } 
  
  @Override
  public void read() {
    maximalStrongAgreementSets = readSetOfSets(DetermineStrongMaximalAgreementSets.SHEET_MAXIMAL_AGREEMENT_SETS, 
        SHEET_DISAGREEMENT_SETS, maximalStrongAgreementSets);
  }  
  
  @Override
  public void process() {
    if (resultReady()) {
      return;
    }
    
    weakDisagreementSets = new HashSet<Set<String>>();
    for (Set<String> agreementSet : maximalStrongAgreementSets) {
      Set<String> disagreementSet = new HashSet<String>();
      disagreementSet.addAll(setS);
      disagreementSet.removeAll(agreementSet);
      weakDisagreementSets.add(disagreementSet);
    }
    isProcessed = true;
  }
  
  @Override
  public void write() {
    writeSetOfSets(SHEET_DISAGREEMENT_SETS, weakDisagreementSets);
  }
  
  @Override
  public void execute() {
    DetermineStrongMaximalAgreementSets determineMaximalAgreementSets = new DetermineStrongMaximalAgreementSets(strFile);
    determineMaximalAgreementSets.execute();
    
    strC = determineMaximalAgreementSets.getStrC();
    setS = determineMaximalAgreementSets.getSetS();
    strongAgreementSets = determineMaximalAgreementSets.getStrongAgreementSets();
    maximalStrongAgreementSets = determineMaximalAgreementSets.getMaximalStrongAgreementSets();
    
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
      DetermineWeakDisagreementSets determineDisagreementSets = new DetermineWeakDisagreementSets(strFile);
      determineDisagreementSets.execute();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  }
}
