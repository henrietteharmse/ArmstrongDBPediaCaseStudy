package org.armstrong.poc.dbpedia;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.armstrong.hypergraph.Hypergraph;
import org.armstrong.hypergraph.HypergraphImpl;
import org.armstrong.hypergraph.mhs.BergeMHS;
import org.armstrong.hypergraph.mhs.MHS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DetermineSigma extends ProcessStep {
  private static Logger logger = LoggerFactory.getLogger(DetermineSigma.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String SHEET_UNIQUENESS_CONSTRAINTS = "UniquenessConstraints";
  
  public DetermineSigma(String strFile) {
    super(strFile);
  }

  @Override
  public void read() {
    necessaryDisagreementSets = readSetOfSets(DetermineNecessaryDisagreementSets.SHEET_NECESSARY_DISAGREEMENT_SETS, 
        SHEET_UNIQUENESS_CONSTRAINTS, necessaryDisagreementSets);

  }

  @Override
  public void process() {
    if (resultReady()) {
      return;
    }
    Hypergraph<String> h = new HypergraphImpl<String>(setS, necessaryDisagreementSets);
    MHS<String> mhs = new BergeMHS<String>();
    uniquenessConstraints = mhs.calcMHS(h);
    
    isProcessed = true;
  }

  @Override
  public void write() {
    writeSetOfSets(SHEET_UNIQUENESS_CONSTRAINTS, uniquenessConstraints);
  }
  
  @Override
  public void execute() {
    DetermineNecessaryDisagreementSets determineNecessaryDisagreementSets = new DetermineNecessaryDisagreementSets(strFile);
    determineNecessaryDisagreementSets.execute();
    
    strC = determineNecessaryDisagreementSets.getStrC();
    setS = determineNecessaryDisagreementSets.getSetS();
    strongAgreementSets = determineNecessaryDisagreementSets.getStrongAgreementSets();
    maximalStrongAgreementSets = determineNecessaryDisagreementSets.getMaximalStrongAgreementSets();
    weakDisagreementSets = determineNecessaryDisagreementSets.getWeakDisagreementSets();
    necessaryDisagreementSets = determineNecessaryDisagreementSets.getNecessaryDisagreementSets();
    
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
      DetermineSigma determineUniquenessConstraints = new DetermineSigma(strFile);
      determineUniquenessConstraints.execute();
      logger.debug("UniquenessConstraints = " + determineUniquenessConstraints.getUniquenessConstraints());
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  }  
}
