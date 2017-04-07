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

import main.java.dataneat.base.BaseNeat;
import main.java.dataneat.genome.NeatChromosome;
import main.java.dataneat.utils.PropertiesHolder;

public class LinkWeightOperator extends BaseNeat implements MutationOperator {

	private static final String POWER = "mutationPower";
	private static final String WEIGHT_MUTATION_RATE = "weightMutationRate";
	private double power = 2.5;
	double mutationRate = 0;

	public LinkWeightOperator(PropertiesHolder p) {
		super(p);

		power = Double.parseDouble(getParams().getProperty(POWER));
		mutationRate = Double.parseDouble(getParams().getProperty(WEIGHT_MUTATION_RATE));
	}

	@Override
	public void operate(List<NeatChromosome> population) {
		
		if (population == null || mutationRate < 0) {
			return;
		}

		population.stream().filter(chrom -> chrom.checkEligible(mutationRate))
				.forEach(chrom -> chrom.mutateLinkWeight(power));
	}

}
