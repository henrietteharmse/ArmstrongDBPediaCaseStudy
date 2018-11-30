package org.armstrong.poc.dbpedia;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ArmstrongABoxPOC {
  private static Logger logger = LoggerFactory.getLogger(ArmstrongABoxPOC.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");
  
  protected static final String LOCATION = "/src/main/resources/";
  
  
  private String strFile;
  private String strC;  
  private Set<String> setS;
  
  
  private boolean isProcessed = false;
  private boolean isResultReady = false;
 
  private Map<String, String> queryParamToIRIMap = new HashMap<String, String>();
  private Map<String, String> iriToQueryParamMap = new HashMap<String, String>();
  
  public ArmstrongABoxPOC(String strFile) {
    super();
    this.strFile = strFile;
  }
  

  public void generateInformativeArmstrongABox() {
    DetermineS determineS = new DetermineS(strFile);
    determineS.read();
    if (!determineS.resultReady()) {
      determineS.process();
      determineS.write();
    }        
    strC = determineS.getStrC();
    setS = determineS.getSetS();
    
  }
  
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Please specify a .xslx file as input.");
      return;
    }
    
    Path path = Paths.get(".").toAbsolutePath().normalize();
    String strFile = path.toFile().getAbsolutePath() + LOCATION + args[0];
    

    ArmstrongABoxPOC poc = new ArmstrongABoxPOC(strFile);
    try {
      poc.generateInformativeArmstrongABox();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
    }
  }
}
