package us.msu.cse.repair.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.tools.JavaFileObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import jmetal.util.Configuration;
import jmetal.util.JMException;
import us.msu.cse.repair.core.compiler.JavaJDKCompiler;
import us.msu.cse.repair.core.coverage.SeedLineGeneratorProcess;
import us.msu.cse.repair.core.faultlocalizer.*;
import us.msu.cse.repair.core.manipulation.AbstractManipulation;
import us.msu.cse.repair.core.manipulation.ManipulationFactory;
import us.msu.cse.repair.core.parser.FileASTRequestorImpl;
import us.msu.cse.repair.core.parser.LCNode;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.SeedStatement;
import us.msu.cse.repair.core.parser.SeedStatementInfo;
import us.msu.cse.repair.core.parser.ingredient.IngredientMode;
import us.msu.cse.repair.core.testexecutors.ExternalTestExecutor;
import us.msu.cse.repair.core.testexecutors.ITestExecutor;
import us.msu.cse.repair.core.testexecutors.InternalTestExecutor;
import us.msu.cse.repair.core.util.ClassFinder;
import us.msu.cse.repair.core.util.CustomURLClassLoader;
import us.msu.cse.repair.core.util.Helper;
import us.msu.cse.repair.core.util.IO;
import us.msu.cse.repair.core.util.Patch;

public class PairGenerator {
	/**
	 * 
	 */

	protected String[] manipulationNames;

	protected Double percentage;
	protected Double thr;

	protected Integer maxNumberOfModificationPoints;

	protected List<ModificationPoint> modificationPoints;
	protected List<List<String>> availableManipulations;

	protected Map<String, CompilationUnit> sourceASTs;
	protected Map<String, String> sourceContents;

	protected Map<SeedStatement, SeedStatementInfo> seedStatements;
	protected Map<String, ITypeBinding> declaredClasses;

	protected Map<LCNode, Double> faultyLines;
	protected String faultyLinesInfoPath;

	protected Set<LCNode> seedLines;

	protected Set<String> positiveTests;
	protected Set<String> negativeTests;

	protected Boolean testFiltered;
	protected String orgPosTestsInfoPath;
	protected String finalTestsInfoPath;

	protected String srcJavaDir;

	protected String binJavaDir;
	protected String binTestDir;
	protected Set<String> dependences;

	protected String externalProjRoot;

	protected String binWorkingRoot;

	protected Set<String> binJavaClasses;
	protected Set<String> binExecuteTestClasses;
	protected String javaClassesInfoPath;
	protected String testClassesInfoPath;

	protected Integer waitTime;

	protected String bugName="";
	protected String patchOutputRoot;

	protected String testExecutorName;

	protected String ingredientScreenerName;
	protected IngredientMode ingredientMode;

	protected Boolean ingredientFilterRule;
	protected Boolean manipulationFilterRule;

	protected Boolean seedLineGenerated;
	
	protected Boolean diffFormat;
	
	protected Boolean twoEditDegreeMode;
	protected Boolean oneEditDegreeMode;

	protected String jvmPath;
	protected List<String> compilerOptions;

	protected URL[] progURLs;
	
	protected String gzoltarDataDir;

	protected static int globalID;
	protected Set<Patch> patches;

	protected static long launchTime;
	protected static int evaluations;
	
	protected Integer mps=0;
	
	protected Map<String,String> runScripts;
	
	public Map<String,String> getRunScripts() {
		return Collections.unmodifiableMap(runScripts);
	}

	public Integer getMps() {
		return mps;
	}

	private boolean mpsInOneClass=false;
	
	
	public boolean getMpsInOneClass() {
		return mpsInOneClass;
	}

	public String getBugName() {
		return bugName;
	}
	
	private String extractBugName() throws Exception {
		int bugNameBeg=srcJavaDir.indexOf("/bugs/")+"/bugs/".length();
		if(bugNameBeg==-1 ) {
			throw new Exception("Wrong name of srcJavaDir");
		}
		int bugNameEnd=srcJavaDir.indexOf("/", bugNameBeg);
		if(bugNameEnd == -1) {
			throw new Exception("Wrong name of srcJavaDir");
		}
		String bugName=srcJavaDir.substring(bugNameBeg,bugNameEnd);
		return bugName;
		
	}
	
	@SuppressWarnings("unchecked")
	public PairGenerator(Map<String, Object> parameters) throws Exception {
		binJavaDir = (String) parameters.get("binJavaDir");
		binTestDir = (String) parameters.get("binTestDir");
		srcJavaDir = (String) parameters.get("srcJavaDir");
		dependences = (Set<String>) parameters.get("dependences");

		percentage = (Double) parameters.get("percentage");

		javaClassesInfoPath = (String) parameters.get("javaClassesInfoPath");
		testClassesInfoPath = (String) parameters.get("testClassesInfoPath");
		
		faultyLinesInfoPath = (String) parameters.get("faultyLinesInfoPath");
	
		gzoltarDataDir = (String) parameters.get("gzoltarDataDir");
		
		String id = Helper.getRandomID();
		
		thr = (Double) parameters.get("thr");
		if (thr == null)
			thr = 0.1;
		
		maxNumberOfModificationPoints = (Integer) parameters.get("maxNumberOfModificationPoints");
		if (maxNumberOfModificationPoints == null)
			maxNumberOfModificationPoints = 40;

		jvmPath = (String) parameters.get("jvmPath");
		if (jvmPath == null)
			jvmPath = System.getProperty("java.home") + "/bin/java";

		externalProjRoot = (String) parameters.get("externalProjRoot");
		if (externalProjRoot == null)
			externalProjRoot = new File("external").getCanonicalPath();

		binWorkingRoot = (String) parameters.get("binWorkingRoot");
		if (binWorkingRoot == null)
			binWorkingRoot = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "working_" + id;
		
		patchOutputRoot = (String) parameters.get("patchOutputRoot");
		if (patchOutputRoot == null)
			patchOutputRoot = "patches_" + id;
		
		orgPosTestsInfoPath = (String) parameters.get("orgPosTestsInfoPath");
		if (orgPosTestsInfoPath == null)
			orgPosTestsInfoPath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "orgTests_" + id + ".txt";
		
		finalTestsInfoPath = (String) parameters.get("finalTestsInfoPath");
		if (finalTestsInfoPath == null)
			finalTestsInfoPath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "finalTests_" + id + ".txt";

		manipulationNames = (String[]) parameters.get("manipulationNames");
		if (manipulationNames == null)
			manipulationNames = new String[] { "Delete", "Replace", "InsertBefore" };

		testExecutorName = (String) parameters.get("testExecutorName");
		if (testExecutorName == null)
			testExecutorName = "ExternalTestExecutor";

		ingredientScreenerName = (String) parameters.get("ingredientScreenerName");
		if (ingredientScreenerName == null)
			ingredientScreenerName = "Direct";

		String modeStr = (String) parameters.get("ingredientMode");
		if (modeStr == null)
			ingredientMode = IngredientMode.Package;
		else
			ingredientMode = IngredientMode.valueOf(modeStr);
		
		diffFormat = (Boolean) parameters.get("diffFormat");
		if (diffFormat == null)
			diffFormat = false;
		

		testFiltered = (Boolean) parameters.get("testFiltered");
		if (testFiltered == null)
			testFiltered = true;

		waitTime = (Integer) parameters.get("waitTime");
		if (waitTime == null)
			waitTime = 6000;

		seedLineGenerated = (Boolean) parameters.get("seedLineGenerated");
		if (seedLineGenerated == null)
			seedLineGenerated = true;

		manipulationFilterRule = (Boolean) parameters.get("manipulationFilterRule");
		if (manipulationFilterRule == null)
			manipulationFilterRule = true;

		ingredientFilterRule = (Boolean) parameters.get("ingredientFilterRule");
		if (ingredientFilterRule == null)
			ingredientFilterRule = true;

		
		oneEditDegreeMode = (Boolean) parameters.get("oneEditDegreeMode");
		if (oneEditDegreeMode == null)
			oneEditDegreeMode = false;

		
		twoEditDegreeMode = (Boolean) parameters.get("twoEditDegreeMode");
		if (twoEditDegreeMode == null)
			twoEditDegreeMode = false;
		
		
		
		
		
		runScripts=new HashMap<String, String>();

		checkParameters();
		invokeModules();

		globalID = 0;
		evaluations = 0;
		launchTime = System.currentTimeMillis();
		patches = new HashSet<Patch>();
		
		
	}

	void checkParameters() throws Exception {
		if (binJavaDir == null)
			throw new Exception("The build directory of Java classes is not specified!");
		else if (binTestDir == null)
			throw new Exception("The build directory of test classes is not specified!");
		else if (srcJavaDir == null)
			throw new Exception("The directory of Java source code is not specified!");
		else if (dependences == null)
			throw new Exception("The dependences of the buggy program is not specified!");
		else if (!(new File(jvmPath).exists()))
			throw new Exception("The JVM path does not exist!");
		else if (!(new File(externalProjRoot).exists()))
			throw new Exception("The directory of external project does not exist!");
	}

	void invokeModules() throws Exception {
		invokeClassFinder();
		invokeFaultLocalizer();
		invokeSeedLineGenerator();
		invokeASTRequestor();
	}

	void invokeClassFinder() throws ClassNotFoundException, IOException {
		ClassFinder finder = new ClassFinder(binJavaDir, binTestDir, dependences);
		binJavaClasses = finder.findBinJavaClasses();
		binExecuteTestClasses = finder.findBinExecuteTestClasses();

		if (javaClassesInfoPath != null)
			FileUtils.writeLines(new File(javaClassesInfoPath), binJavaClasses);
		if (testClassesInfoPath != null)
			FileUtils.writeLines(new File(testClassesInfoPath), binExecuteTestClasses);
	}

	void invokeFaultLocalizer() throws FileNotFoundException, IOException {
		System.out.println("Fault localization starts...");
		IFaultLocalizer faultLocalizer;
		if (gzoltarDataDir == null)
			faultLocalizer = new GZoltarFaultLocalizer(binJavaClasses, binExecuteTestClasses, binJavaDir, binTestDir,
					dependences);
		else
			faultLocalizer = new GZoltarFaultLocalizer2(gzoltarDataDir);

		faultyLines = faultLocalizer.searchSuspicious(thr);

		positiveTests = faultLocalizer.getPositiveTests();
		negativeTests = faultLocalizer.getNegativeTests();

		if (orgPosTestsInfoPath != null)
			FileUtils.writeLines(new File(orgPosTestsInfoPath), positiveTests);

		System.out.println("Number of positive tests: " + positiveTests.size());
		System.out.println("Number of negative tests: " + negativeTests.size());
		
		System.out.println("Negative tests:");

		for (String file: negativeTests) {
			System.out.println(file);
		}
		
		System.out.println("Fault localization is finished!");
	}

	void invokeSeedLineGenerator() throws IOException, InterruptedException {
		if (seedLineGenerated) {
			SeedLineGeneratorProcess slgp = new SeedLineGeneratorProcess(binJavaClasses, javaClassesInfoPath,
					binExecuteTestClasses, testClassesInfoPath, binJavaDir, binTestDir, dependences, externalProjRoot,
					jvmPath);
			seedLines = slgp.getSeedLines();
		} else
			seedLines = null;
	}

	
	
	
	@SuppressWarnings("unused")
	private boolean areMpsInOneClass(ModificationPoint mp1,ModificationPoint mp2) {
		if(mp1.getClass().equals(mp2.getClass())) {
			return true;
		}
		else {
			return false;
		}
	}

	
	@SuppressWarnings({ "rawtypes" })
	void invokeASTRequestor() throws Exception {
		
		System.out.println("AST parsing starts...");
		
		modificationPoints = new ArrayList<ModificationPoint>();
		seedStatements = new HashMap<SeedStatement, SeedStatementInfo>();
		sourceASTs = new HashMap<String, CompilationUnit>();
		sourceContents = new HashMap<String, String>();
		declaredClasses = new HashMap<String, ITypeBinding>();

		FileASTRequestorImpl requestor = new FileASTRequestorImpl(faultyLines, seedLines, modificationPoints,
				seedStatements, sourceASTs, sourceContents, declaredClasses);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String[] classpathEntries = null;
		if (dependences != null)
			classpathEntries = dependences.toArray(new String[dependences.size()]);

		parser.setEnvironment(classpathEntries, new String[] { srcJavaDir }, null, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);

		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		parser.setCompilerOptions(options);

		File srcFile = new File(srcJavaDir);
		Collection<File> javaFiles = FileUtils.listFiles(srcFile, new SuffixFileFilter(".java"),
				TrueFileFilter.INSTANCE);
		String[] sourceFilePaths = new String[javaFiles.size()];

		int i = 0;
		for (File file : javaFiles)
			sourceFilePaths[i++] = file.getCanonicalPath();

		parser.createASTs(sourceFilePaths, null, new String[] { "UTF-8" }, requestor, null);

		mps=modificationPoints.size();
		System.out.println("MPs: " + mps);
		
		
		for(int ind=0;ind<modificationPoints.size();ind++) {
			System.out.println(ind+": "+modificationPoints.get(ind).getLCNode().getClassName()+"   "+modificationPoints.get(ind).getLCNode().getLineNumber());
		}


		
		if(mps==0) {
			mps=modificationPoints.size();
		}

		bugName=extractBugName();
		
		for(int mp1=0;mp1<mps-2;mp1++) {
			for(int mp2=mp1+1;mp2<mps-1;mp2++) {
			

	
				
				
				if(areMpsInOneClass(modificationPoints.get(mp1), modificationPoints.get(mp2))) {
					String script="TwoDegreeEdit" +" ";
					script+="-DsrcJavaDir " + srcJavaDir +" ";
					script+="-DbinJavaDir " + binJavaDir +" ";
					script+="-DbinTestDir " + binTestDir +" ";
					script+="-Ddependences " ;
					String scriptDependences="";
					for (String dep: dependences) {
						if(scriptDependences.length()>0) {
							scriptDependences+=":";	
						}
						scriptDependences+=dep;						
					}
					script+=scriptDependences +" ";
					script+="-DpatchOutputRoot " + patchOutputRoot +" ";
					script+="-DpatchOutputRoot " + patchOutputRoot +" ";
					script+="-Dthr " + thr.toString() +" ";
					script+="-DmaxNumberOfModificationPoints " + maxNumberOfModificationPoints.toString() +" ";
					script+="-DexternalProjRoot " + externalProjRoot + " ";
					if(gzoltarDataDir != null && gzoltarDataDir.length()>0) {
						script+="-DgzoltarDataDir " + gzoltarDataDir +" ";
					}
					script+="-Dmp1 " + String.valueOf(mp1) + " ";
					script+="-Dmp2 " + String.valueOf(mp2) + " ";
					script+="-Dmps " + String.valueOf(mps) + " ";
					
					
					runScripts.put(bugName+"_"+String.valueOf(mp1)+"_"+String.valueOf(mp2), script);
					
				}
			
		
			}
		}
		
		System.out.println("AST parsing is finished!");
	}










	protected Map<String, String> getModifiedJavaSources(Map<String, ASTRewrite> astRewriters) {
		Map<String, String> javaSources = new HashMap<String, String>();

		for (Entry<String, ASTRewrite> entry : astRewriters.entrySet()) {
			String sourceFilePath = entry.getKey();
			String content = sourceContents.get(sourceFilePath);

			Document doc = new Document(content);
			TextEdit edits = entry.getValue().rewriteAST(doc, null);

			try {
				edits.apply(doc);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			javaSources.put(sourceFilePath, doc.get());
		}
		return javaSources;
	}

	protected boolean manipulateOneModificationPoint(ModificationPoint mp, String manipName, Statement ingredStatement,
			Map<String, ASTRewrite> astRewriters) throws JMException {
		String sourceFilePath = mp.getSourceFilePath();
		ASTRewrite rewriter;
		if (astRewriters.containsKey(sourceFilePath))
			rewriter = astRewriters.get(sourceFilePath);
		else {
			CompilationUnit unit = sourceASTs.get(sourceFilePath);
			rewriter = ASTRewrite.create(unit.getAST());
			astRewriters.put(sourceFilePath, rewriter);
		}

		AbstractManipulation manipulation = ManipulationFactory.getManipulation(manipName, mp, ingredStatement,
				rewriter);
		return manipulation.manipulate();
	}

	protected Map<String, JavaFileObject> getCompiledClassesForTestExecution(Map<String, String> javaSources) {
		JavaJDKCompiler compiler = new JavaJDKCompiler(ClassLoader.getSystemClassLoader(), compilerOptions);
		try {
			boolean isCompiled = compiler.compile(javaSources);
			if (isCompiled)
				return compiler.getClassLoader().getCompiledClasses();
			else
				return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	protected ITestExecutor getTestExecutor(Map<String, JavaFileObject> compiledClasses, Set<String> executePosTests)
			throws JMException, IOException {
		if (testExecutorName.equalsIgnoreCase("ExternalTestExecutor")) {
			File binWorkingDirFile = new File(binWorkingRoot, "bin_" + (globalID++));
			IO.saveCompiledClasses(compiledClasses, binWorkingDirFile);
			String binWorkingDir = binWorkingDirFile.getCanonicalPath();
			String tempPath = (executePosTests == positiveTests) ? finalTestsInfoPath : null;
			return new ExternalTestExecutor(executePosTests, negativeTests, tempPath, binJavaDir, binTestDir,
					dependences, binWorkingDir, externalProjRoot, jvmPath, waitTime);

		} else if (testExecutorName.equalsIgnoreCase("InternalTestExecutor")) {
			CustomURLClassLoader urlClassLoader = new CustomURLClassLoader(progURLs, compiledClasses);
			return new InternalTestExecutor(executePosTests, negativeTests, urlClassLoader, waitTime);
		} else {
			Configuration.logger_.severe("test executor name '" + testExecutorName + "' not found ");
			throw new JMException("Exception in getTestExecutor()");
		}
	}

	protected Set<String> getSamplePositiveTests() {
		if (percentage == null || percentage == 1)
			return positiveTests;
		else {
			int num = (int) (positiveTests.size() * percentage);
			List<String> tempList = new ArrayList<String>(positiveTests);
			Collections.shuffle(tempList);
			Set<String> samplePositiveTests = new HashSet<String>();
			for (int i = 0; i < num; i++)
				samplePositiveTests.add(tempList.get(i));
			return samplePositiveTests;
		}
	}

	public List<List<String>> getAvailableManipulations() {
		return this.availableManipulations;
	}

	public List<ModificationPoint> getModificationPoints() {
		return this.modificationPoints;
	}

	public String[] getManipulationNames() {
		return this.manipulationNames;
	}

	public Map<String, CompilationUnit> getSourceASTs() {
		return this.sourceASTs;
	}

	public Map<String, String> getSourceContents() {
		return this.sourceContents;
	}

	public Set<String> getNegativeTests() {
		return this.negativeTests;
	}

	public Set<String> getPositiveTests() {
		return this.positiveTests;
	}

	public Double getPercentage() {
		return this.percentage;
	}

	public void saveTestAdequatePatch(List<Integer> opList, List<Integer> locList, List<Integer> ingredList)
			throws IOException {
		long estimatedTime = System.currentTimeMillis() - launchTime;
		if (patchOutputRoot != null)
			IO.savePatch(opList, locList, ingredList, modificationPoints, availableManipulations, patchOutputRoot,
					globalID, evaluations, estimatedTime);
	}

	public boolean addTestAdequatePatch(List<Integer> opList, List<Integer> locList, List<Integer> ingredList) {
		Patch patch = new Patch(opList, locList, ingredList, modificationPoints, availableManipulations);
		return patches.add(patch);
	}

	public String getSrcJavaDir() {
		return this.srcJavaDir;
	}

	public String getBinJavaDir() {
		return this.binJavaDir;
	}

	public String getBinTestDir() {
		return this.binTestDir;
	}

	public Set<String> getDependences() {
		return this.dependences;
	}

	public int getNumberOfModificationPoints() {
		return modificationPoints.size();
	}

	public Set<Patch> getPatches() {
		return this.patches;
	}

	public void clearPatches() {
		patches.clear();
	}

	public void resetPatchOutputRoot(String patchOutputRoot) {
		this.patchOutputRoot = patchOutputRoot;
	}


	public void resetBinWorkingRoot(String binWorkingRoot) {
		this.binWorkingRoot = binWorkingRoot;
	}

	public static void resetGlobalID(int id) {
		globalID = id;
	}

	public static void increaseGlobalID() {
		globalID++;
	}

	public static void resetLaunchTime(long time) {
		launchTime = time;
	}

	public static long getLaunchTime() {
		return launchTime;
	}

	public static void resetEvaluations(int evals) {
		evaluations = evals;
	}

	public static int getEvaluations() {
		return evaluations;
	}
	
	public String getBinWorkingRoot() {
		return binWorkingRoot;
	}
	
	public String getOrgPosTestsInfoPath() {
		return orgPosTestsInfoPath;
	}
	
	public String getFinalTestsInfoPath() {
		return finalTestsInfoPath;
	}

}
