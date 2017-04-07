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

import java.util.ArrayList;
import java.util.List;

import dataneat.genome.NeatChromosome;

public class CloneOperator {

	public List<NeatChromosome> operate(NeatChromosome chrom, int numOffspring) {
		
		List<NeatChromosome> offspring = new ArrayList<NeatChromosome>();
		offspring.add(chrom);
		
		for (int i = 0; i < (numOffspring - 1); i++) {
			NeatChromosome clone = new NeatChromosome(chrom);
			offspring.add(clone);
		}
		
		return offspring;
	}
}
