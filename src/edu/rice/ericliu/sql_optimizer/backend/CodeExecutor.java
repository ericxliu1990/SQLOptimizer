package edu.rice.ericliu.sql_optimizer.backend;


import edu.rice.ericliu.sql_optimizer.model.AggFunc;
import edu.rice.ericliu.sql_optimizer.frontend.TypeValidChecker;
import edu.rice.ericliu.sql_optimizer.model.AttInfo;
import edu.rice.ericliu.sql_optimizer.model.Attribute;
import edu.rice.ericliu.sql_optimizer.model.Expression;
import edu.rice.ericliu.sql_optimizer.model.RelationalAlgebra;
import edu.rice.ericliu.sql_optimizer.model.TableData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.rice.ericliu.sql_optimizer.model.*;
import edu.rice.ericliu.sql_optimizer.model.Expression.ExpressionType;
import edu.rice.ericliu.sql_optimizer.model.LogicCode.LogicCodeType;
import edu.rice.ericliu.sql_optimizer.model.RelationalAlgebra.RAType;

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
			handler.put(LogicCodeType.Select, 
					codeHandler.getClass().getDeclaredMethod("handleSelectProject", LogicCode.class));
			handler.put(LogicCodeType.GroupAggreation, 
					codeHandler.getClass().getDeclaredMethod("handleGroupAggreation", LogicCode.class));
			handler.put(LogicCodeType.Aggreation, 
					codeHandler.getClass().getDeclaredMethod("handleGroupAggreation", LogicCode.class));
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
	
	@SuppressWarnings("serial")
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
		if(code.getType().equals(LogicCodeType.SelectProject)){
			getOutAttributes(code.getProject(), outAtts, projectMap);
		}else{
			getOutAttributes(inAtts, outAtts, projectMap);
		}
		//this is a hack solution, 
		//should be removed after added the optimization 
		tableUpdate(code.getTargetOp(), outAtts, projectMap);
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
	
	public void handleGroupAggreation(LogicCode code){
		System.out.println("handleGroupAggreation Called!");
		ArrayList <Attribute> inAtts = getTableAttributes(code.getFirstOp());
		ArrayList <Attribute> outAtts = new ArrayList <Attribute>();
		HashMap <String, AggFunc> myAggs = new HashMap <String, AggFunc> ();
		ArrayList <String> groupingAtts = new ArrayList <String> ();
		try{
			if(code.getType().equals(LogicCodeType.GroupAggreation)){
				getAggreationFunction(code.getProject(), outAtts, myAggs);
				groupingAtts = getGroupingAttributes(code.getCondiction());
			}else{
				getAggreationFunction(code.getCondiction(), outAtts, myAggs);
			}
			@SuppressWarnings("unused")
			Grouping foo = new Grouping(inAtts, outAtts, groupingAtts, myAggs,
					getTableFileName(code.getFirstOp()),
					getTableFileName(code.getTargetOp()),
					compiler, path);
		}catch(Exception e){
			System.out.println("handleGroupAggreation" + e.toString());
		}
	}
	
	private void tableUpdate(String tableName, ArrayList <Attribute> atts, HashMap <String, String> projectMap){
		Map <String, AttInfo> attsIn = new HashMap <String, AttInfo>();
		for(int idx = 0; idx < atts.size(); idx++){
			attsIn.put(projectMap.get(atts.get(idx).getName()),
					new AttInfo(0, atts.get(idx).getType(),idx));
		}
		TableData tableAttributes = new TableData(0, attsIn);
		tables.put(tableName, tableAttributes);
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
	
	private ArrayList<String> getGroupingAttributes(Expression expr){
		ArrayList<Expression> list = new ArrayList<Expression>();
		ArrayList<String> ret = new ArrayList<String>();
		traverse(list, expr);
		for(Expression e: list){
			ret.add(e.toJavaString());
		}
		return ret;
	}
	private void getAggreationFunction(Expression expr, ArrayList<Attribute> outAtts, HashMap<String, AggFunc> myAggs){
		ArrayList<Expression> list = new ArrayList<Expression>();
		traverse(list, expr);
		System.out.println(expr);
		System.out.println(list);
		int index = 0;
		for(Expression e: list){
			outAtts.add(new Attribute(dbTypeConvertTable.get(getExpressionType(e)), "att" + Integer.toString(index)));
			myAggs.put("att" + Integer.toString(index), getNewAggFunc(e));
			index += 1;
		}
		return;
	} 
	
	private AggFunc getNewAggFunc(Expression expr){
		if(expr.isAggreationExp()){
			return new AggFunc(expr.getType().name(), expr.getSubexpression().toJavaString());
		}else{
			return new AggFunc("none", expr.toJavaString());
		} 
	}
	
	private void getOutAttributes(Expression expr, ArrayList <Attribute> outAtts, HashMap <String, String> projectMap){
		ArrayList<Expression> list = new ArrayList<Expression>();
		traverse(list, expr);
		int index = 0;
		for(Expression e: list){
			outAtts.add(new Attribute(dbTypeConvertTable.get(getExpressionType(e)), "att" + Integer.toString(index)));
			projectMap.put("att" + Integer.toString(index), e.toJavaString());
			index += 1;
		}
		return;
	}
	private void getOutAttributes(ArrayList <Attribute> inAtts, ArrayList <Attribute> outAtts, HashMap <String, String> projectMap){
		int index = 0;
		for(Attribute att: inAtts){
			outAtts.add(new Attribute(att.getType(), "att" + Integer.toString(index)));
			projectMap.put("att" + Integer.toString(index), att.getName());
			index += 1; 
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
	
	private void traverse(ArrayList<Expression> list, Expression expr){
		if(expr.getType().equals(ExpressionType.And)){
			traverse(list, expr.getRightSubexpression());
			list.add(expr.getLeftSubexpression());
		}else{
			list.add(expr);
		}
		return;
	} 
}