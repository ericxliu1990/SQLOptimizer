package edu.rice.ericliu.sql_optimizer;
import java.io.*;

import org.antlr.runtime.*;

import edu.rice.ericliu.sql_optimizer.backend.CodeExecutor;
import edu.rice.ericliu.sql_optimizer.backend.CodeGenerator;
import edu.rice.ericliu.sql_optimizer.frontend.CatalogReader;
import edu.rice.ericliu.sql_optimizer.frontend.SQLLexer;
import edu.rice.ericliu.sql_optimizer.frontend.SQLParser;
import edu.rice.ericliu.sql_optimizer.frontend.SQLSematicChecker;

import java.util.*;

import edu.rice.ericliu.sql_optimizer.model.*;

class Interpreter {
  
  public static void main (String [] args) throws Exception {
    
//    try {
      
      CatalogReader foo = new CatalogReader ("data/Catalog.xml");
      Map <String, TableData> res = foo.getCatalog ();
    System.out.println (foo.printCatalog (res));
      
      InputStreamReader converter = new InputStreamReader(System.in);
      BufferedReader in = new BufferedReader(converter);
      
      System.out.format ("\nSQL>");
      String soFar = in.readLine () + "\n";
      
      // loop forever, or until someone asks to quit
      while (true) {
        
        // keep on reading from standard in until we hit a ";"
        while (soFar.indexOf (';') == -1) {
          soFar += (in.readLine () + "\n");
        }
        
        // split the string
        String toParse = soFar.substring (0, soFar.indexOf (';') + 1);
        soFar = soFar.substring (soFar.indexOf (';') + 1, soFar.length ());
        toParse = toParse.toLowerCase ();
        
        // parse it
        ANTLRStringStream parserIn = new ANTLRStringStream (toParse);
        SQLLexer lexer = new SQLLexer (parserIn);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser (tokens);
        
        // if we got a quit
        if (parser.parse () == false) {
          break;
        } 
        Query currentQuery = new Query(parser.getSELECT (), parser.getFROM (), parser.getWHERE (),parser.getGROUPBY());
        SQLSematicChecker checker = new SQLSematicChecker(res, currentQuery);
        
        if(checker.check()){
        	System.out.println("SQL Sematic Check passed!");
        }
        RelationalAlgebra currentRA = checker.getRA();
//		System.out.println(currentRA.toString());
        CodeGenerator generator = new CodeGenerator(currentRA);
        generator.generate();
//        for(LogicCode code: generator.getNativeCode()){
//        	System.out.println(code.toString());
//        }
        for(LogicCode code: generator.getSimplifiedCode()){
        	System.out.println(code.toString());
        }
        CodeExecutor executor = new CodeExecutor(generator.getSimplifiedCode());
        executor.execute();
        System.out.format ("\nSQL>");
      } 
//    } catch (Exception e) {
//      System.out.println("Error! Exception: " + e); 
//    } 
  }
}
