package us.msu.cse.repair.algorithms.twoeditdegreerepair;

import us.msu.cse.repair.core.AbstractRepairAlgorithm;
import us.msu.cse.repair.core.AbstractRepairProblem;
import us.msu.cse.repair.ec.algorithms.TwoEditDegreeRepair;
import us.msu.cse.repair.ec.problems.GenProgProblem;
import us.msu.cse.repair.ec.problems.OneTwoEditDegreeProblem;

public class TwoDegreeEditRepair extends AbstractRepairAlgorithm {
	public TwoDegreeEditRepair(OneTwoEditDegreeProblem problem) throws Exception {
		algorithm = new TwoEditDegreeRepair(problem);
	}
}
