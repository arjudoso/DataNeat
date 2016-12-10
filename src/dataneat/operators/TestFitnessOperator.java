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

import java.io.IOException;
import java.util.List;

import dataneat.base.BaseNeat;
import dataneat.data.TableData;
import dataneat.data.TargetDataset;
import dataneat.fitness.TargetFitnessFunction;
import dataneat.genome.NeatChromosome;
import dataneat.phenotype.Network;
import dataneat.utils.PropertiesHolder;

public class TestFitnessOperator extends BaseNeat {
	// almost identical to the fitness operator, but this class instead deals
	// with the test dataset

	private static final String FITNESS_FUNCTION = "fitnessFunction";
		
	private TargetFitnessFunction fitnessFunction;

	public TestFitnessOperator(PropertiesHolder p) {
		super(p);
		
		try {
			fitnessFunction = (TargetFitnessFunction) Class.forName(getParams().getProperty(FITNESS_FUNCTION))
					.newInstance();

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void operate(List<NeatChromosome> population, TargetDataset data) {

		if (population == null) {
			// Population list empty:
			// nothing to do.
			// -----------------------------------------------
			return;
		}

		population.parallelStream().forEach(chrom -> evaluate(chrom, data));		
	}

	private void evaluate(NeatChromosome chrom, TargetDataset data) {
		// this function evaluates a single chromosome

		// create phenotype
		Network net = new Network(chrom, getHolder());

		// create a data object for the network's outputs
		TableData outputs = new TableData(data.numIdealColumns());

		// compute network output for each input row
		for (int i = 0; i < data.numRowsReduced(); i++) {
			net.computeNet(data.getInputRow(i));
			try {
				outputs.addRow(net.getOutput());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// set fitness
		double testFitness = fitnessFunction.computeFitness(data, outputs);
		chrom.setTestFitness(testFitness);
	}
}
