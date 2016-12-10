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

import dataneat.data.TableData;
import dataneat.data.TargetDataset;

public class LogLoss implements TargetFitnessFunction {
	// supports single output

	@Override
	public double computeFitness(TargetDataset data, TableData outputs) {
		double accum = 0.0;
		
		double max = 0.9999999999;
		double min = 0.0000000001;

		for (int i = 0; i < outputs.numRows(); i++) {
			
			double ideal = data.getIdealRow(i).get(0);
			double out = outputs.getRow(i).get(0);

			if (out > max) {
				out = max;
			}
			if (out < min) {
				out = min;
			}

			
			accum += (ideal * Math.log(out)) + (1 - ideal) * Math.log(1 - out);
		}

		return -(accum / outputs.numRows());		
	}

}
