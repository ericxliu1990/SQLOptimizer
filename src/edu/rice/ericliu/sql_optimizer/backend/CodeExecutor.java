package edu.rice.ericliu.sql_optimizer.backend;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.rice.ericliu.sql_optimizer.model.*;
import edu.rice.ericliu.sql_optimizer.model.LogicCode.LogicCodeType;

public class CodeExecutor {
	private Map<LogicCodeType, Method> handler = new HashMap<LogicCodeType, Method>();
	private ArrayList<LogicCode> codes;
	public CodeExecutor(ArrayList<LogicCode> codes){
		try{
			handler.put(LogicCodeType.SelectProject, 
					CodeHandler.class.getDeclaredMethod("handleSelectProject", LogicCode.class));
			handler.put(LogicCodeType.Group, 
					CodeHandler.class.getDeclaredMethod("handleGroup", LogicCode.class));
		}catch(SecurityException e){
			throw new RuntimeException(e.toString());
		}catch(NoSuchMethodException e){
			throw new RuntimeException(e.toString());
		}
		this.codes = codes;
	}
	public void execute(){
		try{
			for(LogicCode code: codes){
				handler.get(code.getType()).invoke(null, code);
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
	public static void handleSelectProject(LogicCode code){
		System.out.println("handleSelectProject Called!");
		
	}
	public static void handleGroup(LogicCode code){
		System.out.println("handleGroup Called!");
	}
}