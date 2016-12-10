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
package dataneat.evolution;

import java.util.ArrayList;
import java.util.List;

import dataneat.base.BaseNeat;
import dataneat.data.TargetDataset;
import dataneat.genome.NeatChromosome;
import dataneat.operators.AddLinkOperator;
import dataneat.operators.AddNodeOperator;
import dataneat.operators.CloneOperator;
import dataneat.operators.LinkWeightOperator;
import dataneat.operators.MutationOperator;
import dataneat.operators.NeatCrossoverOperator;
import dataneat.operators.SpeciationOperator;
import dataneat.operators.TargetFitnessOperator;
import dataneat.speciation.Species;
import dataneat.utils.PropertiesHolder;

public class SupervisedEvolver extends BaseNeat implements TargetEvolver {

	// supervised learning, uses a target fitness operator/function

	private List<MutationOperator> mutationOperators = new ArrayList<MutationOperator>();
	private SpeciationOperator speciator;
	private TargetFitnessOperator fitnessOperator;
	private NeatCrossoverOperator crossOver;
	private CloneOperator cloner;

	public SupervisedEvolver(PropertiesHolder p) {
		super(p);

		initMutationOperators();
	}

	private void initMutationOperators() {

		mutationOperators.add(new AddNodeOperator(getHolder()));
		mutationOperators.add(new AddLinkOperator(getHolder()));
		mutationOperators.add(new LinkWeightOperator(getHolder()));
		speciator = new SpeciationOperator(getHolder());
		fitnessOperator = new TargetFitnessOperator(getHolder());
		crossOver = new NeatCrossoverOperator(getHolder());
		cloner = new CloneOperator();
	}

	@Override
	public void preEvolution(Population pop, TargetDataset data) {
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
			pop.addAll(offspringList);
		}

		pop.correctSize();

		// operate on new population

		for (MutationOperator op : mutationOperators) {

			op.operate(pop.getChromosomes());
		}

		// add elites to next gen
		pop.addElites();

	}

}
