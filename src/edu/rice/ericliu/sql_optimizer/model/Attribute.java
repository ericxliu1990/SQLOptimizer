package edu.rice.ericliu.sql_optimizer.model;

public class Attribute {
 
  private String name;
  private String attType;
  
  public String getName () {
    return name; 
  }
  
  public String getType () {
    return attType;
  }
  
  public Attribute (String inType, String inName) {
    name = inName;
    attType = inType;
  }
  public String toString(){
	  return "Attribute(" + name + ", " + attType + ")";
  }
}