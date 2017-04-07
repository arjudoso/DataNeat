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

public class AddNodeOperator extends BaseNeat implements MutationOperator {

	private static final String ADD_NODE_RATE = "addNodeRate";

	double mutationRate = 0;

	public AddNodeOperator(PropertiesHolder p) {

		super(p);

		mutationRate = Double.parseDouble(getParams().getProperty(ADD_NODE_RATE));
	}

	public void operate(List<NeatChromosome> population) {

		if (population == null) {
			// Population list empty:
			// nothing to do.
			// -----------------------------------------------
			return;
		}

		if (mutationRate < 0) {			
			return;
		}

		population.stream().filter(t -> t.checkEligible(mutationRate)).forEach(t -> t.mutateAddNode());
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setMutationRate(int mutationRate) {
		this.mutationRate = mutationRate;
	}
}
