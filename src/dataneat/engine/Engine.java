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
package dataneat.engine;

import dataneat.base.BaseNeat;
import dataneat.data.DataManipulation;
import dataneat.data.SplitDatasetManager;
import dataneat.data.TableData;
import dataneat.evolution.Population;
import dataneat.evolution.SupervisedEvolver;
import dataneat.genome.NeatChromosome;
import dataneat.monitor.FitnessMonitor;
import dataneat.monitor.RunData;
import dataneat.operators.TestFitnessOperator;
import dataneat.utils.ChromTester;
import dataneat.utils.DataManipChecker;
import dataneat.utils.InputParser;
import dataneat.utils.PropertiesHolder;

public class Engine extends BaseNeat {

	private static final String ROUNDS = "roundLimit";
	private static final String DATA_MANIPULATION = "dataManipulation";
	private static final String MANIP_TEST = "dataManipTest";
	private static final String SPLIT = "splitPercent";
	private static final String BOOT_DELAY = "bootDelay";
	private static final String TEST_DELAY = "testDelay";
	private static final String CONSOLE_DELAY = "consoleDelay";

	final String INPUT = "inputFile";
	final String TEST = "testFile";
	final String HEADER = "inputHeader";
	final String NUMCOL = "numCol";
	final String NUMCOLTEST = "numColTest";
	final String NUMOUTPUTS = "numOutputs";

	private RunData runData;
	private SplitDatasetManager data;
	private TableData testData = null;
	private int roundLimit = -1, bootDelay = 1, testDelay = 0, delay, roundsSinceTestUpdate = 0, rounds = 0,
			numColTotal = 0, numOutputs = 0, consoleDelay = 10, sinceConsoleUpdate = 0;
	private Integer dataManip = 0, dataManipTest = 0;
	private DataManipulation manipulation, testManip;
	private FitnessMonitor fitnessSolution;
	private ChromTester chromTester;
	private TestFitnessOperator testFitnessOperator;
	double splitPercent = -1.0;
	boolean done = false;
	private SupervisedEvolver evolver;
	private Population pop;
	private String testFile = null, trainingFile = null, regex = null;

	public Engine(PropertiesHolder p) {
		super(p);
		chromTester = new ChromTester(p);
		testFitnessOperator = new TestFitnessOperator(p);
		fitnessSolution = new FitnessMonitor(p);
		runData = new RunData(p);

		roundLimit = Integer.parseInt(getParams().getProperty(ROUNDS));
		dataManip = Integer.parseInt(getParams().getProperty(DATA_MANIPULATION));
		splitPercent = Double.parseDouble(getParams().getProperty(SPLIT));
		bootDelay = Integer.parseInt(getParams().getProperty(BOOT_DELAY));
		testDelay = Integer.parseInt(getParams().getProperty(TEST_DELAY));
		dataManipTest = Integer.parseInt(getParams().getProperty(MANIP_TEST));
		consoleDelay = Integer.parseInt(getParams().getProperty(CONSOLE_DELAY));
		manipulation = DataManipChecker.checkDataManip(dataManip);
		testManip = DataManipChecker.checkDataManip(dataManipTest);
	}

	public void autoConfig() {
		// assumes the engine will be building the dataset and population from
		// scratch, loads everything from the values provided in the prop file

		// configure IO
		trainingFile = getParams().getProperty(INPUT);
		testFile = getParams().getProperty(TEST, null);
		regex = ",";
		int colTest = Integer.parseInt(getParams().getProperty(NUMCOLTEST));
		boolean header = Boolean.parseBoolean(getParams().getProperty(HEADER));
		numColTotal = Integer.parseInt(getParams().getProperty(NUMCOL));
		numOutputs = Integer.parseInt(getParams().getProperty(NUMOUTPUTS));

		// build training dataset
		data = new SplitDatasetManager(getHolder());
		data.addTrainingDataFromFile(trainingFile, numColTotal, numOutputs, header);

		if (testFile != null) {
			testData = InputParser.parse(header, regex, testFile, colTest, 0).get(0);
		}

		rebuildPop();

		// build evolver
		evolver = new SupervisedEvolver(getHolder());
	}

	public void tuningReconfig() {
		getHolder().load();
		rebuildPop();

		// build evolver
		evolver = new SupervisedEvolver(getHolder());
	}

	private void rebuildPop() {
		// build population
		pop = new Population(getHolder(), data.numInputColumns(), data.numIdealColumns());
	}

	public void run() {
		// assumes the engine should run using the values loaded from the prop
		// file and a freshly built population.
		// Note: must call autoConfig first!

		run(evolver, pop, data);
	}

	public void run(SupervisedEvolver evolver, Population pop, SplitDatasetManager data) {
		// this method contains the main loop which drives evolution

		reset();

		if (manipulation == DataManipulation.RANDOMIZE) {
			// if we only want to randomize the data once before running NEAT,
			// then do it here
			data.shuffleTrainingRows();
		}

		while (!done) {

			dealWithDataManipulation(data);

			// do some pre evolution and update solution monitors
			evolver.preEvolution(pop, data.getTrainingData());
			fitnessSolution.updateTraining(pop.getTrainingBest());

			// see if its time to stop, need to check if we are using a testing
			// set
			if (splitPercent < 0.0 || splitPercent > 1.0) {
				// no dataset splitting requested, so check for stop flag on
				// training set data

				if (/* fitnessSolution.trainingStop() || */ (rounds > roundLimit)) {
					System.out.println("Stop time!");

					done = true;
				}
			} else {
				updateTestdataInfo(pop, data);
			}

			// log some data
			runData.addRound(rounds, pop.getSpeciesDB().getSpeciesList().size(), pop.getBestRaw(),
					pop.getBestTestFitness(), pop.getSpeciesDB().getThreshold());

			updateConsole();

			rounds++;
			delay++;

			// do some evolution
			evolver.evolve(pop);
		}
	}

	private void dealWithDataManipulation(SplitDatasetManager data) {
		// deal with data manipulation requests
		if (manipulation == DataManipulation.CONTINUOUS_RANDOMIZE) {
			// shuffle every generation
			data.shuffleTrainingRows();
		} else {

			if (manipulation == DataManipulation.BOOTSTRAP && delay >= bootDelay) {
				// bootstrap every bootdelay generations
				data.bootstrapTraining();
				delay = 0;
			}
		}
	}

	private void updateTestdataInfo(Population pop, SplitDatasetManager data) {

		if (roundsSinceTestUpdate >= testDelay) {

			roundsSinceTestUpdate = 0;
			// data split is being used, so check for stop flag on the
			// testing set

			switch (testManip) {
			case BOOTSTRAP:
				data.bootstrapTesting();
				break;
			case CONTINUOUS_RANDOMIZE:
				data.shuffleTestingRows();
				break;
			case NONE:
				break;
			case RANDOMIZE:
				data.shuffleTestingRows();
				break;
			default:
				break;

			}

			testFitnessOperator.operate(pop.getChromosomes(), data.getTestingData());
			fitnessSolution.updateTest(pop.getTestBest());

			if (/* fitnessSolution.testingStop() || */ (rounds > roundLimit)) {
				System.out.println("Stop time!");

				NeatChromosome chrom = pop.getTrainingBest();
				chromTester.test(chrom, data.getTestingData());
				done = true;
			}
		} else {
			roundsSinceTestUpdate++;
		}
	}

	private void reset() {
		rounds = 0;
		roundsSinceTestUpdate = 0;
		delay = bootDelay;
		runData.reset();
		fitnessSolution.reset();
		done = false;
	}

	private void updateConsole() {
		if (sinceConsoleUpdate >= consoleDelay) {
			sinceConsoleUpdate = 0;

			System.out.println("Round: " + rounds + " PopSize: " + pop.getPopActualSize() + " Species: "
					+ pop.getSpeciesDB().getSpeciesList().size() + " BestCurrent: " + pop.getBestRaw() + " Moving: "
					+ runData.getMovingAvgTrain() + " BestTest: " + pop.getBestTestFitness());
		} else {
			sinceConsoleUpdate++;
		}
	}

	public void printChrom() {

		// no data split being done, so print best training
		if (splitPercent < 0.0 || splitPercent > 1.0) {
			NeatChromosome chrom = pop.getTrainingBest();
			chromTester.test(chrom, data.getTrainingData());
		} else {
			NeatChromosome chrom = pop.getTestBest();
			chromTester.test(chrom, data.getTestingData());
		}
	}

	public void generatePredictions() {
		// generate output on testing data if present
		if (testFile != null) {
			NeatChromosome chrom = pop.getTrainingBest();
			chromTester.test(chrom, numOutputs, testData);
		}
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
