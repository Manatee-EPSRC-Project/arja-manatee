package us.msu.cse.repair;

import java.sql.Timestamp;
import java.util.HashMap;

import jmetal.operators.mutation.Mutation;
import us.msu.cse.repair.ec.algorithms.OneEditDegreeRepair;
import us.msu.cse.repair.ec.operators.mutation.ExtendedMutationFactory;
import us.msu.cse.repair.ec.problems.OneTwoEditDegreeProblem;

public class OneDegreeEditMain {
	public static void main(String args[]) throws Exception {
		
		Integer mp1=0;
		Integer mps=0;
		
		
	
		do {	
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	        System.out.println(timestamp);
			
	    	System.out.println("mp1: "+ mp1);		
			HashMap<String, String> parameterStrs = Interpreter.getParameterStrings(args);
			HashMap<String, Object> parameters = Interpreter.getBasicParameterSetting(parameterStrs);
			
	
			parameters.put("mp1",mp1);
			
			
			int totalUncompilable=0;
			int totalTimeouted=0;
			int totalComputed=0;
			int totalFailedTests=0;		
			
			
	
		
			
	
			
	
			parameters.put("ingredientFilterRule", false);
			parameters.put("manipulationFilterRule", false);
			parameters.put("ingredientScreenerName", "Simple");
			
			
	
			
			parameters.put("oneEditDegreeMode", true);
					
			int populationSize = 40;
			int maxGenerations = 50;
			
			String populationSizeS = parameterStrs.get("populationSize");
			if (populationSizeS != null)
				populationSize = Integer.parseInt(populationSizeS);
			
			String maxGenerationsS = parameterStrs.get("maxGenerations");
			if (maxGenerationsS != null)
				maxGenerations = Integer.parseInt(maxGenerationsS);
			
			OneTwoEditDegreeProblem problem = new OneTwoEditDegreeProblem(parameters);
			OneEditDegreeRepair repairAlg = new OneEditDegreeRepair(problem);
			repairAlg.setInputParameter("maxEvaluations", populationSize * maxGenerations);
	
			if(mps==0) {
				mps=problem.getMps();
			}
			
			problem.setTotalComputed(totalComputed);
			problem.setTotalFailedTests(totalFailedTests);
			problem.setTotalTimeouted(totalTimeouted);
			problem.setTotalUncompilable(totalUncompilable);
			
			Mutation mutation;
			parameters = new HashMap<String, Object>();
			parameters.put("probability", 1.0);
			mutation = ExtendedMutationFactory.getMutationOperator("OneTwoDegreeEditMutation", parameters);
	
			// Add the operators to the algorithm
			repairAlg.addOperator("mutation", mutation);
			
			repairAlg.execute();
	
			
			totalComputed=problem.getTotalComputed();
			totalFailedTests=problem.getTotalFailedTests();
			totalTimeouted=problem.getTotalTimeouted();
			totalUncompilable=problem.getTotalUncompilable();		
			
			
			System.out.println("totalUncompilable: " + totalUncompilable);
			System.out.println("totalTimeouted: " + totalTimeouted);
	
			System.out.println("totalComputed: " + totalComputed);
	
			System.out.println("totalFailedTests:" + totalFailedTests);
		} while(mp1++<mps-1);
	}
}
