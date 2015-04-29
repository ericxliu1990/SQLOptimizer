package edu.rice.ericliu.sql_optimizer.model;
import java.util.*;

/**
 * This little class is used to hold the catalog information about a database table.
 * A bunch of these objects in a Map <String, TableData> is used to store the catalog.
 */

public class TableData {
 
  private int tupleCount;
  private Map <String, AttInfo> attributes;
  
  public TableData (int numTuples, Map <String, AttInfo> attsIn) {
    tupleCount = numTuples;
    attributes = attsIn;
  }
  
  public int getTupleCount () {
    return tupleCount; 
  }
  
  public AttInfo getAttInfo (String aboutMe) {
    return attributes.get (aboutMe); 
  }
  public Map<String, AttInfo> getAttributes(){
	  return attributes;
  }
  public String print () {
    String res = tupleCount + " tuples; atts are {";
    for (Map.Entry<String, AttInfo> j : attributes.entrySet ()) {
      res += "(" + j.getKey () + ": " + j.getValue ().print () + ")";
    }
    res += "}";
    return res;
  }
}