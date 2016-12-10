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
package dataneat.monitor;

import dataneat.base.BaseNeat;
import dataneat.genome.NeatChromosome;
import dataneat.utils.PropertiesHolder;

public class FitnessMonitor extends BaseNeat {
	// this class keeps track of the best fitness seen so far on both training
	// and test data.

	private static final String MAXIMIZE = "maximize";
	//private static final String ROUND = "roundThreshold";
	//private static final String DELTA = "fitnessDelta";

	private NeatChromosome bestTrainingFitness = null, bestTestFitness = null;
	private int trainingCounter = 0, testCounter = 0;
	boolean timeToStopTraining = false, timeToStopTesting = false;
	private double delta = 0.001;
	private boolean maximize = false;
	private int roundThreshold = 500;

	public FitnessMonitor(PropertiesHolder p) {
		super(p);
		maximize = Boolean.parseBoolean(getParams().getProperty(MAXIMIZE));
	}

	public void updateTraining(NeatChromosome currentBest) {
		if (bestTrainingFitness == null) {
			bestTrainingFitness = currentBest;
		} else {
			if (maximize) {
				maximizeUpdateTrain(currentBest);
			} else {
				minimizeUpdateTrain(currentBest);
			}
		}
	}

	public void updateTest(NeatChromosome currentBest) {
		if (bestTestFitness == null) {
			bestTestFitness = currentBest;
		} else {
			if (maximize) {
				maximizeUpdateTest(currentBest);
			} else {
				minimizeUpdateTest(currentBest);
			}
		}
	}

	private void maximizeUpdateTrain(NeatChromosome currentBest) {
		if (bestTrainingFitness.getFitness() < (currentBest.getFitness() - delta)) {
			bestTrainingFitness = currentBest;
			resetTraining();
		} else {
			trainingCounter++;
			if (trainingCounter > roundThreshold) {
				timeToStopTraining();
			}
		}
	}

	private void minimizeUpdateTrain(NeatChromosome currentBest) {
		if (bestTrainingFitness.getFitness() > (currentBest.getFitness() + delta)) {
			bestTrainingFitness = currentBest;
			resetTraining();
		} else {
			trainingCounter++;
			if (trainingCounter > roundThreshold) {
				timeToStopTraining();
			}
		}
	}

	private void maximizeUpdateTest(NeatChromosome currentBest) {
		if (bestTestFitness.getTestFitness() < (currentBest.getTestFitness() - delta)) {
			bestTestFitness = currentBest;
			resetTest();
		} else {
			testCounter++;
			if (testCounter > roundThreshold) {
				timeToStopTesting();
			}
		}
	}

	private void minimizeUpdateTest(NeatChromosome currentBest) {
		if (bestTestFitness.getTestFitness() > (currentBest.getTestFitness() + delta)) {
			bestTestFitness = currentBest;
			resetTest();
		} else {
			testCounter++;
			if (testCounter > roundThreshold) {
				timeToStopTesting();
			}
		}
	}

	private void timeToStopTesting() {
		if (roundThreshold >= 0) {
			timeToStopTesting = true;
		}
	}

	private void timeToStopTraining() {
		if (roundThreshold >= 0) {
			timeToStopTraining = true;
		}
	}

	public void reset() {
		bestTrainingFitness = null;
		bestTestFitness = null;
		timeToStopTraining = false;
		timeToStopTesting = false;
		trainingCounter = 0;
		testCounter = 0;
	}

	public boolean trainingStop() {
		return timeToStopTraining;
	}

	public boolean testingStop() {
		return timeToStopTesting;
	}

	public NeatChromosome getBestTrainingFitness() {
		return bestTrainingFitness;
	}

	public void setBestTrainingFitness(NeatChromosome globalBestFitness) {
		this.bestTrainingFitness = globalBestFitness;
	}

	public NeatChromosome getBestTestFitness() {
		return bestTestFitness;
	}

	public void setBestTestFitness(NeatChromosome bestTestFitness) {
		this.bestTestFitness = bestTestFitness;
	}

	public int getTrainingCounter() {
		return trainingCounter;
	}

	public void setTrainingCounter(int adjustedCounter) {
		this.trainingCounter = adjustedCounter;
	}

	public int getTestCounter() {
		return testCounter;
	}

	public void setTestCounter(int testCounter) {
		this.testCounter = testCounter;
	}

	private void resetTraining() {
		trainingCounter = 0;
	}

	private void resetTest() {
		testCounter = 0;
	}

	public void setRoundThreshold(int thresh) {
		roundThreshold = thresh;
	}
}
