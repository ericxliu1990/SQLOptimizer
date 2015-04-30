package edu.rice.ericliu.sql_optimizer.model;

public class LogicCode {
	public static enum LogicCodeType {
		Load, Product, Select, Project, Group,
	}
	private LogicCodeType type;
	private Expression condiction;
	private String firstOp;
	private String secondOp;
	private String targetOp;
	public LogicCode(LogicCodeType type, Expression condiction, String firstOp, String secondOp, String targetOp){
		this.type = type;
		this.condiction = condiction;
		this.firstOp = firstOp;
		this.secondOp = secondOp;
		this.targetOp = targetOp;
	}
	public LogicCode(LogicCodeType type, Expression condiction, String firstOp,  String targetOp){
		this.type = type;
		this.condiction = condiction;
		this.firstOp = firstOp;
		this.targetOp = targetOp;
	}
	public LogicCode(LogicCodeType type, String firstOp,  String targetOp){
		this.type = type;
		this.firstOp = firstOp;
		this.targetOp = targetOp;
	}
	public boolean isLoad(){
		return type.equals(LogicCodeType.Load);
	}
	public boolean isUnary(){
		return type.equals(LogicCodeType.Group) || type.equals(LogicCodeType.Project) || type.equals(LogicCodeType.Select);
	}
	public boolean isBinary(){
		return type.equals(LogicCodeType.Product);
	}
	public String toString(){
		if(isLoad()){
			return type.name() + " " + firstOp + " => " + targetOp;
		}
		if(isUnary()){
			return type.name() + "(" + condiction.toString() + ")" + firstOp + " => " + targetOp;
		}
		if(isBinary()){
			return type.name() + " " + firstOp + ", " + secondOp + " => " + targetOp;
		}
		throw new RuntimeException("Logic Code exception");
	}
	public LogicCodeType getType() {
		return type;
	}

	public void setType(LogicCodeType type) {
		this.type = type;
	}
	public String getSecondOp() {
		return secondOp;
	}
	public void setSecondOp(String secondOp) {
		this.secondOp = secondOp;
	}
	public Expression getCondiction() {
		return condiction;
	}
	public void setCondiction(Expression condiction) {
		this.condiction = condiction;
	}
	public String getFirstOp() {
		return firstOp;
	}
	public void setFirstOp(String firstOp) {
		this.firstOp = firstOp;
	}
	public String getTargetOp() {
		return targetOp;
	}
	public void setTargetOp(String targetOp) {
		this.targetOp = targetOp;
	}

}
