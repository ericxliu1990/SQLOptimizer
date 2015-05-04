package edu.rice.ericliu.sql_optimizer.model;

/**
 * Note that it is asumed that funcToRun is one of "none", "avg", or "sum"
 */

public class AggFunc {
 
  private String funcToRun;
  private String expr;
  
  public String getFuncToRun () {
    return funcToRun; 
  }
  
  public String getExpr () {
    return expr;
  }
  
  public AggFunc (String inFuncToRun, String inExpr) {
    funcToRun = inFuncToRun;
    expr = inExpr;
  }
  
  public String toString(){
	  return "AggFunc(" + funcToRun + ", " + expr + ")";
  }
}