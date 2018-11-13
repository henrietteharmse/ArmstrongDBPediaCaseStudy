package org.armstrong.poc.dbpedia;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DetermineNecessaryDisagreementSets extends ProcessStep {
  private static Logger logger = LoggerFactory.getLogger(DetermineNecessaryDisagreementSets.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String SHEET_NECESSARY_DISAGREEMENT_SETS = "NecessaryDisagreementSets";
  
  public DetermineNecessaryDisagreementSets(String strFile) {
    super(strFile);
  }

  @Override
  public void read() {
    disagreementSets = readSetOfSets(DetermineDisagreementSets.SHEET_DISAGREEMENT_SETS, 
        SHEET_NECESSARY_DISAGREEMENT_SETS, disagreementSets);
    
  }

  @Override
  public void process() {
    if (resultReady()) {
      return;
    }
    
    necessaryDisagreementSets = new HashSet<Set<String>>();
    necessaryDisagreementSets.addAll(disagreementSets);
    
    for (Set<String> disagreementSet1 : disagreementSets) {
      Set<Set<String>> disagreementSetsToRemove = new HashSet<Set<String>>();
      for (Set<String> disagreementSet2 : necessaryDisagreementSets) {
        if (!disagreementSet1.equals(disagreementSet2)) {
          if (disagreementSet2.containsAll(disagreementSet1)) {
            disagreementSetsToRemove.add(disagreementSet2);
          }
        }
      }
      if (!disagreementSetsToRemove.isEmpty()) {
        necessaryDisagreementSets.removeAll(disagreementSetsToRemove);
      }
    }
    
    isProcessed = true;
  }

  @Override
  public void write() {
    writeSetOfSets(SHEET_NECESSARY_DISAGREEMENT_SETS, necessaryDisagreementSets);    
  }

  @Override
  public void execute() {
    if (resultReady()) {
      return;
    }
    
    DetermineDisagreementSets determineDisagreementSets = new DetermineDisagreementSets(strFile);
    determineDisagreementSets.execute();
    
    strC = determineDisagreementSets.getStrC();
    setS = determineDisagreementSets.getSetS();
    agreementSets = determineDisagreementSets.getAgreementSets();
    maximalAgreementSets = determineDisagreementSets.getMaximalAgreementSets();
    disagreementSets = determineDisagreementSets.getDisagreementSets();
    
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
      DetermineNecessaryDisagreementSets determineNecessaryDisagreementSets = 
          new DetermineNecessaryDisagreementSets(strFile);
      determineNecessaryDisagreementSets.execute();
      logger.debug("NecessaryDisagreementSets = " + determineNecessaryDisagreementSets.getNecessaryDisagreementSets());
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  }  
}
