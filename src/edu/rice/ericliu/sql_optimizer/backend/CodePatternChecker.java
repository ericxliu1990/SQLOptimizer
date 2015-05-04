package edu.rice.ericliu.sql_optimizer.backend;

import java.util.ArrayList;

import edu.rice.ericliu.sql_optimizer.model.Expression;
import edu.rice.ericliu.sql_optimizer.model.LogicCode;
import edu.rice.ericliu.sql_optimizer.model.LogicCode.LogicCodeType;

public class CodePatternChecker {
	ArrayList<LogicCode> codes;
	public CodePatternChecker(ArrayList<LogicCode> codes){
		this.codes = codes;
	}
	
	public void patternCheck(){
		int oldSize = 0;
		do{
			oldSize = codes.size();
			checkLoad();
			checkSelectJoin();
			checkSelectProject();
			checkSelectProjectProject();
			checkJoin();
			checkJoinProject();
			checkJoinProjectProject();
			checkDoubleProjectDoubleAggreationDoubleSelect();
			checkGroupProjectGroupAggreation();
			checkGroupProjectGroupAggreationAggreation();

		}while(oldSize != codes.size() && codes.size() > 1);
	}
		
	private void checkLoad(){
		if(codes.size() < 2){
			return;
		}
		if(!getSecondLast(codes).getType().equals(LogicCodeType.Load)){
			return ;
		}
		if(getLast(codes).isUnary()){
			if(dependencyCheck()){
				getLast(codes).setFirstOp(getSecondLast(codes).getFirstOp());
				codes.remove(codes.size() - 2);
				}
		}
		if(getLast(codes).isBinary()){
			if(dependencyCheck()){
				getLast(codes).setFirstOp(getSecondLast(codes).getFirstOp());
				codes.remove(codes.size() - 2);
			}else if(getLast(codes).getSecondOp().equals(getSecondLast(codes).getTargetOp())){
				getLast(codes).setSecondOp(getSecondLast(codes).getFirstOp());
				codes.remove(codes.size() - 2);
			}
		}

		return;
	}
	
	private void checkGroupProjectGroupAggreation(){
		if(!basicCheck(LogicCodeType.Group, LogicCodeType.Project) &&
			!basicCheck(LogicCodeType.Group, LogicCodeType.Aggreation)){
			return;
		}
		if(dependencyCheck()){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			getSecondLast(codes).setProject(getLast(codes).getCondiction());
			getSecondLast(codes).setType(LogicCodeType.GroupAggreation);
			removeLast();
		}
	}
	private void checkGroupProjectGroupAggreationAggreation(){
		if(!basicCheck(LogicCodeType.GroupAggreation, LogicCodeType.Project) &&
				!basicCheck(LogicCodeType.GroupAggreation, LogicCodeType.Aggreation)){
				return;
			}
		if(dependencyCheck()){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			Expression newCondiction = Expression.combine(getLast(codes).getCondiction(), getSecondLast(codes).getProject());
			getSecondLast(codes).setProject(newCondiction);
			removeLast();
		}
	}
	private void checkSelectProject(){
		if(!basicCheck(LogicCodeType.Select, LogicCodeType.Project)){
			return;
		}
		if(dependencyCheck()){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			getSecondLast(codes).setProject(getLast(codes).getCondiction());
			getSecondLast(codes).setType(LogicCodeType.SelectProject);
			removeLast();
		}
	}
	

	private void checkSelectProjectProject(){
		if(!basicCheck(LogicCodeType.SelectProject, LogicCodeType.Project)){
			return;
		}
		if(dependencyCheck()){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			Expression newCondiction = Expression.combine(getLast(codes).getCondiction(), getSecondLast(codes).getProject());
			getSecondLast(codes).setProject(newCondiction);
			removeLast();
		}
	}
	
	private void checkJoinProject(){
		if(!basicCheck(LogicCodeType.Join, LogicCodeType.Project)){
			return;
		}
		if(dependencyCheck()){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			getSecondLast(codes).setProject(getLast(codes).getCondiction());
			getSecondLast(codes).setType(LogicCodeType.JoinProject);
			removeLast();
		}
	}
	
	private void checkJoinProjectProject(){
		if(!basicCheck(LogicCodeType.JoinProject, LogicCodeType.Project)){
			return;
		}
		if(dependencyCheck()){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			Expression newCondiction = Expression.combine(getLast(codes).getCondiction(), getSecondLast(codes).getProject());
			getSecondLast(codes).setProject(newCondiction);
			removeLast();
		}
	}

	private void checkDoubleProjectDoubleAggreationDoubleSelect(){
		if(!basicCheck(LogicCodeType.Project, LogicCodeType.Project) && 
			!basicCheck(LogicCodeType.Aggreation, LogicCodeType.Aggreation) &&
			!basicCheck(LogicCodeType.Select, LogicCodeType.Select)){
			return;
		}
		if(dependencyCheck()){
			getLast(codes).setFirstOp(getSecondLast(codes).getFirstOp());
			Expression newCondiction = Expression.combine(getLast(codes).getCondiction(), getSecondLast(codes).getCondiction());
			getLast(codes).setCondiction(newCondiction);
			codes.remove(codes.size() - 2);
		}
	}
	
	private void checkSelectJoin(){
		if(!basicCheck(LogicCodeType.Select, LogicCodeType.Join)){
			return;
		}
		if(dependencyCheck()){
			getSecondLast(codes).setTargetOp(getLast(codes).getTargetOp());
			Expression newCondiction = Expression.combine(getLast(codes).getCondiction(), getSecondLast(codes).getCondiction());
			getSecondLast(codes).setCondiction(newCondiction);
			removeLast();
		}
		return;
	}
	
	private void checkJoin(){
		if(!basicCheck(LogicCodeType.Select, LogicCodeType.Product)){
			return;
		}
		if(dependencyCheck()){
			getLast(codes).setFirstOp(getSecondLast(codes).getFirstOp());
			LogicCode newJoin = new LogicCode(LogicCodeType.Join, 
												getLast(codes).getCondiction(), 
												getSecondLast(codes).getFirstOp(),
												getSecondLast(codes).getSecondOp(),
												getLast(codes).getTargetOp());
			removeLast();
			removeLast();
			codes.add(newJoin);
		}
		return;
	}
	private boolean basicCheck(LogicCodeType secondLast, LogicCodeType last){
		return (codes.size() >= 2) && (getLast(codes).getType().equals(last)) &&
				(getSecondLast(codes).getType().equals(secondLast));
	}
	
	private boolean dependencyCheck(){
		return (getLast(codes).getFirstOp().equals(getSecondLast(codes).getTargetOp()));
	}
	
	private void removeLast(){
		codes.remove(codes.size() - 1);
		return;
	}
	private LogicCode getLast(ArrayList<LogicCode> codes){
		return codes.get(codes.size() - 1);
	}
	private LogicCode getSecondLast(ArrayList<LogicCode> codes){
		return codes.get(codes.size() - 2);
	}
	public ArrayList<LogicCode> getCodes() {
		return codes;
	}
}
