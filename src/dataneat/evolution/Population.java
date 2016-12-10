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
import java.util.Collections;
import java.util.List;

import dataneat.base.BaseNeat;
import dataneat.genome.NeatChromosome;
import dataneat.speciation.SpeciesDB;
import dataneat.utils.PropertiesHolder;
import dataneat.utils.RandGen;

public class Population extends BaseNeat {


	private static final String POP_SIZE = "popSize";
	private static final String CONNECTED = "connected";
	private static final String MAXIMIZE = "maximize";
	private static final String ELITE_MODE = "eliteMode";
	private static final String ELITE_PERCENT = "elitePercent";

	private boolean connected = true, maximize = false;
	private List<NeatChromosome> chromosomes = new ArrayList<NeatChromosome>();
	private List<NeatChromosome> elites = new ArrayList<NeatChromosome>();
	private double bestFitness = -1.0, bestTestFitness = 0.0, bestRaw = 0.0;
	private SpeciesDB speciesDB;
	private int popTargetSize = 0;
	private int numInputs = 1, numOutputs = 1;
	private Integer eliteMode = 0;
	private Double elitePercent = 0.0;

	public Population(PropertiesHolder p, int numInputs, int numOutputs) {
		super(p);
		this.connected = Boolean.parseBoolean(getParams().getProperty(CONNECTED));
		maximize = Boolean.parseBoolean(getParams().getProperty(MAXIMIZE));
		setPopTargetSize(Integer.parseInt(getParams().getProperty(POP_SIZE)));
		eliteMode = Integer.parseInt(getParams().getProperty(ELITE_MODE));
		elitePercent = Double.parseDouble(getParams().getProperty(ELITE_PERCENT));

		setSpeciesDB(new SpeciesDB(getHolder()));

		this.numInputs = numInputs;
		this.numOutputs = numOutputs;

		initPopulation(numInputs, numOutputs);
	}

	private void initPopulation(int numInputs, int numOutputs) {

		for (int i = 0; i < popTargetSize; i++) {
			NeatChromosome chrom = new NeatChromosome(numInputs, numOutputs, connected, getHolder());
			chromosomes.add(chrom);
		}
	}

	private void addRandom(int amount) {

		for (int i = 0; i < Math.abs(amount); i++) {

			if (chromosomes.size() < 1) {
				NeatChromosome additionalChrom = new NeatChromosome(numInputs, numOutputs, connected, getHolder());
				chromosomes.add(additionalChrom);
			} else {

				int duplicatedChrom = RandGen.rand.nextInt(chromosomes.size());
				NeatChromosome additionalChrom = new NeatChromosome(chromosomes.get(duplicatedChrom));
				chromosomes.add(additionalChrom);
			}
		}
	}

	private void killRandom(int amount) {

		for (int i = 0; i < Math.abs(amount); i++) {

			int deadChrom = RandGen.rand.nextInt(chromosomes.size());
			chromosomes.remove(deadChrom);
		}
	}

	public void correctSize() {
		int difference = chromosomes.size() - popTargetSize;

		if (difference == 0) {
			return;
		}

		if (difference < 0) {
			addRandom(difference);
		} else {
			killRandom(difference);
		}
	}

	public void saveElite() {
		elites.clear();

		switch (eliteMode) {
		case 0:
			int numElites = (int) (elitePercent * popTargetSize);
			chromosomes.sort(Collections.reverseOrder(
					(chrom1, chrom2) -> Double.compare(chrom1.getAdjustedFitness(), chrom2.getAdjustedFitness())));

			for (int i = 0; i < numElites; i++) {
				NeatChromosome chrom = new NeatChromosome(chromosomes.get(i));
				elites.add(chrom);
			}
			break;
		case 1:
		default:
			for (NeatChromosome chrom : speciesDB.getElites()) {
				elites.add(chrom);
			}
			break;
		}

	}

	public void addElites() {

		for (NeatChromosome chrom : elites) {
			chromosomes.add(chrom);
		}
	}

	public double getBestFitness() {
		return bestFitness;
	}

	public List<NeatChromosome> getChromosomes() {
		return chromosomes;
	}

	public void setChromosomes(List<NeatChromosome> pop) {
		this.chromosomes = pop;
	}

	public void addAll(List<NeatChromosome> chroms) {
		chromosomes.addAll(chroms);
	}

	public void clearChroms() {
		chromosomes.clear();
	}

	public int getPopActualSize() {
		return chromosomes.size();
	}

	public int getPopTargetSize() {
		return popTargetSize;
	}

	public void setPopTargetSize(int popSize) {
		this.popTargetSize = popSize;
	}

	public SpeciesDB getSpeciesDB() {
		return speciesDB;
	}

	public void setSpeciesDB(SpeciesDB speciesDB) {
		this.speciesDB = speciesDB;
	}

	public NeatChromosome getTrainingBest() {
		chromosomes.sort(Collections.reverseOrder(
				(chrom1, chrom2) -> Double.compare(chrom1.getAdjustedFitness(), chrom2.getAdjustedFitness())));
		bestFitness = chromosomes.get(0).getAdjustedFitness();
		setBestRaw(chromosomes.get(0).getFitness());
		return chromosomes.get(0);
	}

	public NeatChromosome getTestBest() {

		if (maximize) {
			chromosomes.sort(Collections.reverseOrder(
					(chrom1, chrom2) -> Double.compare(chrom1.getTestFitness(), chrom2.getTestFitness())));
		} else {
			chromosomes.sort((chrom1, chrom2) -> Double.compare(chrom1.getTestFitness(), chrom2.getTestFitness()));
		}

		bestTestFitness = chromosomes.get(0).getTestFitness();
		return chromosomes.get(0);
	}

	public double getBestTestFitness() {
		return bestTestFitness;
	}

	public void setBestTestFitness(double bestTestFitness) {
		this.bestTestFitness = bestTestFitness;
	}

	public List<NeatChromosome> getElites() {
		return elites;
	}

	public double getBestRaw() {
		return bestRaw;
	}

	public void setBestRaw(double bestRaw) {
		this.bestRaw = bestRaw;
	}
}
