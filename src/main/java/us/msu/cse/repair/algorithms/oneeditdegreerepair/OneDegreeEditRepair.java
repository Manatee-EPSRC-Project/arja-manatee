package us.msu.cse.repair.algorithms.oneeditdegreerepair;

import us.msu.cse.repair.core.AbstractRepairAlgorithm;
import us.msu.cse.repair.core.AbstractRepairProblem;
import us.msu.cse.repair.ec.algorithms.OneEditDegreeRepair;
import us.msu.cse.repair.ec.algorithms.TwoEditDegreeRepair;
import us.msu.cse.repair.ec.problems.GenProgProblem;
import us.msu.cse.repair.ec.problems.OneTwoEditDegreeProblem;

public class OneDegreeEditRepair extends AbstractRepairAlgorithm {
	public OneDegreeEditRepair(OneTwoEditDegreeProblem problem) throws Exception {
		algorithm = new OneEditDegreeRepair(problem);
	}
}
