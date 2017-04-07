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
package main.java.dataneat.evolution;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;

import main.java.dataneat.base.BaseNeat;
import main.java.dataneat.genome.NeatChromosome;
import main.java.dataneat.operators.AddLinkOperator;
import main.java.dataneat.operators.AddNodeOperator;
import main.java.dataneat.operators.AsexualOperator;
import main.java.dataneat.operators.CloneOperator;
import main.java.dataneat.operators.CurrTimeFitnessOperator;
import main.java.dataneat.operators.LinkWeightOperator;
import main.java.dataneat.operators.MutationOperator;
import main.java.dataneat.operators.NeatCrossoverOperator;
import main.java.dataneat.operators.PrevTimeFitnessOperator;
import main.java.dataneat.operators.RemoveLinkOperator;
import main.java.dataneat.operators.RemoveNodeOperator;
import main.java.dataneat.operators.SpeciationOperator;
import main.java.dataneat.operators.TargetFitnessOperator;
import main.java.dataneat.speciation.Species;
import main.java.dataneat.utils.PropertiesHolder;

public class SupervisedEvolver extends BaseNeat implements TargetEvolver {

	private static final String CALC_MODE = "calcMode";
	// supervised learning, uses a target fitness operator/function

	private List<MutationOperator> evolvingOperators = new ArrayList<MutationOperator>();
	private List<MutationOperator> pruningOperators = new ArrayList<MutationOperator>();
	private SpeciationOperator speciator;
	private TargetFitnessOperator fitnessOperator;
	private NeatCrossoverOperator crossOver;
	private CloneOperator cloner;
	private AsexualOperator asexual;
	private INDArray stabilMatrix;
	private Integer calculationMode = 0;

	public SupervisedEvolver(PropertiesHolder p, INDArray stabilMatrix) {
		super(p);
		calculationMode = Integer.parseInt(getParams().getProperty(CALC_MODE));
		this.stabilMatrix = stabilMatrix;
		initMutationOperators();
	}

	private void initMutationOperators() {

		evolvingOperators.add(new AddNodeOperator(getHolder()));
		evolvingOperators.add(new AddLinkOperator(getHolder()));
		evolvingOperators.add(new LinkWeightOperator(getHolder()));
		pruningOperators.add(new RemoveLinkOperator(getHolder()));
		pruningOperators.add(new RemoveNodeOperator(getHolder()));
		speciator = new SpeciationOperator(getHolder());

		switch (calculationMode) {
		case 0:
			fitnessOperator = new CurrTimeFitnessOperator(getHolder(), stabilMatrix);
			break;
		case 1:
			fitnessOperator = new PrevTimeFitnessOperator(getHolder(), stabilMatrix);
			break;
		default:
			fitnessOperator = new CurrTimeFitnessOperator(getHolder(), stabilMatrix);
			break;
		}

		crossOver = new NeatCrossoverOperator(getHolder());
		cloner = new CloneOperator();
		asexual = new AsexualOperator();
	}

	@Override
	// shape of data = (batchSize, numInputs)
	public void preEvolution(Population pop, DataSet data) {
		// speciate, clears old species list and creates new ones based on
		// current population
		speciator.operate(pop, pop.getSpeciesDB());

		fitnessOperator.operate(pop.getChromosomes(), data);

		// save elites
		pop.saveElite();
	}

	@Override
	public void evolve(Population pop) {
		// calc offspring numbers, also finds the best chromosome in the
		// population
		pop.getSpeciesDB().assignMatingProportions();

		// crossover - pass old population via species lists, crossover operator
		// generates the next generation, adds it to the population

		// clear old generation, don't worry, the entire population is still
		// alive in the species lists
		pop.clearChroms();

		List<NeatChromosome> offspringList;
		int numOffspring = 0;

		for (Species s : pop.getSpeciesDB().getSpeciesList()) {

			// number of offspring to produce
			numOffspring = s.getNumOffspring(pop.getPopTargetSize());

			if (numOffspring == 0) {
				// no offspring requested, species dies out
				continue;
			}

			// species lists won't exist without at least 1 member, and if there
			// are 2 members or more, we are good for crossover, so the only
			// edge case is 1
			if (s.size() == 1) {
				// only one member of this species, we can't do crossover
				// without at least 2 parents, so initiate cloning instead of
				// crossover
				offspringList = cloner.operate(s.getMembers().get(0), numOffspring);

			} else {

				s.selectionPrep();
				offspringList = crossOver.operate(s, numOffspring);
			}

			// add offspring to the new generation
			for (NeatChromosome chrom : offspringList) {
				pop.add(chrom);
			}
		}

		pop.correctSize();

		// operate on new population

		for (MutationOperator op : evolvingOperators) {

			op.operate(pop.getChromosomes());
		}

		// add elites to next gen
		pop.addElites();
	}

	@Override
	public void prune(Population pop) {
		// calc offspring numbers, also finds the best chromosome in the
		// population
		pop.getSpeciesDB().assignMatingProportions();
		
		// clear old generation, don't worry, the entire population is still
		// alive in the species lists
		pop.clearChroms();

		List<NeatChromosome> offspringList;
		int numOffspring = 0;

		for (Species s : pop.getSpeciesDB().getSpeciesList()) {

			// number of offspring to produce
			numOffspring = s.getNumOffspring(pop.getPopTargetSize());

			if (numOffspring == 0) {
				// no offspring requested, species dies out
				continue;
			}

			// species lists won't exist without at least 1 member
			if (s.size() == 1) {
				// only one member of this species, just clone				
				offspringList = cloner.operate(s.getMembers().get(0), numOffspring);
			} else {				
				offspringList = asexual.operate(s.getMembers(), numOffspring);
			}

			// add offspring to the new generation
			for (NeatChromosome chrom : offspringList) {
				pop.add(chrom);
			}
		}

		pop.correctSize();

		// operate on new population

		for (MutationOperator op : pruningOperators) {
			op.operate(pop.getChromosomes());
		}

		// add elites to next gen
		pop.addElites();
	}

	public void setStabilMatrix(INDArray stabilMatrix) {
		this.stabilMatrix = stabilMatrix;
	}

}
