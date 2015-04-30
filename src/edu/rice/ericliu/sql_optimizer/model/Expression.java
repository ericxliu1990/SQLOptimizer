package edu.rice.ericliu.sql_optimizer.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Expression {
  
// this is an exhaustive list of expression types
//  static public final String [] validTypes = {"plus", "minus", "times", 
//    "divided by",  "or", "and", "not", "literal string", "literal float",
//    "literal int", "identifier", "unary minus",
//    "sum", "avg", "equals", "greater than", "less than"};
	
	private enum ExpCategory{
		UNARY, BINARY, VALUE
	}

  // this is an exhaustive list of the unary expression types
  static public final ArrayList<String> unaryTypes = new ArrayList<String>(){{add("not"); add("unary minus"); add("sum"); add("avg");}};
  
  // this is an exhaustive list of the binary expression types
  static public final ArrayList<String> binaryTypes = new ArrayList<String>(){{add("plus"); add("minus"); add("times");
    add("divided by"); add("or"); add("and"); add("equals"); add("greater than"); add("less than");}};
  
  // this is an exhaustive list of the value types
  static public final ArrayList<String> valueTypes = new ArrayList<String>(){{add("literal string"); add("literal float");
    add("literal int"); add("identifier");}};
  
  static public final Map<ExpCategory, ArrayList<String>> categories = new HashMap<ExpCategory, ArrayList<String>>(){{
	  put(ExpCategory.UNARY, unaryTypes);
	  put(ExpCategory.BINARY, binaryTypes);
	  put(ExpCategory.VALUE, valueTypes);
  }};
  static private final ArrayList<String> aggregationList = new ArrayList<String>(){{
	  add("sum");
	  add("avg");
  }};
  // this is the type of the expression
  private String myType;
  
  // this is the literal value contained in the expression; only non-null
  // if myType is "literal" or "identifier"
  private String myValue;
  // every expression has a category, unaryType, BinaryTypes, valueTypes;
  private ExpCategory category;
  // these are the two subexpressions
  private Expression leftSubexpression;
  private Expression rightSubexpression;
  
  static public Expression combine(Expression expr1, Expression expr2){
	  Expression newExpr = new Expression("and");
	  newExpr.setSubexpression(expr1, expr2);
	  return newExpr;
  }
  @Override
  public String toString () {
    
    String toMe;
//    if(myType == "identifier"){
//  	  System.out.println(myValue);
//    }
    	// see if it is a literal type
      if (category == ExpCategory.VALUE) {
        toMe = myValue;
        return toMe;
      } 
    
    // see if it is a unary type
      if (category == ExpCategory.UNARY) {
        toMe = "(" + myType + " " + leftSubexpression.toString () + ")";
        return toMe;
      }
    
    // lastly, do a binary type
      if (category == ExpCategory.BINARY) {
        toMe = "(" + leftSubexpression.toString() + " " + myType + " " + rightSubexpression.toString() + ")";
        return toMe;
      }

      
    throw new RuntimeException ("got a bad type in the expression when printing");
  }
  
  // create a new expression of type specified type
  public Expression (String expressionType) {
    
    // Verify it is a valid expression type
	
	  for(Map.Entry<ExpCategory, ArrayList<String>> aCategory: categories.entrySet()){
		  for(String aType : aCategory.getValue()){
			  if(expressionType.equals(aType)){
				  this.myType = expressionType;
				  this.category = aCategory.getKey();
				  return;
			  }
		  }
	  }
	  
    // it is not valid, so throw an exception
    throw new RuntimeException ("you tried to create an invalid expr type");
  }
  
  public String getType () {
    return myType;
  }
  
  // this returns the value of the expression, if it is a literal (in which
  // case the literal values encoded as a string is returned), or it is an
  // identifier (in which case the name if the identifier is returned)
  public String getValue () {
      if (category == ExpCategory.VALUE) {
        return myValue;
      }
    throw new RuntimeException ("you can't get a value for that expr type!");
  }
  
  // this sets the value of the expression, if it is a literal or an 
  // identifier
  public void setValue (String toMe) {
      if (category == ExpCategory.VALUE) {
        myValue = toMe;
        return;
    } 
    throw new RuntimeException ("you can't set a value for that expr type!" + toMe);
  }
  
  // this gets the subexpression, which is only possible if this is a 
  // unary operation (such as "unary minus" or "not")
  public Expression getSubexpression () {
    
    // verfiy it is a valid expression type
      if (category == ExpCategory.UNARY) {
        return leftSubexpression;
      }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you can't get the subexpression of an " +
                                "expression that is not unary!");
  }
  
  // this sets the subexpression, which is only possible if this is a 
  // unary operation (such as "unary minus" or "not")
  public void setSubexpression (Expression newChild) {
 
    // verfiy it is a valid expression type
      if (category == ExpCategory.UNARY) {
        leftSubexpression = newChild;
        return;
      }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you can't set the subexpression of an " +
                                "expression that is not unary!");
  }
  
  // this gets either the left or the right subexpression, which is only 
  // possible if this is a binary operation... whichOne should either be
  // the string "left" or the string "right"
  public Expression getSubexpression (String whichOne) {
    
    // Verify it is a valid expression type

      if (category == ExpCategory.BINARY) {
        if (whichOne.equals ("left"))
          return leftSubexpression;
        else if (whichOne.equals ("right"))
          return rightSubexpression;
        else
          throw new RuntimeException ("whichOne must be left or right");
      }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you can't get the l/r subexpression of " +
                                "an expression that is not binry!");
  }

  public Expression getLeftSubexpression () {
    
    // Verify it is a valid expression type

      if (category == ExpCategory.BINARY) {
          return leftSubexpression;
      }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you can't get the l/r subexpression of " +
                                "an expression that is not binry!");
  }
  public Expression getRightSubexpression () {
	    
	    // Verify it is a valid expression type

	      if (category == ExpCategory.BINARY) {
	          return rightSubexpression;
	      }
	    
	    // it is not valid, so throw an exception
	    throw new RuntimeException ("you can't get the l/r subexpression of " +
	                                "an expression that is not binry!");
	  }
  // this sets the left and the right subexpression
  public void setSubexpression (Expression left, Expression right) {
    
    // verfiy it is a valid expression type
      if (category == ExpCategory.BINARY) {
        leftSubexpression = left;
        rightSubexpression = right;
        return;
      }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you can't set the l/r subexpression of " +
                                "an expression that is not binry!");
  }
  public final boolean isUnary(){
	  return category == ExpCategory.UNARY;
  }
  public final boolean isBinary(){
	  return category == ExpCategory.BINARY;
  }
  public final boolean isValue(){
	  return category == ExpCategory.VALUE;
  }
  public final boolean isIdentifier(){
	  return myType == "identifier";
  }
  public final boolean isAggreationExp(){
	  return aggregationList.contains(myType);
  }
}


