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

public class RemoveNodeOperator extends BaseNeat implements MutationOperator {

	private static final String REMOVE_NODE_RATE = "removeNodeRate";
	private static final String REMOVE_NODE_ATTEMPTS = "removeNodeAttempts";
	double mutationRate = -1.0;	
	int removeNodeAttempts = 0;

	public RemoveNodeOperator(PropertiesHolder p) {
		super(p);
		mutationRate = Double.parseDouble(getParams().getProperty(REMOVE_NODE_RATE));		
		removeNodeAttempts = Integer.parseInt(getParams().getProperty(REMOVE_NODE_ATTEMPTS));		
	}

	public void operate(List<NeatChromosome> population) {

		if (population == null) {
			// Population list empty:
			// nothing to do.
			// -----------------------------------------------
			return;
		}

		if (mutationRate < 0) {
			// If the mutation rate is negative we don't perform any mutation.
			// ----------------------------------------------------------------
			return;
		}

		population.stream().filter(t -> t.checkEligible(mutationRate))
				.forEach(t -> t.removeNodeRandom(removeNodeAttempts));
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setMutationRate(int mutationRate) {
		this.mutationRate = mutationRate;
	}
}
