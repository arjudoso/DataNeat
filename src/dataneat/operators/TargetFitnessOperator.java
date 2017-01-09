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
package dataneat.operators;

import java.io.IOException;
import java.util.List;

import dataneat.base.BaseNeat;
import dataneat.data.TableData;
import dataneat.data.TargetDataset;
import dataneat.fitness.TargetFitnessFunction;
import dataneat.genome.NeatChromosome;
import dataneat.phenotype.Network;
import dataneat.utils.PropertiesHolder;

public class TargetFitnessOperator extends BaseNeat {

	private static final String FITNESS_FUNCTION = "fitnessFunction";
	private static final String MAXIMIZE = "maximize";
	private static final String MODE = "mode";

	private boolean maximize = true;
	Integer mode = 0;
	int stochasticIndex = -1;
	private TargetFitnessFunction fitnessFunction;

	public TargetFitnessOperator(PropertiesHolder p) {
		super(p);
		maximize = Boolean.parseBoolean(getParams().getProperty(MAXIMIZE));
		mode = Integer.parseInt(getParams().getProperty(MODE));
		
		try {
			fitnessFunction = (TargetFitnessFunction) Class.forName(getParams().getProperty(FITNESS_FUNCTION))
					.newInstance();

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void operate(List<NeatChromosome> population, TargetDataset data) {

		if (population == null) {
			// Population list empty:
			// nothing to do.
			// -----------------------------------------------
			return;
		}
		
		//manages the stochastic index for stochastic type fitness
		preEval(data);

		//do the fitness evaluations on each chromosome
		population.parallelStream().forEach(chrom -> evaluate(chrom, data));		

		// we need the worst fitness in the population, this depends on if
		// fitness is maximized or minimized
		double worstFitness;
		if (maximize) {
			worstFitness = Double.MAX_VALUE;
			for (NeatChromosome chrom : population) {
				if (chrom.getFitness() < worstFitness) {
					worstFitness = chrom.getFitness();
				}
			}
		} else {
			// RMSE is minimized, so the worst fitness is the highest value
			worstFitness = Double.MIN_VALUE;
			for (NeatChromosome chrom : population) {
				if (chrom.getFitness() > worstFitness) {
					worstFitness = chrom.getFitness();
				}
			}
		}

		// now replace raw fitness with distance of each fitness from the
		// worstfitness of the population. this means that chroms with low RMSE
		// will have the largest distance from the worstFitness, and thus higher
		// adjusted fitness. We need this because NEAT expects fitness to be
		// maximized

		for (NeatChromosome chrom : population) {
			chrom.setAdjustedFitness(Math.abs(worstFitness - chrom.getFitness()));
		}
	}

	private void evaluate(NeatChromosome chrom, TargetDataset data) {
		// this function evaluates a single chromosome

		// create phenotype
		Network net = new Network(chrom, getHolder());

		// create a data object for the network's outputs
		TableData outputs = new TableData(data.numIdealColumns());

		switch (mode) {
		case 0:
			//compute on entire training set
			normalCompute(data, outputs, net);
			break;
		case 1:
			stochasticCompute(data, outputs, net);
			break;
		}
		
		// set fitness
		double fitness = fitnessFunction.computeFitness(data, outputs);
		chrom.setFitness(fitness);
	}

	private void normalCompute(TargetDataset data, TableData outputs, Network net) {

		// compute network output for each input row
		for (int i = 0; i < data.numRowsReduced(); i++) {
			net.computeNet(data.getInputRow(i));
			try {
				outputs.addRow(net.getOutput());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void stochasticCompute(TargetDataset data, TableData outputs, Network net) {
		//computes on a single training example
		
		net.computeNet(data.getInputRow(stochasticIndex));		
		try {
			outputs.addRow(net.getOutput());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void preEval(TargetDataset data) {
		
		switch (mode) {
		case 0:
			break;
		case 1: 
			updateStochasticIndex(data);
			break;
		}
	}
	
	private void updateStochasticIndex(TargetDataset data) {
		if (stochasticIndex >= data.numRowsOriginal()) {
			stochasticIndex = 0;
		} else {
			stochasticIndex++;
		}
	}
}
