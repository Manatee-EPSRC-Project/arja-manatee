package us.msu.cse.repair.ec.algorithms;

import java.util.List;
import jmetal.core.Algorithm;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import us.msu.cse.repair.ec.problems.OneTwoEditDegreeProblem;
import us.msu.cse.repair.ec.variable.Edits;


public class OneEditDegreeRepair extends Algorithm {

	/**
	 * 
	 */
	
	private int mp1;
	
	
	private static final long serialVersionUID = 1L;

	public OneEditDegreeRepair(OneTwoEditDegreeProblem problem) {
		super(problem);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		// TODO Auto-generated method stub
		int maxEvaluations = ((Integer) this.getInputParameter("maxEvaluations")).intValue();
		SolutionSet population = new SolutionSet(maxEvaluations);



		String manipName1;

		
		Solution newIndividual0= new Solution(problem_);
		Edits edits0 = (Edits) newIndividual0.getDecisionVariables()[0];

		List<List<String>> availableManipulations=((OneTwoEditDegreeProblem)problem_).getAvailableManipulations();

		

		
		
		
		System.out.println("One degree patches");
		
		
		for(int op1Ind = (int) edits0.getLowerBound(0) ; op1Ind <= (int) edits0.getUpperBound(0) ; op1Ind++) {
			manipName1 = availableManipulations.get(0).get(op1Ind);
			
			if(!manipName1.equals("Delete")) {
				for(int ing1Ind=(int) edits0.getLowerBound(0+edits0.getNumberOfLocations()) ; ing1Ind <= (int) edits0.getUpperBound(0+edits0.getNumberOfLocations()) ; ing1Ind++) {
					Solution newIndividual= new Solution(problem_);
					Edits edits = (Edits) newIndividual.getDecisionVariables()[0];
					edits.getLocList().add(0);
					edits.getOpList().add(op1Ind);
					edits.getIngredList().add(ing1Ind);

					problem_.evaluate(newIndividual);

					if (newIndividual.getObjective(0) == 0)
						population.add(newIndividual);
					
					
				}
			}
			else {
				Solution newIndividual= new Solution(problem_);
				Edits edits = (Edits) newIndividual.getDecisionVariables()[0];
				edits.getLocList().add(0);
				edits.getOpList().add(op1Ind);
				edits.getIngredList().add(0);
				problem_.evaluate(newIndividual);

				if (newIndividual.getObjective(0) == 0)
					population.add(newIndividual);
				
			}

		}

		return population;
	}



	public int getMp1() {
		return mp1;
	}

	public void setMp1(int mp1) {
		this.mp1 = mp1;
	}


}
