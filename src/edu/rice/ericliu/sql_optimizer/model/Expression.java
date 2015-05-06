package edu.rice.ericliu.sql_optimizer.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Expression {
  
	static private enum ExpCategory{
		UNARY, BINARY, VALUE
	}
	static public enum ExpressionType{
		Not, UnaryMinus, sum, avg, 
		Plus, Minus, Times, DividedBy, Or, And, Equals, GreaterThan, LessThan, 
		String, Float, Int, Identifier,
		Boolean,
	}
	@SuppressWarnings("serial")
	static private final Map<String, ExpressionType> ExpressionTypeTable = new HashMap<String, ExpressionType>(){{
		put("not", ExpressionType.Not);
		put("unary minus", ExpressionType.UnaryMinus);
		put("sum", ExpressionType.sum);
		put("avg", ExpressionType.avg);
		put("plus", ExpressionType.Plus);
		put("minus", ExpressionType.Minus);
		put("times", ExpressionType.Times);
		put("divided by", ExpressionType.DividedBy);
		put("or", ExpressionType.Or);
		put("and", ExpressionType.And);
		put("equals", ExpressionType.Equals);
		put("greater than", ExpressionType.GreaterThan);
		put("less than", ExpressionType.LessThan);
		put("literal string", ExpressionType.String);
		put("literal float", ExpressionType.Float);
		put("literal int", ExpressionType.Int);
		put("identifier", ExpressionType.Identifier);
	}};
  // this is an exhaustive list of the unary expression types
	@SuppressWarnings("serial")
  	static private final ArrayList<ExpressionType> unaryTypes = new ArrayList<ExpressionType>(){{
	  add(ExpressionType.Not); 
	  add(ExpressionType.UnaryMinus); 
	  add(ExpressionType.sum); 
	  add(ExpressionType.avg);
	  }};
  
  // this is an exhaustive list of the binary expression types
  @SuppressWarnings("serial")
  	static private final ArrayList<ExpressionType> binaryTypes = new ArrayList<ExpressionType>(){{
	  add(ExpressionType.Plus); 
	  add(ExpressionType.Minus); 
	  add(ExpressionType.Times);
	  add(ExpressionType.DividedBy); 
	  add(ExpressionType.Or); 
	  add(ExpressionType.And); 
	  add(ExpressionType.Equals); 
	  add(ExpressionType.GreaterThan); 
	  add(ExpressionType.LessThan);
	  }};
  
  // this is an exhaustive list of the value types
  @SuppressWarnings("serial")
static private final ArrayList<ExpressionType> valueTypes = new ArrayList<ExpressionType>(){{
	  add(ExpressionType.String); 
	  add(ExpressionType.Float);
	  add(ExpressionType.Int); 
	  add(ExpressionType.Identifier);
	  }};
  
  @SuppressWarnings("serial")
static private final Map<ExpCategory, ArrayList<ExpressionType>> categories = new HashMap<ExpCategory, ArrayList<ExpressionType>>(){{
	  put(ExpCategory.UNARY, unaryTypes);
	  put(ExpCategory.BINARY, binaryTypes);
	  put(ExpCategory.VALUE, valueTypes);
  }};
  @SuppressWarnings("serial")
static private final ArrayList<ExpressionType> aggregationList = new ArrayList<ExpressionType>(){{
	  add(ExpressionType.sum);
	  add(ExpressionType.avg);
  }};
  @SuppressWarnings("serial")
static private final Map<ExpressionType, String> javaStringMap = new HashMap<ExpressionType, String>(){{
	  put(ExpressionType.Not, "!");
	  put(ExpressionType.UnaryMinus, "-");
	  put(ExpressionType.sum, "sum");
	  put(ExpressionType.avg, "avg");
	  put(ExpressionType.Plus, "+");
	  put(ExpressionType.Minus, "-");
	  put(ExpressionType.Times, "*");
	  put(ExpressionType.DividedBy, "/");
	  put(ExpressionType.Or, "||");
	  put(ExpressionType.And, "&&");
	  put(ExpressionType.Equals, "==");
	  put(ExpressionType.GreaterThan, ">");
	  put(ExpressionType.LessThan, "<");
  }};
  // this is the type of the expression
  private ExpressionType myType;
  
  // this is the literal value contained in the expression; only non-null
  // if myType is "literal" or "identifier"
  private String myValue;
  // every expression has a category, unaryType, BinaryTypes, valueTypes;
  private ExpCategory category;
  // these are the two subexpressions
  private Expression leftSubexpression;
  private Expression rightSubexpression;
  
  static public Expression combine(Expression expr1, Expression expr2){
	  Expression newExpr = new Expression(ExpressionType.And);
	  newExpr.setSubexpression(expr1, expr2);
	  return newExpr;
  }
  static public Expression getAllIdentifier(Expression expr){
	  ArrayList<Expression> list = new ArrayList<Expression>();
	  traverse(list, expr);
	  if(list.size() == 0){
		  return null;
	  }
	  Expression newExpr = list.get(0);
	  for(int idx = 1; idx < list.size(); idx++){
		 expr =  combine(expr, list.get(idx));
	  }
	  return newExpr;
  }
  static private void traverse(ArrayList<Expression> list, Expression expr){
	  if(expr.isBinary()){
		  traverse(list, expr.getLeftSubexpression());
		  traverse(list, expr.getRightSubexpression());
	  }
	  if(expr.isUnary()){
		  traverse(list, expr.getSubexpression());
	  }
	  if(expr.isIdentifier()){
		  list.add(expr);
	  }
  }
  // create a new expression of type specified type
  public Expression (ExpressionType type) {
    
    // Verify it is a valid expression type
	
	  for(Map.Entry<ExpCategory, ArrayList<ExpressionType>> aCategory: categories.entrySet()){
		  for(ExpressionType aType : aCategory.getValue()){
			  if(type.equals(aType)){
				  this.myType = type;
				  this.category = aCategory.getKey();
				  return;
			  }
		  }
	  }
	  
    // it is not valid, so throw an exception
    throw new RuntimeException ("you tried to create an invalid expr type");
  }
  public Expression(String type){
	  this(ExpressionTypeTable.get(type));
  }
  
  @Override
  public String toString () {
    
    if (category == ExpCategory.VALUE) {
        return myValue;
     } 
 
    if (category == ExpCategory.UNARY) {
        return  "(" + myType + " " + leftSubexpression.toString () + ")";
     }
    if (category == ExpCategory.BINARY) {
        return  "(" + leftSubexpression.toString() + " " + myType + " " + rightSubexpression.toString() + ")";
     }
 
    throw new RuntimeException ("got a bad type in the expression when printing");
  }
  
  public String toJavaString(){
	  if(myType == ExpressionType.String){
		  return "Str("  + myValue + ")";
	  }
      if(myType == ExpressionType.Int ) {
          return "Int (" + myValue + ")";
        } 
      if(myType == ExpressionType.Float){
    	  return "Float (" + myValue + ")";
      }
      if(myType == ExpressionType.Identifier){
    	  return getIdentifierAttribute();
      }
      if (category == ExpCategory.UNARY) {
          return "(" + myType + " " + leftSubexpression.toJavaString () + ")";
        }

      if (category == ExpCategory.BINARY) {
          return "(" + leftSubexpression.toJavaString() + " " + javaStringMap.get(myType) + " " + rightSubexpression.toJavaString() + ")";
        }
     throw new RuntimeException ("got a bad type in the expression when printing");
  }
  public String toJavaString(Map<String, String> tableMap){
      if(myType == ExpressionType.Identifier){
    	  return tableMap.get(getIdentifierTable()) + "." + getIdentifierAttribute();
      } 
	  if(myType == ExpressionType.String){
		  return "Str("  + myValue + ")";
	  }
      if(myType == ExpressionType.Int ) {
          return "Int (" + myValue + ")";
        } 
      if(myType == ExpressionType.Float){
    	  return "Float (" + myValue + ")";
      }
      if (category == ExpCategory.UNARY) {
          return "(" + myType + " " + leftSubexpression.toJavaString (tableMap) + ")";
        }

      if (category == ExpCategory.BINARY) {
          return "(" + leftSubexpression.toJavaString(tableMap) + " " + javaStringMap.get(myType) + " " + rightSubexpression.toJavaString(tableMap) + ")";
        }
     throw new RuntimeException ("got a bad type in the expression when printing");
  }
  
  public ExpressionType getType () {
    return myType;
  }
  
  // this returns the value of the expression, if it is a literal (in which
  // case the literal values encoded as a string is returned), or it is an
  // identifier (in which case the name if the identifier is returned)
  public String getValue () {
      if (category.equals(ExpCategory.VALUE)) {
        return myValue;
      }
    throw new RuntimeException ("you can't get a value for that expr type!");
  }
  
  public String getIdentifierTable(){
	  if(myType.equals(ExpressionType.Identifier)){
		  String[] idList  = myValue.split(Pattern.quote("."));
		  return idList[0];
	  }
	  throw new RuntimeException ("you can't get the table value for that expr type!");
  }
  public void setIdentifierTable(String newTableName){
	  if(myType.equals(ExpressionType.Identifier)){
		  String[] idList  = myValue.split(Pattern.quote("."));
		  setValue(newTableName + "." + idList[1]);
		  return;
	  }
	  throw new RuntimeException ("you can't get the table value for " + getType().name() + " type!");
  }
  public String getIdentifierAttribute(){
	  if(myType.equals(ExpressionType.Identifier)){
		  String[] idList  = myValue.split(Pattern.quote("."));
		  return idList[1];
	  }
	  throw new RuntimeException ("you can't get the attribute value for that expr type!");
  }
  
  // this sets the value of the expression, if it is a literal or an 
  // identifier
  public void setValue (String toMe) {
      if (category.equals(ExpCategory.VALUE)) {
        myValue = toMe;
        return;
    } 
    throw new RuntimeException ("you can't set a value for that expr type!" + toMe);
  }

  // this gets the subexpression, which is only possible if this is a 
  // unary operation (such as "unary minus" or "not")
  public Expression getSubexpression () {
    
    // verfiy it is a valid expression type
      if (category.equals(ExpCategory.UNARY)) {
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
      if (category.equals( ExpCategory.UNARY)) {
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

      if (category.equals( ExpCategory.BINARY)) {
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

      if (category.equals(ExpCategory.BINARY)) {
          return leftSubexpression;
      }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you can't get the l/r subexpression of " +
                                "an expression that is not binry!");
  }
  public Expression getRightSubexpression () {
	    
	    // Verify it is a valid expression type

	      if (category.equals(ExpCategory.BINARY)) {
	          return rightSubexpression;
	      }
	    
	    // it is not valid, so throw an exception
	    throw new RuntimeException ("you can't get the l/r subexpression of " +
	                                "an expression that is not binry!");
	  }
  // this sets the left and the right subexpression
  public void setSubexpression (Expression left, Expression right) {
    
    // Verify it is a valid expression type
      if (category.equals(ExpCategory.BINARY)) {
        leftSubexpression = left;
        rightSubexpression = right;
        return;
      }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you can't set the l/r subexpression of " +
                                "an expression that is not binry!");
  }
  public final boolean isUnary(){
	  return category.equals(ExpCategory.UNARY);
  }
  public final boolean isBinary(){
	  return category.equals(ExpCategory.BINARY);
  }
  public final boolean isValue(){
	  return category.equals(ExpCategory.VALUE);
  }
  public final boolean isIdentifier(){
	  return myType.equals(ExpressionType.Identifier);
  }
  public final boolean isAggreationExp(){
	  return aggregationList.contains(myType);
  }
}


