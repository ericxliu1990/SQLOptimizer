package edu.rice.ericliu.sql_optimizer.frontend;

import java.util.HashMap;

import edu.rice.ericliu.sql_optimizer.model.Expression.ExpressionType;


public class TypeValidChecker{
	@SuppressWarnings("serial")
	static private final HashMap<String, ExpressionType> notItem = new HashMap<String, ExpressionType>(){{
		put(ExpressionType.Boolean.name(), ExpressionType.Boolean);
	}};
	@SuppressWarnings("serial")
	static private final HashMap<String, ExpressionType> unaryItem = new HashMap<String, ExpressionType>(){{
		put(ExpressionType.Float.name(), ExpressionType.Float);
		put(ExpressionType.Int.name(), ExpressionType.Int);
	}};

	@SuppressWarnings("serial")
	static private final HashMap<String, ExpressionType> plusItem = new HashMap<String, ExpressionType>(){{
		put(ExpressionType.Int.name() + ExpressionType.Int.name(), ExpressionType.Int);
		put(ExpressionType.Int.name() + ExpressionType.Float.name(), ExpressionType.Float);
		put(ExpressionType.Float.name() + ExpressionType.Int.name(), ExpressionType.Float);
		put(ExpressionType.Float.name() + ExpressionType.Float.name(), ExpressionType.Float);
		put(ExpressionType.Int.name() +  ExpressionType.String.name(), ExpressionType.String);
		put(ExpressionType.String.name() + ExpressionType.Int.name(), ExpressionType.String);
		put(ExpressionType.Float.name() + ExpressionType.String.name(), ExpressionType.String);
		put(ExpressionType.String.name() + ExpressionType.Float.name(), ExpressionType.String);
		put(ExpressionType.String.name() + ExpressionType.String.name(), ExpressionType.String);
	}};
	
	@SuppressWarnings("serial")
	static private final HashMap<String, ExpressionType> minusItem = new HashMap<String, ExpressionType>(){{
		put(ExpressionType.Int.name() + ExpressionType.Int.name(), ExpressionType.Int);
		put(ExpressionType.Int.name() + ExpressionType.Float.name(), ExpressionType.Float);
		put(ExpressionType.Float.name() + ExpressionType.Int.name(), ExpressionType.Float);
		put(ExpressionType.Float.name() + ExpressionType.Float.name(), ExpressionType.Float);
	}};

	@SuppressWarnings("serial")
	static private final HashMap<String, ExpressionType> orItem = new HashMap<String, ExpressionType>(){{
		put(ExpressionType.Boolean.name() + ExpressionType.Boolean.name(), ExpressionType.Boolean);
	}};
	
	@SuppressWarnings("serial")
	static private final HashMap<String, ExpressionType> equalsItem = new HashMap<String, ExpressionType>(){{
		put(ExpressionType.Int.name() + ExpressionType.Int.name(), ExpressionType.Boolean);
		put(ExpressionType.Float.name() + ExpressionType.Int.name(), ExpressionType.Boolean);
		put(ExpressionType.Int.name() + ExpressionType.Float.name(), ExpressionType.Boolean);
		put(ExpressionType.Float.name() + ExpressionType.Float.name(), ExpressionType.Boolean);
		put(ExpressionType.String.name() + ExpressionType.String.name(), ExpressionType.Boolean);
	}};
	@SuppressWarnings("serial")
	static private final HashMap<String, ExpressionType> greaterItem = new HashMap<String, ExpressionType>(){{
		put(ExpressionType.Int.name() + ExpressionType.Int.name(), ExpressionType.Boolean);
		put(ExpressionType.Float.name() + ExpressionType.Int.name(), ExpressionType.Boolean);
		put(ExpressionType.Int.name() + ExpressionType.Float.name(), ExpressionType.Boolean);
		put(ExpressionType.Float.name() + ExpressionType.Float.name(), ExpressionType.Boolean);
		put(ExpressionType.String.name() + ExpressionType.String.name(), ExpressionType.Boolean);
	}};
	@SuppressWarnings("serial")
	static private final HashMap<ExpressionType, HashMap<String, ExpressionType>> paraLookupTable = new HashMap<ExpressionType, HashMap<String, ExpressionType>>(){{
		put(ExpressionType.Not, notItem);
		put(ExpressionType.UnaryMinus, unaryItem);
		put(ExpressionType.sum, unaryItem);
		put(ExpressionType.avg, unaryItem);
		put(ExpressionType.Plus, plusItem);
		put(ExpressionType.Minus, minusItem);
		put(ExpressionType.Times, minusItem);
		put(ExpressionType.DividedBy, minusItem);
		put(ExpressionType.Or, orItem);
		put(ExpressionType.And, orItem);
		put(ExpressionType.Equals, equalsItem);
		put(ExpressionType.GreaterThan, greaterItem);
		put(ExpressionType.LessThan, greaterItem);
		
	}};
	
	@SuppressWarnings("serial")
	static private final HashMap<String, ExpressionType> dbTypeConvertTable = new HashMap<String, ExpressionType>(){{
		put("Int", ExpressionType.Int);
		put("Float", ExpressionType.Float);
		put("Str", ExpressionType.String);
	}};
	
	static public ExpressionType Check(ExpressionType type, ExpressionType firstArg, ExpressionType secondArg){
		ExpressionType returnVal;
		returnVal = paraLookupTable.get(type).get(firstArg.name() + secondArg.name());
		if(returnVal == null){
			throw new typeCheckException ("Type " + type + " does not support parameter type " + firstArg.toString() + " and type " + secondArg.toString());
		}
		return returnVal;
	};
	static public ExpressionType Check(ExpressionType type, ExpressionType firstArg){
		ExpressionType returnVal;
		returnVal = paraLookupTable.get(type).get(firstArg.name());
		if(returnVal == null){
			throw new typeCheckException ("Type " + type + " does not support parameter type " + firstArg.toString());
		}
		return returnVal;
	};
	static public ExpressionType typeNameConvert(String dbType){
		return dbTypeConvertTable.get(dbType);
	}
	
}

@SuppressWarnings("serial")
class typeCheckException extends SematicException{
	public typeCheckException(String s) {
		super(s);
	}
}

