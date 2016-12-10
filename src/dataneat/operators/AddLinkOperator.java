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

import java.util.List;

import dataneat.base.BaseNeat;
import dataneat.genome.ConnectivityType;
import dataneat.genome.NeatChromosome;
import dataneat.utils.PropertiesHolder;

public class AddLinkOperator extends BaseNeat implements MutationOperator {

	private static final String ADD_LINK_RATE = "addLinkRate";
	private static final String CONNECTION_MODE = "recurrent";
	private static final String CONNECTION_ATTEMPT_LIMIT = "connectionAttempts";

	double mutationRate = -1.0;
	ConnectivityType connectionType;
	int recurrent = 0;
	private int connectionAttemptLimit = 10;

	public AddLinkOperator(PropertiesHolder p) {

		super(p);

		mutationRate = Double.parseDouble(getParams().getProperty(ADD_LINK_RATE));
		recurrent = Integer.parseInt(getParams().getProperty(CONNECTION_MODE));
		connectionAttemptLimit = Integer.parseInt(getParams().getProperty(CONNECTION_ATTEMPT_LIMIT));

		switch (recurrent) {
		case 0:
			connectionType = ConnectivityType.FORWARD;
			break;
		case 1:
			connectionType = ConnectivityType.RECURRENT;
			break;
		default:
			connectionType = ConnectivityType.FORWARD;
		}
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
				.forEach(t -> t.mutateAddLink(connectionAttemptLimit, connectionType));

	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setMutationRate(int mutationRate) {
		this.mutationRate = mutationRate;
	}
}
