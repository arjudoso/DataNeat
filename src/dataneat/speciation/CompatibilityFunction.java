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

import dataneat.base.BaseNeat;
import dataneat.genome.LinkDB;
import dataneat.genome.NeatChromosome;
import dataneat.utils.PropertiesHolder;

public class CompatibilityFunction extends BaseNeat {

	private static final String COEFF1 = "coeff1";
	private static final String COEFF2 = "coeff2";
	private static final String COEFF3 = "coeff3";

	private static final double ERROR_VALUE = 100000.0; // a very high species
														// delta for use as an
														// error value

	private double coeff1 = 1.0, coeff2 = 1.0, coeff3 = 1.0;

	public CompatibilityFunction(PropertiesHolder p) {
		super(p);

		coeff1 = Double.parseDouble(getParams().getProperty(COEFF1));
		coeff2 = Double.parseDouble(getParams().getProperty(COEFF2));
		coeff3 = Double.parseDouble(getParams().getProperty(COEFF3));
	}

	public double compute(NeatChromosome firstGenome, NeatChromosome secondGenome) {
		// returns a double that indicates how "far" apart the two genomes are
		// genetically speaking, returns error value if something goes wrong

		if (firstGenome == null || secondGenome == null) {
			return ERROR_VALUE;
		}

		// get the links from the 2 genomes, for use in the subroutines
		LinkDB firstLinks = ((NeatChromosome) firstGenome).getLinks();
		LinkDB secondLinks = ((NeatChromosome) secondGenome).getLinks();

		firstLinks.sortById();
		secondLinks.sortById();

		// calculate the average weight differences of matching
		// genes between the 2 genomes, and num Excess & Disjoint

		int firstLinksIndex = 0, secondLinksIndex = 0, numMatching = 0, numDisjoint = 0;

		double cumlativeWeightDelta = 0.0, avgWeightDiff = 0.0;

		// to calc these terms, we need to walk the links
		while (firstLinksIndex < firstLinks.size() && secondLinksIndex < secondLinks.size()) {

			// innovation Ids equal
			// --------------------------
			if (firstLinks.getByIndex(firstLinksIndex).getInnovationID() == secondLinks.getByIndex(secondLinksIndex)
					.getInnovationID()) {

				numMatching++;

				double weight1 = firstLinks.getByIndex(firstLinksIndex).getWeight();

				double weight2 = secondLinks.getByIndex(secondLinksIndex).getWeight();

				cumlativeWeightDelta += Math.abs(weight1 - weight2);

				firstLinksIndex++;
				secondLinksIndex++;

			} else if (firstLinks.getByIndex(firstLinksIndex).getInnovationID() > secondLinks.getByIndex(secondLinksIndex)
					.getInnovationID()) {

				numDisjoint++;
				secondLinksIndex++;
			} else {

				numDisjoint++;
				firstLinksIndex++;
			}
		}

		avgWeightDiff = (cumlativeWeightDelta / numMatching);

		int numExcess = 0;
		numExcess = (Math.abs(firstLinks.size() - secondLinks.size()));

		return (((coeff1 * numExcess) + (coeff2 * numDisjoint))) + (coeff3 * avgWeightDiff);
	}

}
