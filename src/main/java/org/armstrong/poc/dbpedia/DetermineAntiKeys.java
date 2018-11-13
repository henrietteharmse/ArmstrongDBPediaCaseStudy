package org.armstrong.poc.dbpedia;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.armstrong.hypergraph.Hypergraph;
import org.armstrong.hypergraph.HypergraphImpl;
import org.armstrong.hypergraph.mhs.BergeMHS;
import org.armstrong.hypergraph.mhs.MHS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DetermineAntiKeys extends ProcessStep {
  private static Logger logger = LoggerFactory.getLogger(DetermineAntiKeys.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  public static final String SHEET_ANTI_KEYS = "AntiKeys";
  
  public DetermineAntiKeys(String strFile) {
    super(strFile);
  }

  @Override
  public void read() {
    uniquenessConstraints = readSetOfSets(DetermineUniquenessConstraints.SHEET_UNIQUENESS_CONSTRAINTS, 
        SHEET_ANTI_KEYS, uniquenessConstraints);  
  }

  @Override
  public void process() {
    if (resultReady()) {
      return;
    }
    
    Hypergraph<String> h = new HypergraphImpl<String>(setS, uniquenessConstraints);
    MHS<String> mhs = new BergeMHS<String>();
    Set<Set<String>> setOfSets = mhs.calcMHS(h);
    
    antiKeys = new HashSet<Set<String>>();
    for (Set<String> set : setOfSets) {
      Set<String> setSCopy = new HashSet<String>(setS);
      setSCopy.removeAll(set);
      antiKeys.add(setSCopy);
    }
    
    isProcessed = true;  
  }

  @Override
  public void write() {
    writeSetOfSets(SHEET_ANTI_KEYS, antiKeys);
  }
  
  @Override
  public void execute() {
    DetermineUniquenessConstraints determineUniquenessConstraints = 
        new DetermineUniquenessConstraints(strFile);
    determineUniquenessConstraints.execute();
    
    strC = determineUniquenessConstraints.getStrC();
    setS = determineUniquenessConstraints.getSetS();
    agreementSets = determineUniquenessConstraints.getAgreementSets();
    maximalAgreementSets = determineUniquenessConstraints.getMaximalAgreementSets();
    disagreementSets = determineUniquenessConstraints.getDisagreementSets();
    necessaryDisagreementSets = determineUniquenessConstraints.getNecessaryDisagreementSets();
    uniquenessConstraints = determineUniquenessConstraints.getUniquenessConstraints();
    
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
      DetermineAntiKeys determineAntiKeys = new DetermineAntiKeys(strFile);
      determineAntiKeys.execute();
      logger.debug("Antikeys = " + determineAntiKeys.getAntiKeys());
      logger.debug("Compare AntiKeys and MaxAgreement sets = " + 
          determineAntiKeys.getAntiKeys().containsAll(determineAntiKeys.getMaximalAgreementSets()));
      logger.debug("Compare  MaxAgreement sets and AntiKeys = " + 
          determineAntiKeys.getMaximalAgreementSets().containsAll(determineAntiKeys.getAntiKeys()));
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  }   

}
