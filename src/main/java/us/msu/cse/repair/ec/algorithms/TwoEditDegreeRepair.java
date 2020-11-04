package us.msu.cse.repair.ec.algorithms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.core.Algorithm;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.ec.problems.OneTwoEditDegreeProblem;
import us.msu.cse.repair.ec.variable.Edits;

public class TwoEditDegreeRepair extends Algorithm {

	/**
	 * 
	 */
	
	private int mp1;
	private int mp2;
	
	
	private static final long serialVersionUID = 1L;

	public TwoEditDegreeRepair(OneTwoEditDegreeProblem problem) {
		super(problem);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		// TODO Auto-generated method stub
		int maxEvaluations = ((Integer) this.getInputParameter("maxEvaluations")).intValue();
		SolutionSet population = new SolutionSet(maxEvaluations);


		Set<Integer> compilableIngredientsMp1 = new HashSet<Integer>();
		Set<Integer> compilableIngredientsMp2 = new HashSet<Integer>();

		String manipName1;
		String manipName2;

		
		Solution newIndividual0= new Solution(problem_);
		Edits edits0 = (Edits) newIndividual0.getDecisionVariables()[0];

		List<List<String>> availableManipulations=((OneTwoEditDegreeProblem)problem_).getAvailableManipulations();
		List<ModificationPoint> modificationPoints=((OneTwoEditDegreeProblem)problem_).getModificationPoints();

		
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
					System.out.print("1a: ");
					problem_.evaluate(newIndividual);
					if(newIndividual.getObjective(0) < Double.MAX_VALUE) {
						compilableIngredientsMp1.add(ing1Ind);
					}
					if (newIndividual.getObjective(0) == 0)
						population.add(newIndividual);
					
					
				}				
			}
		}
			
		for(int op2Ind = (int) edits0.getLowerBound(1) ; op2Ind <= (int) edits0.getUpperBound(1) ; op2Ind++) {


			manipName2 = availableManipulations.get(1).get(op2Ind);
			
			if(!manipName2.equals("Delete")) {
				for(int ing2Ind=(int) edits0.getLowerBound(1+edits0.getNumberOfLocations()) ; ing2Ind <= (int) edits0.getUpperBound(1+edits0.getNumberOfLocations()) ; ing2Ind++) {
					Solution newIndividual= new Solution(problem_);
					Edits edits = (Edits) newIndividual.getDecisionVariables()[0];
					edits.getLocList().add(1);
					edits.getOpList().add(op2Ind);
					edits.getIngredList().add(ing2Ind);
					System.out.print("1b: ");
					
					problem_.evaluate(newIndividual);
					if(newIndividual.getObjective(0) < Double.MAX_VALUE) {
						compilableIngredientsMp2.add(ing2Ind);
					}

					if (newIndividual.getObjective(0) == 0)
						population.add(newIndividual);
					
					
				}				
			}			
		
		
	}
		


		//String[] manipulationNames = ((TwoEditDegreeProblem)problem_).getManipulationNames();
		
		int total=0;
		
		System.out.println("Two degree patches");
		
		for(int op1Ind = (int) edits0.getLowerBound(0) ; op1Ind <= (int) edits0.getUpperBound(0) ; op1Ind++) {
			manipName1 = availableManipulations.get(0).get(op1Ind);
			
			if(manipName1.equals("Delete")) {
				for(int op2Ind = (int) edits0.getLowerBound(1) ; op2Ind <= (int) edits0.getUpperBound(1) ; op2Ind++) {
					manipName2 = availableManipulations.get(1).get(op2Ind);
					if(manipName2.equals("Delete")) {
						Solution newIndividual= new Solution(problem_);
						Edits edits = (Edits) newIndividual.getDecisionVariables()[0];
						edits.getLocList().add(0);
						edits.getOpList().add(op1Ind);
						edits.getIngredList().add(0);
						edits.getLocList().add(1);
						edits.getOpList().add(op2Ind);
						edits.getIngredList().add(0);
						total++;
						
						problem_.evaluate(newIndividual);
						if (newIndividual.getObjective(0) == 0)
							population.add(newIndividual);
					}
					else {
						for(int ing2Ind=(int) edits0.getLowerBound(1+edits0.getNumberOfLocations()) ; ing2Ind <= (int) edits0.getUpperBound(1+edits0.getNumberOfLocations()) ; ing2Ind++) {
							Solution newIndividual= new Solution(problem_);
							Edits edits = (Edits) newIndividual.getDecisionVariables()[0];
							edits.getLocList().add(0);
							edits.getOpList().add(op1Ind);
							edits.getIngredList().add(0);
							edits.getLocList().add(1);
							edits.getOpList().add(op2Ind);
							edits.getIngredList().add(ing2Ind);
							
							if(compilableIngredientsMp2.contains(ing2Ind)) {
								total++;
								problem_.evaluate(newIndividual);
								if (newIndividual.getObjective(0) == 0)
									population.add(newIndividual);
							}
						}
					}
				}
					
			}
			else {
				for(int ing1Ind=(int) edits0.getLowerBound(0+edits0.getNumberOfLocations()) ; ing1Ind <= (int) edits0.getUpperBound(0+edits0.getNumberOfLocations()) ; ing1Ind++) {

					for(int op2Ind = (int) edits0.getLowerBound(1) ; op2Ind <= (int) edits0.getUpperBound(1) ; op2Ind++) {
						manipName2 = availableManipulations.get(1).get(op2Ind);
						if(manipName2.equals("Delete")) {
							Solution newIndividual= new Solution(problem_);
							Edits edits = (Edits) newIndividual.getDecisionVariables()[0];
							edits.getLocList().add(0);
							edits.getOpList().add(op1Ind);
							edits.getIngredList().add(ing1Ind);
							edits.getLocList().add(1);
							edits.getOpList().add(op2Ind);
							edits.getIngredList().add(0);
							if(compilableIngredientsMp1.contains(ing1Ind)) {
								total++;

								problem_.evaluate(newIndividual);
								if (newIndividual.getObjective(0) == 0)
									population.add(newIndividual);
							}
						}
						else {
							for(int ing2Ind=(int) edits0.getLowerBound(1+edits0.getNumberOfLocations()) ; ing2Ind <= (int) edits0.getUpperBound(1+edits0.getNumberOfLocations()) ; ing2Ind++) {
								Solution newIndividual= new Solution(problem_);
								Edits edits = (Edits) newIndividual.getDecisionVariables()[0];
								edits.getLocList().add(0);
								edits.getOpList().add(op1Ind);
								edits.getIngredList().add(ing1Ind);
								edits.getLocList().add(1);
								edits.getOpList().add(op2Ind);
								edits.getIngredList().add(ing2Ind);

								if(compilableIngredientsMp1.contains(ing1Ind) && compilableIngredientsMp2.contains(ing2Ind)) {
								
								total++;
									System.out.println("total: " + total);
									System.out.println("mp1 orig: " + modificationPoints.get(0).getStatement().toString());
									System.out.println("mp2 orig: " + modificationPoints.get(1).getStatement().toString());
									
									System.out.println("mp1: " + manipName1 + ":\n" +"ing idx: " +ing1Ind+" "+modificationPoints.get(0).getIngredients().get(ing1Ind).toString());
									System.out.println("mp2: " + manipName2 + ":\n" + "ind idx: "+ing2Ind+" "+modificationPoints.get(1).getIngredients().get(ing2Ind).toString());
									problem_.evaluate(newIndividual);
									System.out.println("----------------------------");

									if (newIndividual.getObjective(0) == 0)
										population.add(newIndividual);
								}
								

							}
						}
					}
				}
			}
		}
	
				
					
		
		System.out.println("Total: " + total);
		return population;
	}


	public int getMp1() {
		return mp1;
	}

	public void setMp1(int mp1) {
		this.mp1 = mp1;
	}

	public int getMp2() {
		return mp2;
	}

	public void setMp2(int mp2) {
		this.mp2 = mp2;
	}

}
