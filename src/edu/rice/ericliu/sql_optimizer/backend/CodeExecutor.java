package edu.rice.ericliu.sql_optimizer.backend;


import edu.rice.ericliu.sql_optimizer.frontend.TypeValidChecker;
import edu.rice.ericliu.sql_optimizer.model.AttInfo;
import edu.rice.ericliu.sql_optimizer.model.Attribute;
import edu.rice.ericliu.sql_optimizer.model.Expression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.rice.ericliu.sql_optimizer.model.*;
import edu.rice.ericliu.sql_optimizer.model.Expression.ExpressionType;
import edu.rice.ericliu.sql_optimizer.model.LogicCode.LogicCodeType;

public class CodeExecutor {
	private Map<LogicCodeType, Method> handler = new HashMap<LogicCodeType, Method>();
	private ArrayList<LogicCode> codes;
	private CodeHandler codeHandler;
	
	public CodeExecutor(Map<String, TableData> catalog, ArrayList<LogicCode> codes){
		this.codes = codes;
		codeHandler = new CodeHandler(catalog);
		try{
			handler.put(LogicCodeType.SelectProject, 
					codeHandler.getClass().getDeclaredMethod("handleSelectProject", LogicCode.class));
			handler.put(LogicCodeType.Group, 
					codeHandler.getClass().getDeclaredMethod("handleGroup", LogicCode.class));
		}catch(SecurityException e){
			throw new RuntimeException(e.toString());
		}catch(NoSuchMethodException e){
			throw new RuntimeException(e.toString());
		}

	}
	public void execute(){
		try{
			for(LogicCode code: codes){
				handler.get(code.getType()).invoke(codeHandler, code);
			}
		}catch(IllegalArgumentException e){
			throw new RuntimeException(e.toString());
		}catch(IllegalAccessException e){
			throw new RuntimeException(e.toString());
		}catch(InvocationTargetException e){
			throw new RuntimeException(e.toString());
		}

	}
}

class CodeHandler{
	private final String compiler = "g++";
	private final String path = "cppDir/";
	private Map <String, TableData> tables;
	
	static private final HashMap<ExpressionType, String> dbTypeConvertTable = new HashMap<ExpressionType, String>(){{
		put(ExpressionType.Int, "Int");
		put(ExpressionType.Float, "Float");
		put(ExpressionType.String, "Str");
	}};
	
	public CodeHandler(Map <String, TableData> tables){
		this.tables = tables;
	}
	
	public void handleSelectProject(LogicCode code){
		System.out.println("handleSelectProject Called!");
		ArrayList <Attribute> inAtts = getTableAttributes(code.getFirstOp());
		ArrayList <Attribute> outAtts = new ArrayList <Attribute>();
		HashMap <String, String> projectMap = new HashMap <String, String>();
		getOutAttributes(code.getProject(), outAtts, projectMap);
		String selection = code.getCondiction().toJavaString();
		try{
			@SuppressWarnings("unused")
			Selection foo = new Selection(inAtts, outAtts, selection, projectMap, 
							getTableFileName(code.getFirstOp()), 
							getTableFileName(code.getTargetOp()),
							compiler, path);
		}catch(Exception e){
			System.out.println("handleSelectProject " + e.toString());
		}
		
	}
	public void handleGroup(LogicCode code){
		System.out.println("handleGroup Called!");
	}
	
	private String getTableFileName(String tableName){
		return "table/" + tableName + ".tbl";
	}
	private ArrayList<Attribute> getTableAttributes(String tableName){
		Map<String, AttInfo> tableAtts = tables.get(tableName).getAttributes();
		Attribute[] attributes = new Attribute[tableAtts.size()];
		for(Map.Entry<String, AttInfo> e: tableAtts.entrySet()){
			attributes[e.getValue().getAttSequenceNumber()] = new Attribute(e.getValue().getDataType(), e.getKey());
		}
		return new ArrayList<Attribute>(Arrays.asList(attributes));
	}
	private void getOutAttributes(Expression expr, ArrayList <Attribute> outAtts, HashMap <String, String> projectMap){
		ArrayList<Expression> list = new ArrayList<Expression>();
		traverse(list, expr);
		int index = 3;
		for(Expression e: list){
			outAtts.add(new Attribute(dbTypeConvertTable.get(getExpressionType(e)), "att" + Integer.toString(index)));
			projectMap.put("att" + Integer.toString(index), e.toJavaString());
		}
		return;
	}
	
	private ExpressionType getExpressionType(Expression expr){
		try{
			if(expr.isIdentifier())
				return getIdentifierType(expr);
			if(expr.isValue()){
				return expr.getType();
			}
			if(expr.isUnary()){
				return TypeValidChecker.Check(expr.getType(), getExpressionType(expr.getSubexpression()));
			}
			if(expr.isBinary()){
				return TypeValidChecker.Check(expr.getType(), getExpressionType(expr.getLeftSubexpression()), getExpressionType(expr.getRightSubexpression()));
			}
			throw new RuntimeException("Error Expression type" + expr.getType());
		}catch(RuntimeException e){
			throw new RuntimeException (e.getMessage() + "\n in Expression " + expr.toString());
		}
	}
	private ExpressionType getIdentifierType(Expression expr){
		return TypeValidChecker.typeNameConvert(tables.get(expr.getIdentifierTable()).getAttInfo(expr.getIdentifierAttribute()).getDataType());
	}
	private HashMap <String, String> getProjectMap (Expression expr){
		HashMap <String, String> projectMap = new HashMap<String, String>();
		return projectMap;
	}
	private void  traverse(ArrayList<Expression> list, Expression expr){
		if(expr.getType().equals(ExpressionType.And)){
			traverse(list, expr.getLeftSubexpression());
			traverse(list, expr.getRightSubexpression());
			return;
		}
		if(expr.getType().equals(ExpressionType.Identifier)){
			list.add(expr);
			return;
		}
		return;
	} 
}