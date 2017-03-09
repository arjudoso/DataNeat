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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import dataneat.base.BaseNeat;
import dataneat.fitness.TargetFitnessFunction;
import dataneat.genome.NeatChromosome;
import dataneat.phenotype.Network;
import dataneat.utils.PropertiesHolder;

public class TestFitnessOperator extends BaseNeat {
	// almost identical to the fitness operator, but this class instead deals
	// with the test dataset

	private static final String FITNESS_FUNCTION = "fitnessFunction";
	private static final String BATCH_SIZE = "batchSize";

	private TargetFitnessFunction fitnessFunction;
	private INDArray stabil;
	private Integer batchSize = 50;

	public TestFitnessOperator(PropertiesHolder p, INDArray stabilMatrix) {
		super(p);
		stabil = stabilMatrix;
		batchSize = Integer.parseInt(getParams().getProperty(BATCH_SIZE));

		try {
			fitnessFunction = (TargetFitnessFunction) Class.forName(getParams().getProperty(FITNESS_FUNCTION))
					.newInstance();

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void operate(List<NeatChromosome> population, DataSet data) {

		if (population == null) {
			// Population list empty:
			// nothing to do.
			// -----------------------------------------------
			return;
		}

		population.parallelStream().forEach(chrom -> evaluate(chrom, data));
	}

	private void evaluate(NeatChromosome chrom, DataSet data) {
		// this function evaluates a single chromosome

		// create phenotype
		Network net = new Network(chrom, getHolder(), stabil);

		// compute on entire training set
		net.computeNetPrevTimestep(data.getFeatures());
		// set fitness
		double testFitness = fitnessFunction.computeFitness(data.getLabels(), net.getOutput(), batchSize);
		chrom.setTestFitness(testFitness);
	}
}
