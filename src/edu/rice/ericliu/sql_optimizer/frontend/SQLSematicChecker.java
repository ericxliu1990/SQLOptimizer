package edu.rice.ericliu.sql_optimizer.frontend;
import java.util.ArrayList;
import java.util.Map;

import edu.rice.ericliu.sql_optimizer.model.Expression;
import edu.rice.ericliu.sql_optimizer.model.Expression.ExpressionType;
import edu.rice.ericliu.sql_optimizer.model.Query;
import edu.rice.ericliu.sql_optimizer.model.RelationalAlgebra;
import edu.rice.ericliu.sql_optimizer.model.RelationalAlgebra.RAType;
import edu.rice.ericliu.sql_optimizer.model.TableData;


public class SQLSematicChecker {

	private Map <String, TableData> tables;
	private ArrayList <Expression> selectField;
	private Map <String, String> fromField;
	private Expression whereField;
	private ArrayList <String> groupbyField;
	private ArrayList <String> freeAttrList = new  ArrayList <String>();
	private boolean isAggregateClause = false;
	private RelationalAlgebra ra;
	private RelationalAlgebra raGrouping;
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
	
	private ExpressionType getIdentifierType(Expression expr){
		if(!fromField.containsKey(expr.getIdentifierTable())){
			throw new SematicException ("Invalid Table alias name " + expr.getIdentifierTable());
		}
		if(!tables.get(fromField.get(expr.getIdentifierTable())).getAttributes().containsKey(expr.getIdentifierAttribute())){
			throw new SematicException ("Invalid Attribute name " + expr.getIdentifierAttribute() + " in table " + fromField.get(expr.getIdentifierTable()));
		}
		expr.setIdentifierTable(fromField.get(expr.getIdentifierTable()));
		return TypeValidChecker.typeNameConvert(tables.get(expr.getIdentifierTable()).getAttInfo(expr.getIdentifierAttribute()).getDataType());
	}
	private ExpressionType checkExpression(Expression expr){
		try{
			if(expr.isIdentifier())
				return getIdentifierType(expr);
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

		if(expr.isAggreationExp()){
			if(checkContainIdentifer(expr) != 0){
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
			if(e.isAggreationExp()){
				addAggreation(e);
			}else{
				addProjection(e);
			}
		}
		return true;
	}
	private void addAggreation(Expression e){
		RelationalAlgebra newAggreation = new RelationalAlgebra(RAType.Aggreation, e);
		if(ra == null){
			raTable.setParent(newAggreation);
			ra = newAggreation;
			newAggreation.setChild(raTable);
			raGrouping = ra;
			return;
		}
		ra.setParent(newAggreation);
		newAggreation.setChild(ra);
		ra = newAggreation;
	}
	
	private void addProjection(Expression e){
		RelationalAlgebra newProjection = new RelationalAlgebra(RAType.Projection, e);
		if(ra == null){
			raTable.setParent(newProjection);
			ra = newProjection;
			newProjection.setChild(raTable);
			raGrouping = ra;
			return;
		}
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
		if(raTable == null){
			raTable = newRANode;
			return;
		}else{
			RelationalAlgebra newProductNode = new RelationalAlgebra(RAType.Product);
			raTable.setParent(newProductNode);
			newProductNode.setLeftChild(raTable);
			newRANode.setParent(newProductNode);
			newProductNode.setRightChild(newRANode);
			raTable = newProductNode;
			return;
		}
	}

	private boolean checkWhereField(){
		checkExpression(whereField);
		addSelection(whereField);
		return true;
	}
	
	private void addSelection(Expression exp){
		if(exp.getType().equals(ExpressionType.And)){
			addSelection(exp.getLeftSubexpression());
			addExpression(exp.getRightSubexpression());
		}else{
			addExpression(exp);
			return;
		}
	}
	
	private void addExpression(Expression exp){
		RelationalAlgebra newSelection = new RelationalAlgebra(RAType.Selection, exp);
		raTable.setParent(newSelection);
		newSelection.setChild(raTable);
		raTable = newSelection;
		return;
	}
	private boolean checkGroupbyField(){
		freeAttrList.removeAll(groupbyField);
		if(!freeAttrList.isEmpty()){
			throw new SematicException("The querry contains free attributes " + freeAttrList.toString());
		}
		for(String att: groupbyField){
			Expression att_expr = new Expression(ExpressionType.Identifier);
			att_expr.setValue(att);
			addGrouping(att_expr);
		}
		return true;
	}
	
	private void addGrouping(Expression expr){
		RelationalAlgebra newProjection = new RelationalAlgebra(RAType.Grouping, expr);
		raTable.setParent(newProjection);
		newProjection.setChild(raTable);
		raTable = newProjection;
	}
	
	public boolean check(){
		try{
			checkFromField();
			checkSelectField();
			if(whereField != null){
				checkWhereField();
				raGrouping.setChild(raTable);
				raTable.setParent(raGrouping);
			}
			if(isAggregateClause){
				checkGroupbyField();
				raGrouping.setChild(raTable);
				raTable.setParent(raGrouping);
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
	public RelationalAlgebra getGroupingRA(){
		return raGrouping;
	}
}

@SuppressWarnings("serial")
class SematicException extends RuntimeException {
	public SematicException(String s){
		super(s);
	}
}