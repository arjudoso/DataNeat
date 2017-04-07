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

import dataneat.base.BaseNeat;
import dataneat.evolution.Population;
import dataneat.genome.NeatChromosome;
import dataneat.speciation.SpeciesDB;
import dataneat.utils.PropertiesHolder;

public class SpeciationOperator extends BaseNeat {

	public SpeciationOperator(PropertiesHolder p) {
		super(p);
	}

	public void operate(Population pop, SpeciesDB speciesDB) {

		if (pop == null || speciesDB == null) {
			return;
		}

		speciesDB.prepareForSpeciation();
		
		//needs to be done sequentially
		pop.getChromosomes().stream().forEach(chrom -> speciate(speciesDB, chrom));

		speciesDB.postSpeciation();
	}

	private void speciate(SpeciesDB speciesDB, NeatChromosome chrom) {

		boolean added = false;

		// first, check the chromosome's previous species designation, chances
		// are good its still right.
		// compute distance between the two chromosomes and see if its whithin
		// epsilon of the threshold

		if (speciesDB.checkIfSpecies(chrom, chrom.getSpecies()))
		{
			speciesDB.addChromosometoSpecies(chrom, chrom.getSpecies());
			added = true;
		} else {			

				int species = speciesDB.whichSpecies(chrom);
				if (species >= 0) {
					chrom.setSpecies(species);
					speciesDB.addChromosometoSpecies(chrom, species);
					added = true;					
				}			
		}

		// create a new species if no matches found
		if (!added) {
			chrom.setSpecies(speciesDB.newSpecies(chrom));			
		}
	}
}
