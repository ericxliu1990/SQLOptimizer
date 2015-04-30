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
			simplifiedCode = patternCheck(codes);
		}
	}
	private ArrayList<LogicCode> patternCheck(ArrayList<LogicCode> codes){
			int oldSize = 0;
			do{
				oldSize = codes.size();
				checkLoad(codes);
				checkDoubleSelect(codes);
				checkSelectJoin(codes);
				checkSelectProject(codes);
				checkSelectProjectProject(codes);
				checkJoin(codes);
				checkJoinProject(codes);
				checkJoinProjectProject(codes);
				checkDoubleProject(codes);
				checkGroupProject(codes);

			}while(oldSize != codes.size() && codes.size() > 1);
			return codes;
	}
	private void checkGroupProject(ArrayList<LogicCode> codes){
		if(codes.size() < 2){
			return;
		}
		if(!getLast(codes).getType().equals(LogicCodeType.Group)){
			return;
		}
		if(!getSecondLast(codes).getType().equals(LogicCodeType.Project)){
			return;
		}
		if(getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp())){
			getLast(codes).setFirstOp(getSecondLast(codes).getFirstOp());
			getLast(codes).setProject(getSecondLast(codes).getCondiction());
			getLast(codes).setType(LogicCodeType.Load.GroupProject);
			codes.remove(codes.size() - 2);
		}
		return;
	}
	private void checkSelectProject(ArrayList<LogicCode> codes){
		if(codes.size() < 2){
			return;
		}
		if(!getLast(codes).getType().equals(LogicCodeType.Project)){
			return;
		}
		if(!getSecondLast(codes).getType().equals(LogicCodeType.Select)){
			return;
		}
		if(getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp())){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			getSecondLast(codes).setProject(getLast(codes).getCondiction());
			getSecondLast(codes).setType(LogicCodeType.SelectProject);
			codes.remove(codes.size() - 1);
		}
		return;
	}
	private void checkJoinProject(ArrayList<LogicCode> codes){
		if(codes.size() < 2){
			return;
		}
		if(!getLast(codes).getType().equals(LogicCodeType.Project)){
			return;
		}
		if(!getSecondLast(codes).getType().equals(LogicCodeType.Join)){
			return;
		}
		if(getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp())){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			getSecondLast(codes).setProject(getLast(codes).getCondiction());
			getSecondLast(codes).setType(LogicCodeType.JoinProject);
			codes.remove(codes.size() - 1);
		}
		return;
	}
	private void checkSelectProjectProject(ArrayList<LogicCode> codes){
		if(codes.size() < 2){
			return;
		}
		if(!getLast(codes).getType().equals(LogicCodeType.Project)){
			return;
		}
		if(!getSecondLast(codes).getType().equals(LogicCodeType.SelectProject)){
			return;
		}
		if(getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp())){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			Expression newCondiction = Expression.combine(getLast(codes).getCondiction(), getSecondLast(codes).getProject());
			getSecondLast(codes).setProject(newCondiction);
			codes.remove(codes.size() - 1);
		}
		return;
	}
	private void checkJoinProjectProject(ArrayList<LogicCode> codes){
		if(codes.size() < 2){
			return;
		}
		if(!getLast(codes).getType().equals(LogicCodeType.Project)){
			return;
		}
		if(!getSecondLast(codes).getType().equals(LogicCodeType.JoinProject)){
			return;
		}
		if(getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp())){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			Expression newCondiction = Expression.combine(getLast(codes).getCondiction(), getSecondLast(codes).getProject());
			getSecondLast(codes).setProject(newCondiction);
			codes.remove(codes.size() - 1);
		}
		return;
	}
	private void checkLoad(ArrayList<LogicCode> codes){
		if(codes.size() < 2){
			return;
		}
		if(!getSecondLast(codes).getType().equals(LogicCodeType.Load)){
			return ;
		}
		if(getLast(codes).isUnary()){
			if(getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp())){
				getLast(codes).setFirstOp(getSecondLast(codes).getFirstOp());
				codes.remove(codes.size() - 2);
				}
		}
		if(getLast(codes).isBinary()){
			if(getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp())){
				getLast(codes).setFirstOp(getSecondLast(codes).getFirstOp());
				codes.remove(codes.size() - 2);
			}else if(getLast(codes).getSecondOp().equals(getSecondLast(codes).getTargetOp())){
				getLast(codes).setSecondOp(getSecondLast(codes).getFirstOp());
				codes.remove(codes.size() - 2);
			}
		}

		return;
	}
	private void checkDoubleSelect(ArrayList<LogicCode> codes){
		if(codes.size() < 2){
			return;
		}
		if(!getLast(codes).getType().equals(LogicCodeType.Select)){
			return;
		}
		if(!getSecondLast(codes).getType().equals(LogicCodeType.Select)){
			return;
		}
		if(getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp())){
			getLast(codes).setFirstOp(getSecondLast(codes).getFirstOp());
			Expression newCondiction = Expression.combine(getLast(codes).getCondiction(), getSecondLast(codes).getCondiction());
			getLast(codes).setCondiction(newCondiction);
			codes.remove(codes.size() - 2);
		}
		return;
	}
	private void checkDoubleProject(ArrayList<LogicCode> codes){
		if(codes.size() < 2){
			return;
		}
		if(!getLast(codes).getType().equals(LogicCodeType.Project)){
			return ;
		}
		if(!getSecondLast(codes).getType().equals(LogicCodeType.Project)){
			return ;
		}
		if(getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp())){
			getLast(codes).setFirstOp(getSecondLast(codes).getFirstOp());
			Expression newCondiction = Expression.combine(getLast(codes).getCondiction(), getSecondLast(codes).getCondiction());
			getLast(codes).setCondiction(newCondiction);
			codes.remove(codes.size() - 2);
		}
		return;
	}
	private void checkSelectJoin(ArrayList<LogicCode> codes){
		if(codes.size() < 2){
			return;
		}
		if(!getLast(codes).getType().equals(LogicCodeType.Select)){
			return;
		}
		if(!getSecondLast(codes).getType().equals(LogicCodeType.Join)){
			return;
		}
		if(getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp())){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			Expression newCondiction = Expression.combine(getLast(codes).getCondiction(), getSecondLast(codes).getCondiction());
			getSecondLast(codes).setCondiction(newCondiction);
			codes.remove(codes.size() - 1);
		}
		return;
	}
	private void checkJoin(ArrayList<LogicCode> codes){
		if(codes.size() < 2){
			return;
		}
		if(!getLast(codes).getType().equals(LogicCodeType.Select)){
			return;
		}
		if(!getSecondLast(codes).getType().equals(LogicCodeType.Product)){
			return;
		}
		if(getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp())){
			getLast(codes).setFirstOp(getSecondLast(codes).getFirstOp());
			LogicCode newJoin = new LogicCode(LogicCodeType.Join, 
												getLast(codes).getCondiction(), 
												getSecondLast(codes).getFirstOp(),
												getSecondLast(codes).getSecondOp(),
												getLast(codes).getTargetOp());
			codes.remove(codes.size() - 1);
			codes.remove(codes.size() - 1);
			codes.add(newJoin);
		}
		return;
	}
	private LogicCode getLast(ArrayList<LogicCode> codes){
		return codes.get(codes.size() - 1);
	}
	private LogicCode getSecondLast(ArrayList<LogicCode> codes){
		return codes.get(codes.size() - 2);
	}
	private String traverse(RelationalAlgebra ra){
		String result, t1, t2;
		if(ra.isBinary()){
			t1 = traverse(ra.getLeftChild());
			t2 = traverse(ra.getRightChild());
			result = getNextTable();
			nativeCode.add(new LogicCode(typeConversion.get(ra.getType()), ra.getValue() , t1, t2, result));
			return result;
		}
		if(ra.isUnary()){
			t1 = traverse(ra.getChild());
			result = getNextTable();
			nativeCode.add(new LogicCode(typeConversion.get(ra.getType()), ra.getValue(), t1, result));
			return result;
		}
		if(ra.isTable()){
			result = getNextTable();
			nativeCode.add(new LogicCode(LogicCodeType.Load, ra.getTable(), result));
			return result;
		}
		throw new RuntimeException("Errow RA type");
	}
	private String getNextTable(){
		return "Table" + Integer.toString(currentTable++);
	}
}
