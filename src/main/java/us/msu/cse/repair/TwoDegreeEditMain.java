package us.msu.cse.repair;

import java.util.HashMap;

import jmetal.operators.mutation.Mutation;
import us.msu.cse.repair.algorithms.twoeditdegreerepair.TwoDegreeEditRepair;
import us.msu.cse.repair.core.AbstractRepairAlgorithm;
import us.msu.cse.repair.ec.operators.mutation.ExtendedMutationFactory;
import us.msu.cse.repair.ec.problems.OneTwoEditDegreeProblem;

public class TwoDegreeEditMain {
	public static void main(String args[]) throws Exception {
		HashMap<String, String> parameterStrs = Interpreter.getParameterStrings(args);
		HashMap<String, Object> parameters = Interpreter.getBasicParameterSetting(parameterStrs);
		

		
		
		int mp1;
		int mp2;
		
		if (parameters.containsKey("mp1") && parameters.containsKey("mp2")) {
			mp1 = (int) parameters.get("mp1");
			mp2 = (int) parameters.get("mp2");
		}
		else throw new RuntimeException("mp1, mp2 or mps not provided as a parameter");

		int totalUncompilable=0;
		int totalTimeouted=0;
		int totalComputed=0;
		int totalFailedTests=0;		
		
		

		System.out.println("mp1, mp2: "+ mp1 +", "+ mp2);
		

		

		parameters.put("ingredientFilterRule", false);
		parameters.put("manipulationFilterRule", false);
		parameters.put("ingredientScreenerName", "Simple");
		
		

		
		parameters.put("twoEditDegreeMode", true);
				
		int populationSize = 40;
		int maxGenerations = 50;
		
		String populationSizeS = parameterStrs.get("populationSize");
		if (populationSizeS != null)
			populationSize = Integer.parseInt(populationSizeS);
		
		String maxGenerationsS = parameterStrs.get("maxGenerations");
		if (maxGenerationsS != null)
			maxGenerations = Integer.parseInt(maxGenerationsS);
		
		OneTwoEditDegreeProblem problem = new OneTwoEditDegreeProblem(parameters);
		AbstractRepairAlgorithm repairAlg = new TwoDegreeEditRepair(problem);
		repairAlg.setInputParameter("maxEvaluations", populationSize * maxGenerations);

		
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
		
		System.out.println("totalUncompilable: " + totalUncompilable);
		System.out.println("totalTimeouted: " + totalTimeouted);

		System.out.println("totalComputed: " + totalComputed);

		System.out.println("totalFailedTests:" + totalFailedTests);
		
		
		
	}
}
