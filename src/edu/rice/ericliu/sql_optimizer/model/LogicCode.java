package edu.rice.ericliu.sql_optimizer.model;

public class LogicCode {
	public static enum LogicCodeType {
		Load, Product, Select, Project, Group, Join, GroupProject, SelectProject, JoinProject
	}
	private LogicCodeType type;
	private Expression condiction;
	private Expression project;
	private String firstOp;
	private String secondOp;
	private String targetOp;
	public LogicCode(LogicCodeType type, Expression condiction, String firstOp, String secondOp, String targetOp){
		this.type = type;
		this.setCondiction(condiction);
		this.firstOp = firstOp;
		this.secondOp = secondOp;
		this.targetOp = targetOp;
	}
	public LogicCode(LogicCodeType type, Expression condiction, String firstOp,  String targetOp){
		this.type = type;
		this.setCondiction(condiction);
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
		return type.equals(LogicCodeType.Product)|| type.equals(LogicCodeType.Join);
	}
	public boolean isProject(){
		return type.equals(LogicCodeType.GroupProject) || type.equals(LogicCodeType.SelectProject) || type.equals(LogicCodeType.JoinProject);
	}
	public String toString(){
		if(isLoad()){
			return type.name() + " " + firstOp + " => " + targetOp;
		}
		if(isUnary()){
			return type.name() + "(" + getCondiction().toString() + ")" + firstOp + " => " + targetOp;
		}
		if(type.equals(LogicCodeType.Product)){
			return type.name() + " " + firstOp + ", " + secondOp + " => " + targetOp;
		}
		if(type.equals(LogicCodeType.Join)){
			return type.name() + "(" + getCondiction().toString() + ")" + firstOp + ", " + secondOp + " => " + targetOp;
		}
		if(isProject()){
			return type.name() + "(" + getCondiction().toString() + ")(" + getProject().toString() + ")" + firstOp + " => " + targetOp;
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
	public Expression getProject() {
		return project;
	}
	public void setProject(Expression project) {
		this.project = project;
	}

}
