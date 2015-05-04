package edu.rice.ericliu.sql_optimizer.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.rice.ericliu.sql_optimizer.model.*;
import edu.rice.ericliu.sql_optimizer.model.LogicCode.LogicCodeType;
import edu.rice.ericliu.sql_optimizer.model.RelationalAlgebra.RAType;

public class CodeGenerator {
	private static final HashMap<RAType, LogicCodeType> typeConversion = new HashMap<RAType, LogicCodeType>(){{
		put(RAType.Product, LogicCodeType.Product);
		put(RAType.Projection, LogicCodeType.Project);
		put(RAType.Selection, LogicCodeType.Select);
		put(RAType.Grouping, LogicCodeType.Group);
		put(RAType.Aggreation, LogicCodeType.Aggreation);
	}};
	private RelationalAlgebra myRa;
	private ArrayList<LogicCode> nativeCode = new ArrayList<LogicCode>();
	private ArrayList<LogicCode> simplifiedCode = new ArrayList<LogicCode>();
	private int currentTable = 0;
	public CodeGenerator(RelationalAlgebra ra){
		this.myRa = ra;
	}
	public void generate(){
		traverse(myRa);
		simplifier();
	}
	public ArrayList<LogicCode> getNativeCode(){
		return nativeCode;
	}
	public ArrayList<LogicCode> getSimplifiedCode(){
		return simplifiedCode;
	}
	private void simplifier(){
		ArrayList<LogicCode> codes = new ArrayList<LogicCode>();
		for(LogicCode code: nativeCode){
			codes.add(code);
			CodePatternChecker checker = new CodePatternChecker(codes);
			checker.patternCheck();
			simplifiedCode = checker.getCodes();
		}
	}

	private String traverse(RelationalAlgebra ra){
		String result, t1, t2;
		if(ra.isBinary()){
			t1 = traverse(ra.getLeftChild());
			t2 = traverse(ra.getRightChild());
			result = getNextTable();
			nativeCode.add(new LogicCode(typeConversion.get(ra.getType()), ra.getValue() , t1, t2, result));
			nativeCode.get(nativeCode.size() - 1);
			return result;
		}
		if(ra.isUnary()){
			t1 = traverse(ra.getChild());
			result = getNextTable();
			nativeCode.add(new LogicCode(typeConversion.get(ra.getType()), ra.getValue(), t1, result));
			nativeCode.get(nativeCode.size() - 1);
			return result;
		}
		if(ra.isTable()){
			result = getNextTable();
			nativeCode.add(new LogicCode(LogicCodeType.Load, ra.getTable(), result));
			nativeCode.get(nativeCode.size() - 1);
			return result;
		}
		throw new RuntimeException("Errow RA type");
	}
	private String getNextTable(){
		return "Table" + Integer.toString(currentTable++);
	}
}
