package edu.rice.ericliu.sql_optimizer.model;
import java.util.ArrayList;
import java.util.Map;


public class Query {
	public ArrayList<Expression> select;
	public Map<String, String> from;
	public Expression where;
	public ArrayList<String> groupby;
	
	private boolean isAggregateClause = false;
	public Query(ArrayList<Expression> select, Map<String, String> from,
			Expression where, ArrayList<String> groupby) {
		super();
		this.select = select;
		this.from = from;
		this.where = where;
		this.groupby = groupby;
	}
	@Override
	public String toString() {
		  String returnStr;
	      returnStr = "Expressions in SELECT:\n";
	      
	      for (Expression e : select)
	        returnStr = returnStr + "\t" + e.toString () + "\n";
	      
	      returnStr += "Tables in FROM:\n";
	      
	      returnStr = returnStr + "\t" + from + "\n";
	      
	      returnStr += "WHERE clause:\n";
	      
	      if (where != null)
	        returnStr = returnStr + "\t" + where.toString () + "\n";
	      
	      returnStr = returnStr + "GROUPING atts:" + "\n";
	      for (String att : groupby) {
	        returnStr = returnStr + "\t" + att + "\n";
	      }
	      return returnStr;
	}
	public boolean isSimple(){
		if(from.size() == 1){
			return true;
		}
		return false;
	}
	public boolean isAggregate(){
		return isAggregateClause;
	}
}
