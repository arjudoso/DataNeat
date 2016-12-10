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
package dataneat.fitness;

import java.util.List;

import dataneat.data.TableData;
import dataneat.data.TargetDataset;

public class MultiLogLoss implements TargetFitnessFunction {

	@Override
	public double computeFitness(TargetDataset data, TableData outputs) {
		double accum = 0.0;
		double max = 0.9999999999;
		double min = 0.0000000001;

		for (int i = 0; i < outputs.numRows(); i++) {
			// iterate over number of training examples

			// get the ideal and actual outputs for this example
			List<Double> ideal = data.getIdealRow(i);
			List<Double> out = outputs.getRow(i);

			for (int j = 0; j < out.size(); j++) {
				// iterate over number of classes

				double temp = 0;
				// only compute if we are on the correct label
				if (ideal.get(j) == 1) {
					temp = out.get(j);

					if (temp > max) {
						temp = max;
					}
					if (temp < min) {
						temp = min;
					}
					accum += Math.log(temp);
				}
			}

		}

		return -(accum / outputs.numRows());
	}

}
