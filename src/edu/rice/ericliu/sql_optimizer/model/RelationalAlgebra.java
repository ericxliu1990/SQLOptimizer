package edu.rice.ericliu.sql_optimizer.model;


public class RelationalAlgebra {
	public static enum RAType {
		Projection, Selection, Product, ThetaJoin, Grouping, Aggreation, Table
	};
	private RAType type;
	private String table;
	private Expression value;
	private RelationalAlgebra parent;
	private RelationalAlgebra leftChild;
	private RelationalAlgebra rightChild;
	
	public RelationalAlgebra(RAType type, String table){
		if(!type.equals(RAType.Table)){
			throw new RuntimeException("Only table type is allowed!");
		}
		this.type = type;
		this.table = table;
	}
	public RelationalAlgebra(RAType type, Expression value){
		if(type.equals(RAType.Table)){
			throw new RuntimeException("table type is not allowed!");
		}
		this.type = type;
		this.value = value;
	}
	public RelationalAlgebra(RAType type){
		if(!type.equals(RAType.Product)){
			throw new RuntimeException("Only Product type is allowed!");
		}
		this.type = type;
	}
	public String toString(){
		if(type.equals(RAType.Product)){
			return  getLeftChild().toString() + " X " + getRightChild().toString();
		}
		
		if(this.isUnary()){
			return type.name() + "_" + value.toString() + "[" + getChild().toString() +  "]";
		}
		if(this.isBinary()){
			return type.name() + "_" + value.toString() + "[" + getLeftChild().toString() + ", " + getRightChild() + "]";
		}
		if(this.isTable()){
			return  table;
		}

		throw new RuntimeException("Error type!");
	}
	public RAType getType(){
		return type;
	}
	
	public void setType(RAType type){
		this.type = type;
	}
	public Expression getValue(){
		if(isTable()){
			throw new RuntimeException("table don't have a value");
		}
		return value;
	}
	public String getTable(){
		if(!isTable()){
			throw new RuntimeException("Only table have a value");
		}
		return table;
	}
	public boolean isBinary(){
		if(type == RAType.Product || type == RAType.ThetaJoin ){
			return true;
		}
		return false;
	}
	public boolean isUnary(){
		if(type == RAType.Projection || type == RAType.Selection || type ==  RAType.Grouping || type == RAType.Aggreation){
			return true;
		}
		return false;
	}
	public boolean isTable(){
		if(type == RAType.Table){
			return true;
		}
		return false;
	}
	public RelationalAlgebra getChild(){
		if(this.isUnary()){
			return leftChild;
		}
		throw new RuntimeException("You can't get the child of a RA that is not unary!");
	}
	public void setChild(RelationalAlgebra newChild){
		if(this.isUnary()){
			leftChild = newChild;
			return;
		}
		throw new RuntimeException("You can't set the child of a RA that is not unary!");
	}
	public RelationalAlgebra getLeftChild(){
		if(this.isBinary()){
			return leftChild;
		}
		throw new RuntimeException("You can't get the left child of a RA that is not binary!");
	}
	public RelationalAlgebra getRightChild(){
		if(this.isBinary()){
			return rightChild;
		}
		throw new RuntimeException("You can't get the right child of a RA that is not binary!");
	}
	public void setLeftChild(RelationalAlgebra newChild){
		if(this.isBinary()){
			leftChild = newChild;
			return;
		}
		throw new RuntimeException("You can't set the left child of a RA that is not binary!");
	}
	
	public void setRightChild(RelationalAlgebra newChild){
		if(this.isBinary()){
			rightChild = newChild;
			return;
		}
		throw new RuntimeException("You can't set the right child of a RA that is not binary!");
	}

	public RelationalAlgebra getParent() {
		return parent;
	}

	public void setParent(RelationalAlgebra parent) {
		this.parent = parent;
	}
}

