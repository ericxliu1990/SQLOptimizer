package edu.rice.ericliu.sql_optimizer.backend;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.rice.ericliu.sql_optimizer.frontend.TypeValidChecker;
import edu.rice.ericliu.sql_optimizer.model.AggFunc;
import edu.rice.ericliu.sql_optimizer.model.AttInfo;
import edu.rice.ericliu.sql_optimizer.model.Attribute;
import edu.rice.ericliu.sql_optimizer.model.Expression;
import edu.rice.ericliu.sql_optimizer.model.Expression.ExpressionType;
import edu.rice.ericliu.sql_optimizer.model.LogicCode;
import edu.rice.ericliu.sql_optimizer.model.LogicCode.LogicCodeType;
import edu.rice.ericliu.sql_optimizer.model.TableData;

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
			handler.put(LogicCodeType.JoinProject, 
					codeHandler.getClass().getDeclaredMethod("handleJoinProject", LogicCode.class));
			handler.put(LogicCodeType.Product, 
					codeHandler.getClass().getDeclaredMethod("handleJoinProject", LogicCode.class));
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
			getOutAttributes(code.getProject(), outAtts, projectMap, 0);
		}else{
			getOutAttributesDirect(inAtts, outAtts, projectMap, 0);
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
		System.out.println(inAtts);
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

	public void handleJoinProject(LogicCode code){
		try{
		System.out.println("handleJoinProject Called!");
		ArrayList <Attribute> inAttsLeft = getTableAttributes(code.getFirstOp());
		System.out.println(inAttsLeft);
		ArrayList <Attribute> inAttsRight = getTableAttributes(code.getSecondOp());
		System.out.println(inAttsRight);
		HashMap<String, String> tableMap = getTableMap(code);
		ArrayList <Attribute> outAtts = new ArrayList <Attribute> ();
		HashMap <String, String> projectMap = new HashMap <String, String> ();
		if(code.getType().equals(LogicCodeType.JoinProject)){
			getOutAttributes(code.getProject(), tableMap, outAtts, projectMap, 0);
		}
		if(code.getType().equals(LogicCodeType.Product)){
			getOutAttributesDirect("left", inAttsLeft, outAtts, projectMap, 0);
			getOutAttributesDirect("right",inAttsRight, outAtts, projectMap, inAttsLeft.size());
		}
		System.out.println(projectMap);
		System.out.println(outAtts);
		//this is a hack solution, 
		//should be removed after added the optimization 
		HashMap <String, String> projectMapWithoutTableName = removeTableName(projectMap);
		System.out.println(projectMapWithoutTableName);
		tableUpdate(code.getTargetOp(), outAtts, projectMapWithoutTableName);
		HashMap<String, ArrayList <String>> tableHash = new HashMap<String, ArrayList <String>> ();
		tableHash.put(code.getFirstOp(), new ArrayList<String>());
		tableHash.put(code.getSecondOp(), new ArrayList<String>());
		if(code.getType().equals(LogicCodeType.JoinProject)){
			getEqualityCheckList(code.getCondiction(), tableHash);
		}
		String selection = new String();
		if(code.getType().equals(LogicCodeType.JoinProject)){
			selection = code.getCondiction().toJavaString(tableMap);
		}
		if(code.getType().equals(LogicCodeType.Product)){
			selection = "true";
		}
		System.out.println(selection);
		
	    @SuppressWarnings("unused")
	    Join foo = new Join (inAttsLeft, inAttsRight, outAtts, 
								tableHash.get(code.getFirstOp()), 
								tableHash.get(code.getSecondOp()), 
								selection, projectMap, 
	                            getTableFileName(code.getFirstOp()),
	                            getTableFileName(code.getSecondOp()), 
	                            getTableFileName(code.getTargetOp()), 
	                            compiler, path); 
	      } catch (Exception e) {
	    	  System.out.println("handleJoinProject " + e.toString());
	      }
	}
	private void getEqualityCheckList(Expression expr, HashMap<String, ArrayList <String>> tableHash){
		ArrayList<Expression> list = new ArrayList<Expression>();
		traverseEquals(list, expr);
		for(Expression e: list){
			tableHash.get(e.getLeftSubexpression().getIdentifierTable()).add(e.getLeftSubexpression().getIdentifierAttribute());
			tableHash.get(e.getRightSubexpression().getIdentifierTable()).add(e.getRightSubexpression().getIdentifierAttribute());
		}
	}
	private void traverseEquals(ArrayList<Expression> list, Expression expr){
		if(expr.getType().equals(ExpressionType.Equals) &&
				expr.getLeftSubexpression().getType().equals(ExpressionType.Identifier) &&
				expr.getRightSubexpression().getType().equals(ExpressionType.Identifier)){
			list.add(expr);
			return;
		}
		if(expr.isUnary()){
			traverseEquals(list, expr.getSubexpression());
		}
		if(expr.isBinary()){
			traverseEquals(list, expr.getLeftSubexpression());
			traverseEquals(list, expr.getRightSubexpression());
		}
		return;
	} 
	private HashMap<String, String> getTableMap(LogicCode code){
		HashMap<String, String> tableMap = new HashMap<String, String>();
		tableMap.put(code.getFirstOp(), "left");
		tableMap.put(code.getSecondOp(), "right");
		return tableMap;
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
	
	private HashMap <String, String> removeTableName(HashMap <String, String> map){
		HashMap <String, String> retMap = new HashMap <String, String>();
		for(Map.Entry <String, String> entry: map.entrySet()){
			String identifier = entry.getValue().replace("right.", "");
			identifier = identifier.replace("left.", "");
			retMap.put(entry.getKey(), identifier);
		}
		return retMap;
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
		traverseAnd(list, expr);
		for(Expression e: list){
			ret.add(e.toJavaString());
		}
		return ret;
	}
	
	private void getAggreationFunction(Expression expr, ArrayList<Attribute> outAtts, HashMap<String, AggFunc> myAggs){
		ArrayList<Expression> list = new ArrayList<Expression>();
		traverseAnd(list, expr);
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
	
	private void getOutAttributes(Expression expr, ArrayList <Attribute> outAtts, 
								HashMap <String, String> projectMap, int index){
		ArrayList<Expression> list = new ArrayList<Expression>();
		traverseAnd(list, expr);
		for(Expression e: list){
			outAtts.add(new Attribute(dbTypeConvertTable.get(getExpressionType(e)), "att" + Integer.toString(index)));
			projectMap.put("att" + Integer.toString(index), e.toJavaString());
			index += 1;
		}
		return;
	}
	
	private void getOutAttributes(Expression expr, HashMap<String, String> tableMap, 
			ArrayList <Attribute> outAtts, HashMap <String, String> projectMap, int index){
		ArrayList<Expression> list = new ArrayList<Expression>();
		traverseAnd(list, expr);
		for(Expression e: list){
			outAtts.add(new Attribute(dbTypeConvertTable.get(getExpressionType(e)), "att" + Integer.toString(index)));
			projectMap.put("att" + Integer.toString(index), e.toJavaString(tableMap));
			index += 1;
		}
		return;
	}
	private void getOutAttributesDirect(ArrayList <Attribute> inAtts,
			ArrayList <Attribute> outAtts, HashMap <String, String> projectMap, int index){
		for(Attribute att: inAtts){
			outAtts.add(new Attribute(att.getType(), "att" + Integer.toString(index)));
			projectMap.put("att" + Integer.toString(index), att.getName());
			index += 1; 
		}
		return;
	}
	
	private void getOutAttributesDirect(String tableMap, ArrayList <Attribute> inAtts, 
			ArrayList <Attribute> outAtts, HashMap <String, String> projectMap, int index){
		for(Attribute att: inAtts){
			outAtts.add(new Attribute(att.getType(), "att" + Integer.toString(index)));
			projectMap.put("att" + Integer.toString(index), tableMap + "." + att.getName());
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
	
	private void traverseAnd(ArrayList<Expression> list, Expression expr){
		if(expr.getType().equals(ExpressionType.And)){
			traverseAnd(list, expr.getRightSubexpression());
			list.add(expr.getLeftSubexpression());
		}else{
			list.add(expr);
		}
		return;
	} 
}