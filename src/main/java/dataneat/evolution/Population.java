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
import dataneat.monitor.FitnessMonitor;
import dataneat.speciation.SpeciesDB;
import dataneat.utils.PropertiesHolder;
import dataneat.utils.RandGen;

public class Population extends BaseNeat {

	private static final String POP_SIZE = "popSize";
	private static final String CONNECTED = "connected";
	private static final String ELITE_MODE = "eliteMode";
	private static final String ELITE_PERCENT = "elitePercent";

	private boolean connected = true;
	private List<NeatChromosome> chromosomes = new ArrayList<NeatChromosome>();
	private List<NeatChromosome> elites = new ArrayList<NeatChromosome>();
	private FitnessMonitor fitnessMonitor;
	private SpeciesDB speciesDB;
	private int popTargetSize = 0;
	private int numInputs = 1, numOutputs = 1;
	private Integer eliteMode = 0;
	private double elitePercent = 0.0;
	public Population(PropertiesHolder p, int numInputs, int numOutputs) {
		super(p);
		this.connected = Boolean.parseBoolean(getParams().getProperty(CONNECTED));
		setPopTargetSize(Integer.parseInt(getParams().getProperty(POP_SIZE)));
		eliteMode = Integer.parseInt(getParams().getProperty(ELITE_MODE));
		elitePercent = Double.parseDouble(getParams().getProperty(ELITE_PERCENT));

		setSpeciesDB(new SpeciesDB(getHolder()));

		this.numInputs = numInputs;
		this.numOutputs = numOutputs;
		fitnessMonitor = new FitnessMonitor(p);
		initPopulation(numInputs, numOutputs);
	}

	private void initPopulation(int numInputs, int numOutputs) {

		for (int i = 0; i < popTargetSize; i++) {
			NeatChromosome chrom = new NeatChromosome(numInputs, numOutputs, connected, getHolder());
			add(chrom);
		}
		
		initMonitor();
	}
	
	private void initMonitor() {
		fitnessMonitor.updateTraining(chromosomes.get(0));
		fitnessMonitor.updateTest(chromosomes.get(0));
	}
	
	public void add(NeatChromosome chrom) {
		//wrapper for adding chroms to the arrayList.
		//all add operations must go through this method		
		fitnessMonitor.updateTraining(chrom);
		chromosomes.add(chrom);
	}
	
	public void updateTestBest() {		
		for (NeatChromosome chrom : chromosomes) {			
			fitnessMonitor.updateTest(chrom);			
		}
		System.out.println(fitnessMonitor.getBestTestFitness().getTestFitness());
	}

	private void addRandom(int amount) {

		for (int i = 0; i < Math.abs(amount); i++) {

			if (chromosomes.size() < 1) {
				NeatChromosome additionalChrom = new NeatChromosome(numInputs, numOutputs, connected, getHolder());
				add(additionalChrom);
			} else {

				int duplicatedChrom = RandGen.rand.nextInt(chromosomes.size());
				NeatChromosome additionalChrom = new NeatChromosome(chromosomes.get(duplicatedChrom));
				add(additionalChrom);
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
	
	public NeatChromosome getTrainingBest() {
		return fitnessMonitor.getBestTrainingFitness();
	}
	
	public NeatChromosome getTestBest() {
		return fitnessMonitor.getBestTestFitness();
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
			add(chrom);
		}
	}

	public List<NeatChromosome> getChromosomes() {
		return chromosomes;
	}

	public void setChromosomes(List<NeatChromosome> pop) {
		this.chromosomes = pop;
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

	public List<NeatChromosome> getElites() {
		return elites;
	}
	
	public FitnessMonitor getMonitor() {
		return fitnessMonitor;
	}
}
