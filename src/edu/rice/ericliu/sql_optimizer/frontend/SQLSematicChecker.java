package edu.rice.ericliu.sql_optimizer.frontend;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import edu.rice.ericliu.sql_optimizer.model.*;
import edu.rice.ericliu.sql_optimizer.model.RelationalAlgebra.RAType;


public class SQLSematicChecker {

	private Map <String, TableData> tables;
	private ArrayList <Expression> selectField;
	private Map <String, String> fromField;
	private Expression whereField;
	private ArrayList <String> groupbyField;
	private ArrayList <String> freeAttrList = new  ArrayList <String>();
	private boolean isAggregateClause = false;
	private RelationalAlgebra ra;
	private RelationalAlgebra raTable;
	
	public SQLSematicChecker(Map<String, TableData> catalog, Query query) {
		super();
		this.tables = catalog;
		this.selectField = query.select;
		this.fromField = query.from;
		this.whereField = query.where;
		this.groupbyField = query.groupby;
	}
	private boolean checkValidTableName(String tableName){
		if(!tables.containsKey(tableName)){
			throw new SematicException ("Table Name " + tableName + " does not exist!");
		}
		return true;
	}
	@SuppressWarnings("unused")
	private boolean checkValidAttbute(String tableName, String attributeName){
		 if(checkValidTableName(tableName) || tables.get(tableName).getAttributes().containsKey(attributeName)){
			 throw new SematicException ("Attribute Name " + attributeName + " does not exist in table " + tableName);
		 }
		 return true;
	}
	
	private String getIdentifierType(String identifier){
		String[] idList  = identifier.split(Pattern.quote("."));
		if(!fromField.containsKey(idList[0])){
			throw new SematicException ("Invalid Table alias name " + idList[0]);
		}
		if(!tables.get(fromField.get(idList[0])).getAttributes().containsKey(idList[1])){
			throw new SematicException ("Invalid Attribute name " + idList[1] + " in table " + fromField.get(idList[0]));
		}
		return TypeValidChecker.typeNameConvert(tables.get(fromField.get(idList[0])).getAttInfo(idList[1]).getDataType());
	}
	private String checkExpression(Expression expr){
		try{
			if(expr.isIdentifier())
				return getIdentifierType(expr.getValue());
			if(expr.isValue()){
				return expr.getType();
			}
			if(expr.isUnary()){
				return TypeValidChecker.Check(expr.getType(), checkExpression(expr.getSubexpression()));
			}
			if(expr.isBinary()){
				return TypeValidChecker.Check(expr.getType(), checkExpression(expr.getLeftSubexpression()), checkExpression(expr.getRightSubexpression()));
			}
			return null;
		}catch(typeCheckException e){
			throw new SematicException (e.getMessage() + "\n in Expression " + expr.toString());
		}
	}
	private void addFreeAttr(Expression expr){
		if(expr.isIdentifier()){
			freeAttrList.add(expr.getValue());
		}
		if(expr.isUnary()){
			addFreeAttr(expr.getSubexpression());
		}
		if(expr.isBinary()){
			addFreeAttr(expr.getRightSubexpression());
			addFreeAttr(expr.getLeftSubexpression());
		}
	}
	private int checkContainIdentifer(Expression expr){
		if(expr.isIdentifier()){
			return 1;
		}
		if(expr.isUnary()){
			return checkContainIdentifer(expr.getSubexpression());
		}
		if(expr.isBinary()){
			return checkContainIdentifer(expr.getRightSubexpression()) + checkContainIdentifer(expr.getLeftSubexpression());
		}
		return 0;
	}
	private boolean checkFreeAttr(Expression expr){
		//By definition aggregation function only exists on the first layer
		//we check whether it is aggregation function and add all offspring

		//this expression is not a aggregation expression and
		// it does not contain identifiers
		System.out.println(expr);
		if(expr.isAggreationExp()){
			if(checkContainIdentifer(expr) != 0){
				if(isAggregateClause){
					throw new SematicException("Multiple aggreation exists.");
				}
				isAggregateClause = true;
			}
			return true;
		}else{
			addFreeAttr(expr);
			return false;
		}
	}
	private boolean checkSelectField(){
		for(Expression e: selectField){
			checkFreeAttr(e);
			checkExpression(e);
			addProjection(e);
		}
		return true;
	}
	private void addProjection(Expression e){
		RelationalAlgebra newProjection = new RelationalAlgebra(RAType.Projection, e);
		ra.setParent(newProjection);
		newProjection.setChild(ra);
		ra = newProjection;
	}
	private boolean checkFromField(){
		for(String item: fromField.values()){
			if(!checkValidTableName(item)){
				return false;
			}
			addRATable(item);
		}
		return true;
	}
	private void addRATable(String newTable){
		RelationalAlgebra newRANode = new RelationalAlgebra(RAType.Table, newTable);
		if(ra == null){
			ra = newRANode;
			raTable = ra;
			return;
		}else{
			RelationalAlgebra newProductNode = new RelationalAlgebra(RAType.Product);
			ra.setParent(newProductNode);
			newProductNode.setLeftChild(ra);
			newRANode.setParent(newProductNode);
			newProductNode.setRightChild(newRANode);
			ra = newProductNode;
			raTable = ra;
			return;
		}
	}
	private boolean checkWhereField(){
		checkExpression(whereField);
		addSelection(whereField);
		return true;
	}
	private void addSelection(Expression exp){
		if(exp.getType() == "and"){
			addSelection(exp.getLeftSubexpression());
			addExpression(exp.getRightSubexpression());
		}else{
			addExpression(exp);
			return;
		}
	}
	private void addExpression(Expression exp){
		RelationalAlgebra newSelection = new RelationalAlgebra(RAType.Selection, exp);
		newSelection.setParent(raTable.getParent());
		raTable.getParent().setChild(newSelection);
		newSelection.setChild(raTable);
		raTable.setParent(newSelection);
		return;
	}
	private boolean checkGroupbyField(){
		freeAttrList.removeAll(groupbyField);
		if(!freeAttrList.isEmpty()){
			throw new SematicException("The querry contains free attributes " + freeAttrList.toString());
		}
		for(String att: groupbyField){
			Expression att_expr = new Expression("identifier");
			att_expr.setValue(att);
			addGrouping(att_expr);
		}
		return true;
	}
	private void addGrouping(Expression expr){
		RelationalAlgebra newProjection = new RelationalAlgebra(RAType.Grouping, expr);
		System.out.println(expr.toString());
		ra.setParent(newProjection);
		newProjection.setChild(ra);
		ra = newProjection;
	}
	public boolean check(){
		try{
			checkFromField();
			checkSelectField();
			if(whereField != null){
				checkWhereField();
			}
			if(isAggregateClause){
				checkGroupbyField();	
			}
		}catch(SematicException e){
			System.out.println("SQL Sematic Error: " + e.getMessage());
			return false;
		}
		return true;
	}
	public RelationalAlgebra getRA(){
		return ra;
	}
}

class TypeValidChecker{
	static private final HashMap<String, String> notItem = new HashMap<String, String>(){{
		put("boolean", "boolean");
	}};
	static private final HashMap<String, String> unaryItem = new HashMap<String, String>(){{
		put("literal float", "literal float");
		put("literal int", "literal int");
	}};

	static private final HashMap<String, String> plusItem = new HashMap<String, String>(){{
		put("literal int" + "literal int", "literal int");
		put("literal int" + "literal float", "literal float");
		put("literal float" + "literal int", "literal float");
		put("literal float" + "literal float", "literal float");
		put("literal int" +  "literal string", "literal string");
		put("literal string" + "literal int", "literal string");
		put("literal float" + "literal string", "literal string");
		put("literal string" + "literal float", "literal string");
		put("literal string" + "literal string", "literal string");
	}};
	static private final HashMap<String, String> minusItem = new HashMap<String, String>(){{
		put("literal int" + "literal int", "literal int");
		put("literal int" + "literal float", "literal float");
		put("literal float" + "literal int", "literal float");
		put("literal float" + "literal float", "literal float");
	}};

	static private final HashMap<String, String> orItem = new HashMap<String, String>(){{
		put("boolean" + "boolean", "boolean");
	}};
	
	static private final HashMap<String, String> equalsItem = new HashMap<String, String>(){{
		put("literal int" + "literal int", "boolean");
		put("literal float" + "literal int", "boolean");
		put("literal int" + "literal float", "boolean");
		put("literal float" + "literal float", "boolean");
		put("literal string" + "literal string", "boolean");
	}};
	static private final HashMap<String, String> greaterItem = new HashMap<String, String>(){{
		put("literal int" + "literal int", "boolean");
		put("literal float" + "literal int", "boolean");
		put("literal int" + "literal float", "boolean");
		put("literal float" + "literal float", "boolean");
		put("literal string" + "literal string", "boolean");
	}};
	static private final HashMap<String, HashMap<String, String>> paraLookupTable = new HashMap<String, HashMap<String, String>>(){{
		put("not", notItem);
		put("unary minus", unaryItem);
		put("sum", unaryItem);
		put("avg", unaryItem);
		put("plus", plusItem);
		put("minus", minusItem);
		put("times", minusItem);
		put("divided by", minusItem);
		put("or", orItem);
		put("and", orItem);
		put("equals", equalsItem);
		put("greater than", greaterItem);
		put("less than", greaterItem);
		
	}};
	static private final HashMap<String, String> dbTypeConvertTable = new HashMap<String, String>(){{
		put("Int", "literal int");
		put("Float", "literal float");
		put("Str", "literal string");
	}};
	static public String Check(String type, String firstArg, String secondArg){
		String returnVal;
		returnVal = paraLookupTable.get(type).get(firstArg + secondArg);
		if(returnVal == null){
			throw new typeCheckException ("Type " + type + " does not support parameter type " + firstArg + " and type " + secondArg);
		}
		return returnVal;
	};
	static public String Check(String type, String firstArg){
		String returnVal;
		returnVal = paraLookupTable.get(type).get(firstArg);
		if(returnVal == null){
			throw new typeCheckException ("Type " + type + " does not support parameter type " + firstArg);
		}
		return returnVal;
	};
	static public String typeNameConvert(String dbType){
		return dbTypeConvertTable.get(dbType);
	}
}

class SematicException extends RuntimeException {
	public SematicException(String s){
		super(s);
	}
}

class typeCheckException extends SematicException{
	public typeCheckException(String s) {
		super(s);
	}
}

