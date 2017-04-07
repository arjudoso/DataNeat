/*******************************************************************************
 * Copyright [2016] [Ricardo Rivero]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package dataneat.spark;

import org.apache.spark.api.java.JavaRDD;
import org.deeplearning4j.eval.Evaluation;
import org.graphstream.ui.view.Viewer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import dataneat.base.BaseNeat;
import dataneat.evolution.Population;
import dataneat.genome.NeatChromosome;
import dataneat.monitor.RunData;
import dataneat.phenotype.Network;
import dataneat.spark.function1.SparkNetworkAccuracyCurr;
import dataneat.spark.function1.SparkNetworkOutCurr;
import dataneat.spark.function2.SparkEvaluationFunction2;
import dataneat.utils.IO;
import dataneat.utils.PropertiesHolder;
import dataneat.utils.RandGen;

public class SparkEngine extends BaseNeat {

	private static final String TEST_DELAY = "testDelay";
	private static final String CONSOLE_DELAY = "consoleDelay";	
	private static final String COMPLEXITY_THRESH = "complexityThresh";
	private static final String STABIL_THRESH = "stabilizationDelta";
	private static final String BATCH_SIZE = "batchSize";
	
	private double stabilDelta = 0.01;
	private Integer batchSize = 50;
	private INDArray stabilMatrix;
	private double mpc = 0.0, prevMpc = 0.0, mpcBaseline = 0.0, complexityThresh = 0.0;	
	private RunData runData;
	private boolean prune = false;
	private int testDelay = 0, roundsSinceTestUpdate = 0, iteration = 0, numInputs = 0, numOutputs = 0,
			consoleDelay = 10, sinceConsoleUpdate = 0, mpcCounter = 0, mpcThresh = 3;
	private SparkTestFitnessOperator testFitnessOperator;
	private SparkSupervisedEvolver evolver;
	private Population pop;

	public SparkEngine(PropertiesHolder p) {
		super(p);
		runData = new RunData(p);
	}

	public SparkEngine autoConfig(int numInputs, int numOutputs, int batchSize) {
		// assumes the engine will be building the dataset and population from
		// scratch, loads everything from the values provided in the prop file
		testDelay = Integer.parseInt(getParams().getProperty(TEST_DELAY));
		consoleDelay = Integer.parseInt(getParams().getProperty(CONSOLE_DELAY));		
		complexityThresh = Double.parseDouble(getParams().getProperty(COMPLEXITY_THRESH));		
		stabilDelta = Double.parseDouble(getParams().getProperty(STABIL_THRESH));
		testFitnessOperator = new SparkTestFitnessOperator(getHolder());
		this.numInputs = numInputs;
		this.numOutputs = numOutputs;
		rebuildPop(numInputs, numOutputs);
		evolver = new SparkSupervisedEvolver(getHolder());
		return this;
	}

	private void rebuildPop(int numInputs, int numOutputs) {
		// build population
		pop = new Population(getHolder(), numInputs, numOutputs);
	}

	public void runMulti(JavaRDD<DataSet> train, int epochs) {
		mpcBaseline = mpcCalc();
		System.out.println("MPC Base: " + mpcBaseline);

		for (int i = 0; i < epochs; i++) {
			System.out.println("Epoch " + i);
			run(evolver, pop, train);
			reset();
			mpc = mpcCalc();
			System.out.println("MPC: " + mpc);
			mpcCheck();
		}
	}

	public void runMulti(JavaRDD<DataSet> train, JavaRDD<DataSet> test, int epochs) {
		mpcBaseline = mpcCalc();
		System.out.println("MPC Base: " + mpcBaseline);
		for (int i = 0; i < epochs; i++) {
			System.out.println("Epoch " + i);						
			run(evolver, pop, train, test);
			reset();
			mpc = mpcCalc();
			System.out.println("MPC: " + mpc);
			mpcCheck();
		}
	}

	public void run(SparkSupervisedEvolver evolver, Population pop, JavaRDD<DataSet> train, JavaRDD<DataSet> test) {
		// does a single round of evolution

		// do some pre evolution
		evolver.preEvolution(pop, train);

		updateTestdataInfo(pop, test);

		// log some data
		runData.addRound(iteration, pop.getSpeciesDB().getSpeciesList().size(), pop.getTrainingBest().getFitness(),
				pop.getTestBest().getTestFitness(), pop.getSpeciesDB().getThreshold());

		updateConsole();

		iteration++;

		// do some evolution
		if (prune) {
			evolver.prune(pop);
		} else {
			evolver.evolve(pop);
		}
	}

	public void run(SparkSupervisedEvolver evolver, Population pop, JavaRDD<DataSet> train) {
		// does a single round of evolution

		// do some pre evolution and update solution monitors
		evolver.preEvolution(pop, train);

		// log some data
		runData.addRound(iteration, pop.getSpeciesDB().getSpeciesList().size(), pop.getTrainingBest().getFitness(),
				pop.getTestBest().getTestFitness(), pop.getSpeciesDB().getThreshold());

		updateConsole();

		iteration++;

		// do some evolution
		if (prune) {
			evolver.prune(pop);
		} else {
			evolver.evolve(pop);
		}
	}

	private double mpcCalc() {
		double ret = 0.0;
		for (NeatChromosome chrom : pop.getChromosomes()) {
			ret += chrom.getGenomeSize();
		}
		ret /= pop.getPopActualSize();
		return ret;
	}

	private void mpcCheck() {
		if (!prune) {
			// currently not pruning
			if (((mpc - mpcBaseline) > complexityThresh) && pop.getMonitor().isTrainingStag()) {
				// population complexity is above threshold, prune time
				prune = true;
				System.out.println("Prune Time");
			}
		} else {
			// currently pruning
			if ((mpc - prevMpc + 0.01) > 0.0) {
				// pruning not producing lower population complexity
				mpcCounter++;
				System.out.println("Count: " + mpcCounter);
				if (mpcCounter > mpcThresh) {
					mpcCounter = 0;
					prune = false;
					mpcBaseline = mpc;
					System.out.println("Done Pruning");
				}
			} else {
				System.out.println("Still Pruning");
				// pruning is producing lower population complexity
				mpcCounter = 0;
			}
			prevMpc = mpc;
		}
	}

	private void updateTestdataInfo(Population pop, JavaRDD<DataSet> test) {

		if (roundsSinceTestUpdate >= testDelay) {
			roundsSinceTestUpdate = 0;
			testFitnessOperator.operate(pop.getChromosomes(), test);
			pop.updateTestBest();
		} else {
			roundsSinceTestUpdate++;
		}
	}

	public SparkEngine reset() {
		iteration = 0;
		return this;
	}

	public void tuningReset() {
		reset();
		rebuildPop(numInputs, numOutputs);
		long seed = RandGen.getSeed();
		RandGen.rand.setSeed(seed);
		roundsSinceTestUpdate = 0;
	}

	private void updateConsole() {
		if (sinceConsoleUpdate >= consoleDelay) {
			sinceConsoleUpdate = 0;

			System.out.println("Iteration: " + iteration + " PopSize: " + pop.getPopActualSize() + " Species: "
					+ pop.getSpeciesDB().getSpeciesList().size() + " BestCurrent: " + pop.getTrainingBest().getFitness()
					+ " Moving: " + runData.getMovingAvgTrain() + " BestTest: " + pop.getTestBest().getTestFitness());
		} else {
			sinceConsoleUpdate++;
		}
	}

	public JavaRDD<INDArray> generatePredictions(JavaRDD<DataSet> inputs, String mode) {
		NeatChromosome chrom = null;
		switch (mode) {
		case "train":
			chrom = pop.getTrainingBest();
			break;
		case "test":
			chrom = pop.getTestBest();
			break;
		default:
			chrom = pop.getTrainingBest();
		}

		SparkNetworkOutCurr outFunc = new SparkNetworkOutCurr(getHolder());
		outFunc.setChrom(chrom);
		return inputs.map(outFunc);		
	}
	
	public Evaluation combinedEval(JavaRDD<DataSet> inputs, String mode) {
		NeatChromosome chrom = null;
		switch (mode) {
		case "train":
			chrom = pop.getTrainingBest();
			break;
		case "test":
			chrom = pop.getTestBest();
			break;
		default:
			chrom = pop.getTrainingBest();
		}
		
		SparkNetworkAccuracyCurr func = new SparkNetworkAccuracyCurr(getHolder());
		func.setChrom(chrom);
		JavaRDD<Evaluation> evals = inputs.map(func);
		SparkEvaluationFunction2 func2 = new SparkEvaluationFunction2();
		Evaluation combinedEval = evals.reduce(func2);
		return combinedEval;
	}

	public void writePredictions(INDArray inputs, String mode, String file) {
		NeatChromosome chrom = null;
		switch (mode) {
		case "train":
			chrom = pop.getTrainingBest();
			break;
		case "test":
			chrom = pop.getTestBest();
			break;
		default:
			chrom = pop.getTrainingBest();
		}		

		batchSize = inputs.rows();
		getParams().setProperty(BATCH_SIZE, Integer.toString(batchSize));
		stabilMatrix = Nd4j.zeros(batchSize, 1);
		stabilMatrix.addi(stabilDelta);
		Network net = new Network(chrom, getHolder(), stabilMatrix);
		net.computeNetPrevTimestep(inputs);
		IO.addToFile(net.getOutput().toString(), file);
	}

	public void displayNetwork(int id) {
		for (NeatChromosome chrom : pop.getChromosomes()) {
			if (chrom.getId() == id) {
				chrom.buildGraph().display();
				break;
			}
		}
	}

	public void displayBestTrainingNetwork() {
		Viewer v = pop.getTrainingBest().buildGraph().display();
		v.disableAutoLayout();
	}

	public void displayBestTestNetwork() {
		pop.getTestBest().buildGraph().display();
	}

	public RunData getRunData() {
		return runData;
	}

	public void setRunData(RunData runData) {
		this.runData = runData;
	}

	public Population getPop() {
		return pop;
	}
}
