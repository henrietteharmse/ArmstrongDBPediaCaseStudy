package org.armstrong.poc.dbpedia;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.query.ParameterizedSparqlString;

public class QueryUtils {
  private static final String IRI_SEPARATOR = "/";
  
  public static final String RDF_TYPE = "rdfType";
  public static final String TYPE = "type";
  public static final String OBJECT_DIFFERENTIATOR = "2";
  public static final String IRI_INDICATOR = "IRI";
  
  public static Map<String, String> generateQueryParameters(Set<String> setS) {
    Map<String, String> iriToQueryParamMap = new HashMap<String, String>();
    
    IRIFactory iriFactory = IRIFactory.iriImplementation();
    
    for (String strIRI : setS) {
      IRI iri = iriFactory.create(strIRI);
      String rawPath = iri.getRawPath();
      int lastIndex = rawPath.lastIndexOf(IRI_SEPARATOR);
      String queryParameter = rawPath.substring(lastIndex+1, rawPath.length());
      iriToQueryParamMap.put(strIRI, queryParameter);
    } 
    
    return iriToQueryParamMap;
  } 
  
  public static void querySubjectPredicateObject(String subject, String predicate, String object, 
      StringBuilder queryBuilder) {
    querySubjectPredicateObject(subject, predicate, object, queryBuilder, false);
  }

  public static void querySubjectPredicateObject(String subject, String predicate, String object, 
      StringBuilder queryBuilder, boolean optional) {
    if (optional) {
      queryBuilder.append("OPTIONAL {");
    }
    queryBuilder.append("?");
    queryBuilder.append(subject);
    queryBuilder.append(" ?");
    queryBuilder.append(predicate);
    queryBuilder.append(" ?");
    queryBuilder.append(object);
    queryBuilder.append(" .");
    if (optional) {
      queryBuilder.append("}");
    }
  }  
  
  
  public static void querySubjectPredicateObject(String subject1, String subject2, String predicate, String object, 
      StringBuilder queryBuilder, boolean optional) {
    if (optional) {
      queryBuilder.append("OPTIONAL {");
    }
    queryBuilder.append("?");
    queryBuilder.append(subject1);
    queryBuilder.append(" ?");
    queryBuilder.append(predicate);
    queryBuilder.append(" ?");
    queryBuilder.append(object);
    queryBuilder.append(" .");
    queryBuilder.append("?");
    
    queryBuilder.append(subject2);
    queryBuilder.append(" ?");
    queryBuilder.append(predicate);
    queryBuilder.append(" ?");
    queryBuilder.append(object);
    queryBuilder.append(" .");
    if (optional) {
      queryBuilder.append("}");
    }
  } 
  
  public static void generateQueryForTuple1(String instance, Set<String> setS, Map<String, String> iriToQueryParamMap, 
      StringBuilder queryBuilder, ParameterizedSparqlString pss) {
    generateQueryForTuple1(instance, setS, iriToQueryParamMap, queryBuilder, pss, false);
  }

  public static void generateQueryForTuple1(String instance, Set<String> setS, Map<String, String> iriToQueryParamMap, 
      StringBuilder queryBuilder, ParameterizedSparqlString pss, boolean optional) {
    
    for (String component : setS) {
      querySubjectPredicateObject(instance, iriToQueryParamMap.get(component).concat(IRI_INDICATOR), 
          iriToQueryParamMap.get(component), queryBuilder, optional);
      pss.setIri(iriToQueryParamMap.get(component).concat(IRI_INDICATOR), component);
    }
  }
  
  public static void generateQueryForTuple2(String instance, Set<String> setS, Set<String> agreementSet, 
      Map<String, String> iriToQueryParamMap, StringBuilder queryBuilder, ParameterizedSparqlString pss) {
    
    generateQueryForTuple2(instance, setS, agreementSet, iriToQueryParamMap, queryBuilder, pss, false);
  } 

  public static void generateQueryForTuple2(String instance, Set<String> setS, Set<String> agreementSet, 
      Map<String, String> iriToQueryParamMap, StringBuilder queryBuilder, ParameterizedSparqlString pss, 
      boolean optional) {
    
    for (String component : agreementSet) {
      querySubjectPredicateObject(instance, iriToQueryParamMap.get(component).concat(IRI_INDICATOR), 
          iriToQueryParamMap.get(component), queryBuilder, optional);
      pss.setIri(iriToQueryParamMap.get(component).concat(IRI_INDICATOR), component);
    }
    
    Set<String> complementSet = new HashSet<String>(setS);
    complementSet.removeAll(agreementSet);
    for (String component : complementSet) {
      querySubjectPredicateObject(instance, iriToQueryParamMap.get(component).concat(IRI_INDICATOR), 
          iriToQueryParamMap.get(component).concat(OBJECT_DIFFERENTIATOR), queryBuilder, optional);
      pss.setIri(iriToQueryParamMap.get(component).concat(IRI_INDICATOR), component);
    }
  }   
  
  public static void generateWhereParametersForTuple1(String instance, Set<String> setS, 
      Map<String, String> iriToQueryParamMap, StringBuilder queryBuilder) {
    
    for (String component : setS) {
      queryBuilder.append("?");
      queryBuilder.append(iriToQueryParamMap.get(component));
      queryBuilder.append(" ");
    }
  }
  
  public static void generateWhereParametersForTuple2(String instance, Set<String> setS, Set<String> agreementSet, 
      Map<String, String> iriToQueryParamMap, StringBuilder queryBuilder) {
    
//    for (String component : agreementSet) {
//      queryBuilder.append("?");
//      queryBuilder.append(iriToQueryParamMap.get(component));
//      queryBuilder.append(" ");
//    }
    Set<String> complementSet = new HashSet<String>(setS);
    complementSet.removeAll(agreementSet);
    for (String component : complementSet) {
      queryBuilder.append("?");
      queryBuilder.append(iriToQueryParamMap.get(component).concat(OBJECT_DIFFERENTIATOR));
      queryBuilder.append(" ");      
    }
  }
  
  public static void generateOptionalQuery(String subject1, String subject2, Set<String> setS, Set<String> agreementSet, 
      Map<String, String> iriToQueryParamMap, StringBuilder queryBuilder, ParameterizedSparqlString pss) {
    
    for (String component : agreementSet) {
      querySubjectPredicateObject(subject1, subject2, iriToQueryParamMap.get(component).concat(IRI_INDICATOR), 
          iriToQueryParamMap.get(component), queryBuilder, false);
      pss.setIri(iriToQueryParamMap.get(component).concat(IRI_INDICATOR), component);
    }
    
    Set<String> complementSet = new HashSet<String>(setS);
    complementSet.removeAll(agreementSet);
    for (String component : complementSet) {
      querySubjectPredicateObject(subject1, iriToQueryParamMap.get(component).concat(IRI_INDICATOR), 
          iriToQueryParamMap.get(component), queryBuilder, true);      
      querySubjectPredicateObject(subject2, iriToQueryParamMap.get(component).concat(IRI_INDICATOR), 
          iriToQueryParamMap.get(component).concat(OBJECT_DIFFERENTIATOR), queryBuilder, true);
      pss.setIri(iriToQueryParamMap.get(component).concat(IRI_INDICATOR), component);
    }
    
  }
}
