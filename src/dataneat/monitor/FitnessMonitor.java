package dataneat.monitor;

import dataneat.base.BaseNeat;
import dataneat.genome.NeatChromosome;
import dataneat.utils.PropertiesHolder;

public class FitnessMonitor extends BaseNeat {
	//general class for tracking if fitness is stagnant
	//also used to keep track of best training or testing score seen

	private static final String MAXIMIZE = "maximize";
	
	private NeatChromosome bestTrainingFitness = null, bestTestFitness = null;
	private int trainingCounter = 0, testCounter = 0;
	boolean trainingStagnant = false, testingStagnant = false;
	private double delta = 0.001;
	private boolean maximize = false;
	private int roundThreshold = 50;

	public FitnessMonitor(PropertiesHolder p) {
		super(p);
		maximize = Boolean.parseBoolean(getParams().getProperty(MAXIMIZE));
	}

	public void updateTraining(NeatChromosome currentBest) {
		if (bestTrainingFitness == null) {
			bestTrainingFitness = new NeatChromosome(currentBest);
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
			bestTestFitness = new NeatChromosome(currentBest);
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
			bestTrainingFitness = new NeatChromosome(currentBest);
			resetTraining();
		} else {
			trainingCounter++;
			if (trainingCounter > roundThreshold) {
				trainingStagnant();
			}
		}
	}

	private void minimizeUpdateTrain(NeatChromosome currentBest) {
		if (bestTrainingFitness.getFitness() > (currentBest.getFitness() + delta)) {
			bestTrainingFitness = new NeatChromosome(currentBest);
			resetTraining();
		} else {
			trainingCounter++;
			if (trainingCounter > roundThreshold) {
				trainingStagnant();
			}
		}
	}

	private void maximizeUpdateTest(NeatChromosome currentBest) {
		if (bestTestFitness.getTestFitness() < (currentBest.getTestFitness() - delta)) {
			bestTestFitness = new NeatChromosome(currentBest);
			resetTest();
		} else {
			testCounter++;
			if (testCounter > roundThreshold) {
				testingStagnant();
			}
		}
	}

	private void minimizeUpdateTest(NeatChromosome currentBest) {
		if (bestTestFitness.getTestFitness() > (currentBest.getTestFitness() + delta)) {
			bestTestFitness = new NeatChromosome(currentBest);
			resetTest();
		} else {
			testCounter++;
			if (testCounter > roundThreshold) {
				testingStagnant();
			}
		}
	}

	private void testingStagnant() {
		if (roundThreshold >= 0) {
			testingStagnant = true;
		}
	}

	private void trainingStagnant() {
		if (roundThreshold >= 0) {
			trainingStagnant = true;
		}
	}

	public void reset() {
		bestTrainingFitness = null;
		bestTestFitness = null;
		trainingStagnant = false;
		testingStagnant = false;
		trainingCounter = 0;
		testCounter = 0;
	}

	public boolean isTrainingStag() {
		return trainingStagnant;
	}

	public boolean isTestingStag() {
		return testingStagnant;
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
	
	public void setDelta(double delta) {
		this.delta = delta;
	}
}
