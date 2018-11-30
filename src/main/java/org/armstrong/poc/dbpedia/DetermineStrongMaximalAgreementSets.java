package org.armstrong.poc.dbpedia;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DetermineStrongMaximalAgreementSets extends ProcessStep {
  private static Logger logger = LoggerFactory.getLogger(DetermineStrongMaximalAgreementSets.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String SHEET_MAXIMAL_AGREEMENT_SETS = "MaximalStrongAgreementSets";
  
  public DetermineStrongMaximalAgreementSets(String strFile) {
    super(strFile);
  }

  @Override
  public void read() {
    strongAgreementSets = readSetOfSets(DetermineStrongAgreementSets.SHEET_AGREEMENT_SETS, SHEET_MAXIMAL_AGREEMENT_SETS, strongAgreementSets);  
  }

  @Override
  public void process() {
    if (resultReady()) {
      return;
    }
    
    maximalStrongAgreementSets = new HashSet<Set<String>>();
    maximalStrongAgreementSets.addAll(strongAgreementSets);
    
    for (Set<String> agreementSet1 : strongAgreementSets) {
      Set<Set<String>> agreementSetsToRemove = new HashSet<Set<String>>();
      for (Set<String> agreementSet2 : maximalStrongAgreementSets) {
        if (!agreementSet1.equals(agreementSet2)) {
          if (agreementSet1.containsAll(agreementSet2)) {
            agreementSetsToRemove.add(agreementSet2);
          }
        }
      }
      if (!agreementSetsToRemove.isEmpty()) {
        maximalStrongAgreementSets.removeAll(agreementSetsToRemove);
      }
    }
    isProcessed = true;
  }

  @Override
  public void write() {
    writeSetOfSets(SHEET_MAXIMAL_AGREEMENT_SETS, maximalStrongAgreementSets);    
  }

  @Override
  public void execute() {
    DetermineStrongAgreementSets determineAgreementSet = new DetermineStrongAgreementSets(strFile);
    determineAgreementSet.execute();
    
    strC = determineAgreementSet.getStrC();
    setS = determineAgreementSet.getSetS();
    strongAgreementSets = determineAgreementSet.getStrongAgreementSets();
    
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
      DetermineStrongMaximalAgreementSets determineMaximalAgreementSets = new DetermineStrongMaximalAgreementSets(strFile);
      determineMaximalAgreementSets.execute();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  } 
}
