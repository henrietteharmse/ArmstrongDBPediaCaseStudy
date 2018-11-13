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

public class DetermineUniquenessConstraints extends ProcessStep {
  private static Logger logger = LoggerFactory.getLogger(DetermineUniquenessConstraints.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String SHEET_UNIQUENESS_CONSTRAINTS = "UniquenessConstraints";
  
  public DetermineUniquenessConstraints(String strFile) {
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
    DetermineNecessaryDisagreementSets determineNecessaryDisagreementSetsDisagreementSets = new DetermineNecessaryDisagreementSets(strFile);
    determineNecessaryDisagreementSetsDisagreementSets.execute();
    
    strC = determineNecessaryDisagreementSetsDisagreementSets.getStrC();
    setS = determineNecessaryDisagreementSetsDisagreementSets.getSetS();
    agreementSets = determineNecessaryDisagreementSetsDisagreementSets.getAgreementSets();
    maximalAgreementSets = determineNecessaryDisagreementSetsDisagreementSets.getMaximalAgreementSets();
    disagreementSets = determineNecessaryDisagreementSetsDisagreementSets.getDisagreementSets();
    necessaryDisagreementSets = determineNecessaryDisagreementSetsDisagreementSets.getNecessaryDisagreementSets();
    
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
      DetermineUniquenessConstraints determineUniquenessConstraints = new DetermineUniquenessConstraints(strFile);
      determineUniquenessConstraints.execute();
      logger.debug("UniquenessConstraints = " + determineUniquenessConstraints.getUniquenessConstraints());
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  }  
}
