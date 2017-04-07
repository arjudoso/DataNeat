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
package dataneat.speciation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dataneat.base.BaseNeat;
import dataneat.genome.NeatChromosome;
import dataneat.monitor.FitnessMonitor;
import dataneat.utils.PropertiesHolder;
import dataneat.utils.RandGen;

public class Species extends BaseNeat {
	private static final String SPECIES_DROPOFF = "speciesDropAge";
	private static final String SELECTION = "selectionType";
	private static final String TOURN_SIZE = "tournamentSize";
	private static final String SURVIVAL_THRESH = "survivalThresh";

	private int speciesId = -1, dropOff = 0;
	private List<NeatChromosome> members = new ArrayList<NeatChromosome>();
	private List<NeatChromosome> eligiblePop;
	private NeatChromosome rep = null;
	private double matingProp = 0.0, averageAdjusted = 0.0;
	private double survivalThresh = 0.2;
	private boolean stagnant = false;
	private FitnessMonitor fitnessMonitor;
	private Integer selectionType = 0;
	private int tournamentSize = 0;

	public Species(PropertiesHolder p) {
		super(p);
		dropOff = Integer.parseInt(getParams().getProperty(SPECIES_DROPOFF));
		selectionType = Integer.parseInt(getParams().getProperty(SELECTION));
		tournamentSize = Integer.parseInt(getParams().getProperty(TOURN_SIZE));
		survivalThresh = Double.parseDouble(getParams().getProperty(SURVIVAL_THRESH));
		fitnessMonitor = new FitnessMonitor(p);
		fitnessMonitor.setRoundThreshold(dropOff);
	}

	public void updateBestFitness() {
		if (members.size() < 1) {
			return;
		}

		sortByAdjusted();
		NeatChromosome currentBest = members.get(0);
		fitnessMonitor.updateTraining(currentBest);

		if (fitnessMonitor.isTrainingStag()) {
			stagnant = true;
		}
	}

	public void addMember(NeatChromosome chrom) {
		members.add(chrom);
	}

	public void clearMembers() {
		members.clear();
	}

	public int search(NeatChromosome chrom) {
		for (int i = 0; i < members.size(); i++) {
			if (members.get(i).getId() == chrom.getId()) {
				return i;
			}
		}

		return -1;
	}

	public List<NeatChromosome> getElites(int numElites) {

		List<NeatChromosome> elites = new ArrayList<NeatChromosome>();

		members.sort(Collections.reverseOrder(
				(chrom1, chrom2) -> Double.compare(chrom1.getAdjustedFitness(), chrom2.getAdjustedFitness())));

		for (int i = 0; i < numElites; i++) {

			if (i >= members.size()) {
				break;
			}
			NeatChromosome elite = new NeatChromosome(members.get(i));
			elites.add(elite);
		}

		return elites;
	}

	public List<NeatChromosome> getElites(double percent) {
		int numElites = (int) (percent * members.size());
		return getElites(numElites);
	}

	private double getSumAdjustedFitness() {
		double total = 0.0;
		for (NeatChromosome chrom : members) {
			total += chrom.getAdjustedFitness();
		}
		return total;
	}

	public double getAverageAdjustedFitness() {
		// recalculate, it might have changed since last asked for
		averageAdjusted = (getSumAdjustedFitness() / members.size());
		return averageAdjusted;
	}

	public void assignProp(double totalAverageSpeciesFitness) {
		// assumes averageAdjusted is updated, does not reupdate to save time.
		matingProp = (averageAdjusted / totalAverageSpeciesFitness);
	}

	public void assignRep() {
		if (members.size() < 1) {
			return;
		}

		members.sort(Collections.reverseOrder(
				(chrom1, chrom2) -> Double.compare(chrom1.getAdjustedFitness(), chrom2.getAdjustedFitness())));
		setRep(members.get(0));
	}

	public void sortByAdjusted() {
		members.sort(Collections.reverseOrder(
				(chrom1, chrom2) -> Double.compare(chrom1.getAdjustedFitness(), chrom2.getAdjustedFitness())));
	}

	private NeatChromosome holdTournament(int size) {
		NeatChromosome currentBest = getRandom();
		NeatChromosome challenger;

		for (int i = 0; i < (size - 1); i++) {

			challenger = getRandom();

			if (challenger.getAdjustedFitness() > currentBest.getAdjustedFitness()) {
				currentBest = challenger;
			}
		}

		return currentBest;
	}

	public void selectionPrep() {
		switch (selectionType) {
		case 0:
			// truncation
			truncate();
			break;
		case 1:
			// tournament
			break;
		}
	}

	private void truncate() {
		sortByAdjusted();

		// get top percentage of previous gen for mating
		int eligibleSize = (int) (size() * survivalThresh);

		// can't do crossover with less than 2 eligible mates
		if (eligibleSize < 2) {
			eligibleSize = 2;
		}

		// subset list of eligible mates
		eligiblePop = members.subList(0, eligibleSize);
	}
	
	public NeatChromosome generateSelection() {
		switch (selectionType) {
		case 0:
			//truncation
			return getTruncatedRandom();
		case 1:
			//tournament
			return holdTournament(tournamentSize);
		default:
			return getRandom();
		}		
	}

	public int getNumOffspring(int popSize) {
		return (int) (popSize * matingProp);
	}

	public List<NeatChromosome> getMembers() {
		return members;
	}

	public void setMembers(List<NeatChromosome> members) {
		this.members = members;
	}

	public int getSpeciesId() {
		return speciesId;
	}

	public void setSpeciesId(int species) {
		this.speciesId = species;
	}

	public int size() {
		return members.size();
	}

	public NeatChromosome getRep() {
		return rep;
	}

	public void setRep(NeatChromosome rep) {
		this.rep = rep;
	}

	public double getMatingProp() {
		return matingProp;
	}

	public void setMatingProp(double matingProp) {
		this.matingProp = matingProp;
	}

	public boolean isStagnant() {
		return stagnant;
	}

	public void setStagnant(boolean stagnant) {
		this.stagnant = stagnant;
	}

	private NeatChromosome getRandom() {
		int index = RandGen.rand.nextInt(members.size());
		return members.get(index);
	}
	
	private NeatChromosome getTruncatedRandom() {
		int index = RandGen.rand.nextInt(eligiblePop.size());
		return eligiblePop.get(index);
	}
}
