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

public class RMSE implements TargetFitnessFunction {
	
	public RMSE() {
	}

	@Override
	public double computeFitness(TargetDataset data, TableData outputs) {
		// currently only works for networks with a single output
		double rmse = 0.0;
		double mse = 0.0;
		double accum = 0.0;
		
		for (int i = 0; i < outputs.numRows(); i++) {

			accum += Math.pow((data.getIdealRow(i).get(0) - outputs.getRow(i).get(0)), 2);
		}
		
		mse = accum / outputs.numRows();

		rmse = Math.sqrt(mse);
					
		return rmse;
	}
}
