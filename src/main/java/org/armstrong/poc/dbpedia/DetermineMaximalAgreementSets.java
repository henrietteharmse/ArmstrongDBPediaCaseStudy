package org.armstrong.poc.dbpedia;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DetermineMaximalAgreementSets extends ProcessStep {
  private static Logger logger = LoggerFactory.getLogger(DetermineMaximalAgreementSets.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String SHEET_MAXIMAL_AGREEMENT_SETS = "MaximalAgreementSets";
  
  public DetermineMaximalAgreementSets(String strFile) {
    super(strFile);
  }

  @Override
  public void read() {
    agreementSets = readSetOfSets(DetermineAgreementSets.SHEET_AGREEMENT_SETS, SHEET_MAXIMAL_AGREEMENT_SETS, agreementSets);  
  }

  @Override
  public void process() {
    if (resultReady()) {
      return;
    }
    
    maximalAgreementSets = new HashSet<Set<String>>();
    maximalAgreementSets.addAll(agreementSets);
    
    for (Set<String> agreementSet1 : agreementSets) {
      Set<Set<String>> agreementSetsToRemove = new HashSet<Set<String>>();
      for (Set<String> agreementSet2 : maximalAgreementSets) {
        if (!agreementSet1.equals(agreementSet2)) {
          if (agreementSet1.containsAll(agreementSet2)) {
            agreementSetsToRemove.add(agreementSet2);
          }
        }
      }
      if (!agreementSetsToRemove.isEmpty()) {
        maximalAgreementSets.removeAll(agreementSetsToRemove);
      }
    }
    isProcessed = true;
  }

  @Override
  public void write() {
    writeSetOfSets(SHEET_MAXIMAL_AGREEMENT_SETS, maximalAgreementSets);    
  }

  @Override
  public void execute() {
    DetermineAgreementSets determineAgreementSet = new DetermineAgreementSets(strFile);
    determineAgreementSet.execute();
    
    strC = determineAgreementSet.getStrC();
    setS = determineAgreementSet.getSetS();
    agreementSets = determineAgreementSet.getAgreementSets();
    
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
      DetermineMaximalAgreementSets determineMaximalAgreementSets = new DetermineMaximalAgreementSets(strFile);
      determineMaximalAgreementSets.execute();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  } 
}
