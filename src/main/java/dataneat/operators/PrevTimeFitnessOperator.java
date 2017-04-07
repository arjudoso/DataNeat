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
package main.java.dataneat.operators;

import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;

import main.java.dataneat.base.BaseNeat;
import main.java.dataneat.fitness.TargetFitnessFunction;
import main.java.dataneat.genome.NeatChromosome;
import main.java.dataneat.phenotype.Network;
import main.java.dataneat.utils.PropertiesHolder;

public class PrevTimeFitnessOperator extends BaseNeat implements TargetFitnessOperator{

	private static final String FITNESS_FUNCTION = "fitnessFunction";
	private static final String MAXIMIZE = "maximize";		
	private boolean maximize = true;		
	private TargetFitnessFunction fitnessFunction;
	private INDArray stabil;

	public PrevTimeFitnessOperator(PropertiesHolder p, INDArray stabilMatrix) {
		super(p);
		stabil = stabilMatrix;
		maximize = Boolean.parseBoolean(getParams().getProperty(MAXIMIZE));		

		try {
			fitnessFunction = (TargetFitnessFunction) Class.forName(getParams().getProperty(FITNESS_FUNCTION))
					.newInstance();

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	
	public void operate(List<NeatChromosome> population, DataSet data) {

		if (population == null) {
			// Population list empty:
			// nothing to do.
			// -----------------------------------------------
			return;
		}

		// do the fitness evaluations on each chromosome
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

	private void evaluate(NeatChromosome chrom, DataSet data) {
		// this function evaluates a single chromosome

		// create phenotype
		Network net = new Network(chrom, getHolder(),stabil);		

		// compute on entire training set
		net.computeNetPrevTimestep(data.getFeatures());
				
		// set fitness
		double fitness = fitnessFunction.computeFitness(data.getLabels(), net.getOutput());
		chrom.setFitness(fitness);
	}	
}
