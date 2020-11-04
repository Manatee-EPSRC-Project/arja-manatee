package us.msu.cse.repair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import us.msu.cse.repair.core.PairGenerator;

public class GeneratePairsMain {
	

	private static void scriptSave(String bugName, String name, String content) {
		Path currentRelativePath = Paths.get("");
		String currentDirectory = currentRelativePath.toAbsolutePath().toString();
		
		Path path = Paths.get(currentDirectory+"/"+bugName);
		if (!Files.exists(path)) {
			new File(currentDirectory+"/"+bugName).mkdir();
		}
		
		content="#!/bin/bash\n" 
				+"java -cp arjaOrig.jar us.msu.cse.repair.Main "
				+ content
				+ ">outputs_two_mp/out_"+name; 
				
				
		Path pathScript = Paths.get(path+"/"+name+".job");

		byte[] strToBytes=content.getBytes();
		try {
			Files.write(pathScript,strToBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String args[]) throws Exception {
		HashMap<String, String> parameterStrs = Interpreter.getParameterStrings(args);
		HashMap<String, Object> parameters = Interpreter.getBasicParameterSetting(parameterStrs);
		

		
		
		
		parameters.put("ingredientFilterRule", false);
		parameters.put("manipulationFilterRule", false);
		parameters.put("ingredientScreenerName", "Simple");
		
		

		
		parameters.put("generatePairsMode", true);
				
		
		PairGenerator problem = new PairGenerator(parameters);
		
		String bugName=problem.getBugName();
		Map<String,String> runScripts=problem.getRunScripts();
		for(Map.Entry<String, String> runScript: runScripts.entrySet()) {
			scriptSave(bugName,runScript.getKey(),runScript.getValue());
		}
		
	}
}
