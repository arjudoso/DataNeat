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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataneat.base.BaseNeat;
import dataneat.genome.NeatChromosome;
import dataneat.utils.PropertiesHolder;

public class SpeciesDB extends BaseNeat {

	private static final String SPECIES_LIMIT = "speciesLimit";
	private static final String SPECIES_THRESHOLD = "speciesThreshold";
	private static final String SPECIES_ADJUSTMENT = "speciesAdjustment";	

	private static int speciesIds = 0;
	private double threshold = 0.0;
	private double adjustment = 0.0;

	private int speciesTarget = -1;

	private CompatibilityFunction compFunction = new CompatibilityFunction(getHolder());

	private List<Species> speciesList = new ArrayList<Species>();

	public SpeciesDB(PropertiesHolder p) {
		super(p);
		setSpeciesTarget(Integer.parseInt(getParams().getProperty(SPECIES_LIMIT)));
		setThreshold(Double.parseDouble(getParams().getProperty(SPECIES_THRESHOLD)));
		adjustment = Double.parseDouble(getParams().getProperty(SPECIES_ADJUSTMENT));		
	}

	public List<NeatChromosome> getElites() {

		List<NeatChromosome> elites = new ArrayList<NeatChromosome>();

		for (Species s : speciesList) {

			// if the species has less than 5 members, do not save an elite from
			// it

			if (s.size() < 5) {
				continue;
			}

			elites.addAll(s.getElites(1));
		}

		return elites;
	}

	public void addChromosometoSpecies(NeatChromosome chrom, int speciesId) {

		boolean added = false;

		for (Species s : speciesList) {

			if (s.getSpeciesId() == speciesId) {
				s.addMember(chrom);
				added = true;
				break;
			}
		}

		if (!added) {
			// shouldn't be possible to get here, we have a chromosome that is
			// part of a species that has been wiped from the database
			// Bring the species back from the dead
			Species s = new Species(getHolder());
			s.setSpeciesId(speciesId);
			s.addMember(chrom);
			s.setRep(chrom);
			speciesList.add(s);
		}
	}

	public int newSpecies(NeatChromosome chrom) {

		Species s = new Species(getHolder());
		s.setSpeciesId(speciesIds);
		s.addMember(chrom);
		s.setRep(chrom);
		speciesList.add(s);

		speciesIds++;
		return (speciesIds - 1);
	}

	public void killSpecies(int speciesId) {
		for (Species s : speciesList) {
			if (s.getSpeciesId() == speciesId) {
				speciesList.remove(s);
				break;
			}
		}
	}

	public void prepareForSpeciation() {

		// assign representatives for each species
		for (Species s : speciesList) {
			s.assignRep();
		}

		// clear the old species list
		for (Species s : speciesList) {
			s.clearMembers();
		}
	}

	public void postSpeciation() {
		// adjust threshold after speciation, if required
		if (speciesTarget > 0) {
			adjustThreshold();
		}

		// remove dead species
		for (int i = 0; i < speciesList.size(); i++) {

			speciesList.get(i).updateBestFitness();

			if ((speciesList.get(i).size() < 1) || (speciesList.get(i).isStagnant())) {
				speciesList.remove(i);
			}
		}
	}

	public void assignMatingProportions() {
		// returns the fraction of offspring for each species

		// double totalFit = 0; // hold the total fitness of all species
		double totalAverageSpeciesFitness = 0.0;

		// hold the proportion of offspring each species should generate

		for (Species s : speciesList) {

			totalAverageSpeciesFitness += s.getAverageAdjustedFitness();
		}

		// set map values to be a proportion of the total adjusted
		// fitness
		for (Species s : speciesList) {

			s.assignProp(totalAverageSpeciesFitness);
		}
	}

	public Map<Integer, Double> getMatingProportions() {

		HashMap<Integer, Double> prop = new HashMap<Integer, Double>();

		for (Species s : speciesList) {
			prop.put(s.getSpeciesId(), s.getMatingProp());
		}

		return prop;
	}

	private void adjustThreshold() {
		int speciesCount = speciesList.size();

		if (speciesCount < speciesTarget) {
			threshold -= adjustment;
		} else {
			if (speciesCount > speciesTarget) {
				threshold += adjustment;
			}
		}

		if (threshold < adjustment) {
			threshold = adjustment;
		}
	}

	public boolean checkIfSpecies(NeatChromosome chrom, int speciesId) {
		NeatChromosome rep = null;

		for (Species s : speciesList) {
			if (s.getSpeciesId() == speciesId) {
				rep = s.getRep();
				break;
			}
		}

		// can handle null values
		return (getCompFunction().compute(chrom, rep) < getThreshold());

	}

	public int whichSpecies(NeatChromosome chrom) {

		for (Species s : speciesList) {
			if (getCompFunction().compute(chrom, s.getRep()) < getThreshold()) {
				return s.getSpeciesId();
			}
		}

		return -1;
	}

	public Species getSpecies(int species) {
		return speciesList.get(species);
	}

	/**
	 * @return the speciesTarget
	 */
	public int getSpeciesTarget() {
		return speciesTarget;
	}

	/**
	 * @param speciesTarget
	 *            the speciesTarget to set
	 */
	public void setSpeciesTarget(int speciesTarget) {
		this.speciesTarget = speciesTarget;
	}

	public CompatibilityFunction getCompFunction() {
		return compFunction;
	}

	public void setCompFunction(CompatibilityFunction compFunction) {
		this.compFunction = compFunction;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public List<Species> getSpeciesList() {
		return speciesList;
	}

}
