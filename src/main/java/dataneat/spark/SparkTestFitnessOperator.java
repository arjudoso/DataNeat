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
package dataneat.spark;

import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.nd4j.linalg.dataset.DataSet;

import dataneat.base.BaseNeat;
import dataneat.fitness.TargetFitnessFunction;
import dataneat.genome.NeatChromosome;
import dataneat.spark.function1.SparkNetworkEvalCurr;
import dataneat.utils.PropertiesHolder;

public class SparkTestFitnessOperator extends BaseNeat implements SparkTargetFitnessOperator {
	// almost identical to the fitness operator, but this class instead deals
	// with the test dataset

	private static final String FITNESS_FUNCTION = "fitnessFunction";
	private TargetFitnessFunction fitnessFunction;
	long count = 0l;

	public SparkTestFitnessOperator(PropertiesHolder p) {
		super(p);
		
		try {
			fitnessFunction = (TargetFitnessFunction) Class.forName(getParams().getProperty(FITNESS_FUNCTION))
					.newInstance();

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {			
			e.printStackTrace();
		}

	}

	public void operate(List<NeatChromosome> population, JavaRDD<DataSet> data) {

		if (population == null) {
			// Population list empty:
			// nothing to do.
			// -----------------------------------------------
			return;
		}		
		count = data.count();
		
		population.parallelStream().forEach(chrom -> evaluate(chrom, data));
	}

	private void evaluate(NeatChromosome chrom, JavaRDD<DataSet> data) {
		SparkNetworkEvalCurr eval = new SparkNetworkEvalCurr(getHolder());
		eval.setChrom(chrom);		
		eval.setFitnessFunction(fitnessFunction);
		JavaRDD<Double> scoresDistributed = data.map(eval);
		double fitness = scoresDistributed.reduce((a,b) -> a+b) / count ;		
		chrom.setTestFitness(fitness);
	}
}
