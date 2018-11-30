package org.armstrong.poc.dbpedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public abstract class ProcessStep {
  private static Logger logger = LoggerFactory.getLogger(ProcessStep.class);
  // Why This Failure marker
  private static final Marker WTF_MARKER = MarkerFactory.getMarker("WTF");  
  protected static final String LOCATION = "/src/main/resources/";
  
  protected static final String ACTUAL_SPARQL_ENDPOINT = "http://localhost:7200/repositories/ArmstrongPersonData";
//  protected static final String ACTUAL_SPARQL_ENDPOINT = "http://localhost:7200/repositories/DBPediaPersonData";
//  protected static final String ARMSTRONG_SPARQL_ENDPOINT = "http://localhost:7200/repositories/ArmstrongPersonData/update";
  
  protected String strFile = null;
  
  protected String strC = null;  
  protected Set<String> setS; 
  protected Set<String> setX;
  protected Set<Set<String>> strongAgreementSets = null;
  protected Set<Set<String>> maximalStrongAgreementSets = null;
  protected Set<Set<String>> weakDisagreementSets = null;
  protected Set<Set<String>> necessaryDisagreementSets = null;
  protected Set<Set<String>> uniquenessConstraints = null;
  protected Set<Set<String>> antiKeys = null;
  
  protected boolean isProcessed = false;
  protected boolean isResultReady = false;
  
  
  public ProcessStep(String strFile) {
    super();
    this.strFile = strFile;
  }

  public boolean resultReady() {
    if (isProcessed || isResultReady) {
      return true;
    } else {
      return false;
    }
  }
  
  public abstract void read();
  
  public abstract void process();

  public abstract void write();
  
  public void execute() {
    read();
    process();
    write();
  }
  
  protected Map<Integer, String> readHeader(String strSheet, Sheet sheet, Row row) {
    Map<Integer, String> columnIndexMap = new HashMap<Integer, String>();
    
    int nColumn = 0;
        
    Iterator<Cell> cellIterator = row.cellIterator();
    if (!cellIterator.hasNext()) {
      logger.error("First cell of row " + row.getRowNum() + " of " + strSheet + "sheet of " 
          + strFile + " is empty.");
    } else {
      for (; cellIterator.hasNext();) {        
        Cell cell = cellIterator.next();
        columnIndexMap.put(nColumn++, cell.getStringCellValue());
      }
    }    
    
    return columnIndexMap;    
  }

  protected Map<String,Integer> createHeader(Sheet sheet) {
    Map<String,Integer> columnIndexMap = new HashMap<String,Integer>();
    
    int nColumn = 0;
    Row row = sheet.createRow(0);
    for (String component: setS) {
      Cell cell = row.createCell(nColumn);
      cell.setCellValue(component);
      columnIndexMap.put(component, new Integer(nColumn++));
    }
    
    return columnIndexMap;
  }  
  
  public Set<Set<String>> readSetOfSets(String strSheetToRead, String strSheetToCheck, Set<Set<String>> setOfSets) {
    if (setOfSets != null) {
      return setOfSets;
    }
    Workbook workbook = null;
    try {
      FileInputStream inputStream = new FileInputStream(new File(strFile));
      workbook = WorkbookFactory.create(inputStream);
      Sheet sheet = workbook.getSheet(strSheetToRead);
      
      Iterator<Row> rowIterator = sheet.iterator();
      if (!rowIterator.hasNext()) {
        logger.error(strSheetToRead + " sheet of " + strFile + " is empty.");
      } else {
        setOfSets = new HashSet<Set<String>>();
        Row row = rowIterator.next();
        Map<Integer, String> columnIndexMap = readHeader(strSheetToRead, sheet, row);
        for (;rowIterator.hasNext();) {
          row = rowIterator.next();
          Set<String> set = new HashSet<String>(); 
          
          Iterator<Cell> cellIterator = row.cellIterator();
          for (; cellIterator.hasNext();) {        
            Cell cell = cellIterator.next();
            String strValue = cell.getStringCellValue();
            if (!strValue.isEmpty()) {
              String component = columnIndexMap.get(cell.getColumnIndex());
              set.add(component);
            }
          }
          setOfSets.add(set);
        }        
      }
      
      Sheet sheetToCheck = workbook.getSheet(strSheetToCheck);
      if (sheetToCheck != null) {
        isResultReady = true;
      }  
      workbook.close();
      return setOfSets;
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
      if (workbook != null) {
        try {
          workbook.close();
        } catch (IOException e) {
          logger.error(WTF_MARKER, e.getMessage(), e);
        }
      }
    } 
    return setOfSets;
  }  

  public Set<Set<String>> readSetOfSets(String strSheetToRead, Set<Set<String>> setOfSets) {
    if (setOfSets != null) {
      return setOfSets;
    }
    Workbook workbook = null;
    try {
      FileInputStream inputStream = new FileInputStream(new File(strFile));
      workbook = WorkbookFactory.create(inputStream);
      Sheet sheet = workbook.getSheet(strSheetToRead);
      
      Iterator<Row> rowIterator = sheet.iterator();
      if (!rowIterator.hasNext()) {
        logger.error(strSheetToRead + " sheet of " + strFile + " is empty.");
      } else {
        setOfSets = new HashSet<Set<String>>();
        Row row = rowIterator.next();
        Map<Integer, String> columnIndexMap = readHeader(strSheetToRead, sheet, row);
        for (;rowIterator.hasNext();) {
          row = rowIterator.next();
          Set<String> set = new HashSet<String>(); 
          
          Iterator<Cell> cellIterator = row.cellIterator();
          for (; cellIterator.hasNext();) {        
            Cell cell = cellIterator.next();
            String strValue = cell.getStringCellValue();
            if (!strValue.isEmpty()) {
              String component = columnIndexMap.get(cell.getColumnIndex());
              set.add(component);
            }
          }
          setOfSets.add(set);
        }        
      }
        
      workbook.close();
      return setOfSets;
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
      if (workbook != null) {
        try {
          workbook.close();
        } catch (IOException e) {
          logger.error(WTF_MARKER, e.getMessage(), e);
        }
      }
    } 
    return setOfSets;
  }  
  
  protected void writeSetOfSets(String strSheet, Set<Set<String>> setOfSets) {
    if (isResultReady) {
      return;
    }
    
    Workbook workbook = null;
    try {
      FileInputStream inputStream = new FileInputStream(new File(strFile));
      workbook = WorkbookFactory.create(inputStream);
      Sheet sheet = workbook.createSheet(strSheet);
      Map<String,Integer> columnIndexMap = createHeader(sheet);
      
      int nColumnIndex = 0;
      int i = 1;
      
      for (Set<String> set : setOfSets) {
        Row row = sheet.createRow(i++);
        for (String component : set) {
          nColumnIndex = columnIndexMap.get(component);
          Cell cell = row.createCell(nColumnIndex);
          cell.setCellValue("X");          
        } 
      }
      FileOutputStream outputFile = new FileOutputStream(new File(strFile));
      workbook.write(outputFile);
      outputFile.close();
      workbook.close();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
      if (workbook != null) {
        try {
          workbook.close();
        } catch (IOException e) {
          logger.error(WTF_MARKER, e.getMessage(), e);
        }
      }
    }
  }
  
  public void writeSet(String strSheetToWrite, Set<String> set) {
    if (isResultReady) {
      return;
    }
    Workbook workbook = null;
    try {
      FileInputStream inputStream = new FileInputStream(new File(strFile));
      workbook = WorkbookFactory.create(inputStream);
      Sheet sheet = workbook.createSheet(strSheetToWrite);
  
      int i = 0;
      for (String component : set) {
        Row row = sheet.createRow(i++);
        Cell cell = row.createCell(0);
        cell.setCellValue(component);
      }
      FileOutputStream outputFile = new FileOutputStream(new File(strFile));
      workbook.write(outputFile);
      outputFile.close();
      workbook.close();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
      if (workbook != null)
        try {
          workbook.close();
        } catch (IOException e) {
          logger.error(WTF_MARKER, e.getMessage(), e);
        }
    }
  }

  public Set<String> readSet(String strSheetToRead, Set<String> set) {
    if (set != null) {
      return set;
    }
    set = new HashSet<String>();
    Workbook workbook = null;
    try {
      FileInputStream inputStream = new FileInputStream(new File(strFile));
      workbook = WorkbookFactory.create(inputStream);
      Sheet sheet = workbook.getSheet(strSheetToRead);
      
      Iterator<Row> rowIterator = sheet.iterator();
      if (!rowIterator.hasNext()) {
        logger.error(strSheetToRead + " sheet of " + strFile + " is empty.");
      } else {
        for (;rowIterator.hasNext();) {
          Row row = rowIterator.next();
          Iterator<Cell> cellIterator = row.cellIterator();
          if (!cellIterator.hasNext()) {
            logger.error("First cell of row " + row.getRowNum() + " of " + strSheetToRead + "sheet of " + strFile + " is empty.");
          } else {
            Cell cell = cellIterator.next();
            set.add(cell.getStringCellValue());
          }
        }        
      }      
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
      if (workbook != null) {
        try {
          workbook.close();
        } catch (IOException e) {
          logger.error(WTF_MARKER, e.getMessage(), e);
        }
      }
    }
    
    return set;
  }
  
  public Set<String> readSet(String strSheetToRead, String strSheetToCheck, Set<String> set) {
    set = readSet(strSheetToRead, set);

    Workbook workbook = null;
    try {   
      FileInputStream inputStream = new FileInputStream(new File(strFile));
      workbook = WorkbookFactory.create(inputStream);
      
      Sheet sheetToCheck = workbook.getSheet(strSheetToCheck);
      if (sheetToCheck != null) {
        isResultReady = true;
      }  
      workbook.close();
    } catch (Throwable t) {
      logger.error(WTF_MARKER, t.getMessage(), t);
      if (workbook != null) {
        try {
          workbook.close();
        } catch (IOException e) {
          logger.error(WTF_MARKER, e.getMessage(), e);
        }
      }
    }
    
    return set;
  }
  
  public String getStrC() {
    return strC;
  }

  public Set<String> getSetS() {
    return setS;
  } 

  public Set<String> getSetX() {
    return setX;
  }

  public Set<Set<String>> getStrongAgreementSets() {
    return strongAgreementSets;
  }  
  
  public Set<Set<String>> getMaximalStrongAgreementSets() {
    return maximalStrongAgreementSets;
  }

  public Set<Set<String>> getWeakDisagreementSets() {
    return weakDisagreementSets;
  }

  public Set<Set<String>> getNecessaryDisagreementSets() {
    return necessaryDisagreementSets;
  }

  public Set<Set<String>> getUniquenessConstraints() {
    return uniquenessConstraints;
  }

  public Set<Set<String>> getAntiKeys() {
    return antiKeys;
  }   
 
}
