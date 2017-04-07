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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import dataneat.base.BaseNeat;
import dataneat.genome.LinkDB;
import dataneat.genome.LinkGene;
import dataneat.genome.NeatChromosome;
import dataneat.speciation.Species;
import dataneat.utils.PropertiesHolder;
import dataneat.utils.RandGen;

public class NeatCrossoverOperator extends BaseNeat {

	private static final String ENABLE_CHANCE = "enableChance";

	private double enableChance = 0.25;

	public NeatCrossoverOperator(PropertiesHolder p) {

		super(p);

		enableChance = Double.parseDouble(getParams().getProperty(ENABLE_CHANCE));
	}

	public List<NeatChromosome> operate(List<NeatChromosome> population, int numOffspring) {

		int size = population.size();

		List<NeatChromosome> offspring = new ArrayList<NeatChromosome>();

		// create a place to store the pairs of int values which represent the
		// two chromosomes for each crossover
		List<Point> crossOverSelections = new ArrayList<Point>();

		// get pairs for crossOver
		for (int i = 0; i < numOffspring; i++) {
			int index1 = 0, index2 = 0;

			index1 = RandGen.rand.nextInt(size);

			do {
				index2 = RandGen.rand.nextInt(size);
			} while (index1 == index2);

			Point crossOverPair = new Point();
			crossOverPair.setLocation(index1, index2);
			crossOverSelections.add(crossOverPair);
		}

		// execute crossOver

		for (Point p : crossOverSelections) {

			offspring.add(doCrossover(p, population));
		}

		return offspring;
	}
	
	public List<NeatChromosome> operate(Species species, int numOffspring) {

		List<NeatChromosome> offspring = new ArrayList<NeatChromosome>();		

		for (int i = 0; i < numOffspring; i++) {

			NeatChromosome firstMate = species.generateSelection();
			NeatChromosome secondMate = species.generateSelection();
			offspring.add(doCrossover(firstMate, secondMate));
		}

		return offspring;
	}

	protected NeatChromosome doCrossover(NeatChromosome firstMate, NeatChromosome secondMate) {
		int FIRSTMATE = 0;
		int SECONDMATE = 1;

		int best = FIRSTMATE;

		/*
		 * Calculate which chromosome we will be using disjoint/excess genes
		 * from. We want to use the fittest chromosome. If the fitness is equal,
		 * we want the smallest chromosome. If both size and fitness are equal,
		 * just use a random chromosome.
		 */

		if (firstMate.getAdjustedFitness() == secondMate.getAdjustedFitness()) {

			if (firstMate.getGenomeSize() == secondMate.getGenomeSize()) {

				best = RandGen.rand.nextInt(2); // generates random int between
												// 0,1
			}

			else {

				if (firstMate.getGenomeSize() < secondMate.getGenomeSize()) {

					best = FIRSTMATE;
				}

				else {

					best = SECONDMATE;
				}
			}
		}

		else {

			if (firstMate.getAdjustedFitness() > secondMate.getAdjustedFitness()) {

				best = FIRSTMATE;
			}

			else {

				best = SECONDMATE;
			}
		}

		// drop the best and worst mates in some holders
		NeatChromosome bestMate = firstMate;
		NeatChromosome worstMate = secondMate;

		if (best == SECONDMATE) {

			bestMate = (NeatChromosome) secondMate;
			worstMate = (NeatChromosome) firstMate;
		}

		// start by making the offspring a copy of the best parent, and then
		// modify it during crossover
		NeatChromosome baby = new NeatChromosome(bestMate);

		// Get the link genes, which will be used to execute crossover
		// ------------------

		LinkDB bestLinks = bestMate.getLinks();
		LinkDB worstLinks = worstMate.getLinks();
		LinkDB babyLinks = baby.getLinks();

		// the crossover procedure requires the lists sorted in order of
		// increasing innovation ID

		bestLinks.sortById();

		worstLinks.sortById();

		// create 2 indices to iterate over the parents
		// -------------------

		int bestLinksIndex = 0;
		int worstLinksIndex = 0;

		while (bestLinksIndex < bestLinks.size()) {

			if (worstLinksIndex == worstLinks.size()) {
				// we ran out of genes to examine in the worst parent
				boolean isDisabled = checkGeneDisabled(bestLinks.getByIndex(bestLinksIndex));

				if (isDisabled) {

					if (RandGen.rand.nextDouble() < enableChance) {

						babyLinks.getByIndex(bestLinksIndex).setEnabled(true);
					}
				}

				bestLinksIndex++;
			} else {

				// innovation Ids equal
				// --------------------------
				if (bestLinks.getByIndex(bestLinksIndex).getInnovationID()
						.equals(worstLinks.getByIndex(worstLinksIndex).getInnovationID())) {

					// check if gene is disabled in either parent and save as a
					// flag
					// before making modifications to genome.
					boolean isDisabled = checkGeneDisabled(bestLinks.getByIndex(bestLinksIndex),
							worstLinks.getByIndex(worstLinksIndex));

					// need to know if the link has previously been split, we
					// don't
					// want to give the baby an unsplit copy because then the
					// link
					// could be split again by the mutation operator, resulting
					// in
					// multiple nodes splitting the same link
					boolean isSplit = bestLinks.getByIndex(bestLinksIndex).isAlreadySplit();

					if (RandGen.rand.nextDouble() > 0.5) {

						// if this executes, then replace the baby's link with
						// the
						// one from the other parent, otherwise just leave it be
						LinkGene link = new LinkGene(worstLinks.getByIndex(worstLinksIndex));
						link.setAlreadySplit(isSplit);
						babyLinks.set(bestLinksIndex, link);
					}

					// if the flag was set, then 75% chance gene is disabled in
					// offspring.

					if (isDisabled) {

						if (RandGen.rand.nextDouble() < enableChance) {

							babyLinks.getByIndex(bestLinksIndex).setEnabled(true);
						} else {
							babyLinks.getByIndex(bestLinksIndex).setEnabled(false);
						}
					}

					// increment both indices, so that we move on to comparing
					// the
					// next link gene in each parent
					bestLinksIndex++;
					worstLinksIndex++;

				} else if (bestLinks.getByIndex(bestLinksIndex).getInnovationID()
						.compareTo(worstLinks.getByIndex(worstLinksIndex).getInnovationID()) > 0) {

					// innovation IDs are being walked in ascending order. If
					// bestlinks > worstlinks, it means there are linkgenes in
					// the
					// worst parent that aren't present in the best parent.
					// These
					// genes are ignored in NeatCrossover, so just increment the
					// worst parent index and move on.
					worstLinksIndex++;

					// this case doesn't effect the offspring.

				} else {

					// if worstLinkIndex > bestLinkIndex, it means there are
					// genes
					// in best parent that are not present in the worst parent.
					// These do end up in the offspring, so we can't just ignore
					// them.

					// need to check for disabled genes here, since this gene
					// ends
					// up in the offspring.

					// worstGene is not a factor in this case, this gene ID does
					// not
					// exist in the worstGene genome.

					// no need to modify the baby, this gene is just staying as
					// is,
					// since the baby was originally a copy of the best parent.

					boolean isDisabled = checkGeneDisabled(bestLinks.getByIndex(bestLinksIndex));

					if (isDisabled) {

						if (RandGen.rand.nextDouble() < enableChance) {

							babyLinks.getByIndex(bestLinksIndex).setEnabled(true);
						}
					}

					bestLinksIndex++;
				}
			}
		}

		//baby.setLinks(babyLinks);

		return baby;
	}

	protected NeatChromosome doCrossover(Point pair, List<NeatChromosome> eligiblePop) {

		// use the points to get the 2 chroms
		NeatChromosome firstMate = eligiblePop.get(pair.x);
		NeatChromosome secondMate = eligiblePop.get(pair.y);

		return doCrossover(firstMate, secondMate);
	}

	private boolean checkGeneDisabled(LinkGene bestGene) {
		return !(bestGene.isEnabled());
	}

	private boolean checkGeneDisabled(LinkGene bestGene, LinkGene worstGene) {

		// check if the gene is disabled in either parent.
		// if either allele is false, then gene is disabled in at least one
		// parent.

		return (!(bestGene.isEnabled()) || !(worstGene.isEnabled()));
	}

	public double getEnableChance() {
		return enableChance;
	}

	public void setEnableChance(double enableChance) {
		this.enableChance = enableChance;
	}

}
