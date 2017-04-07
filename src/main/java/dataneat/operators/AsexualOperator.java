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

import java.util.ArrayList;
import java.util.List;

import main.java.dataneat.genome.NeatChromosome;
import main.java.dataneat.utils.RandGen;

public class AsexualOperator {

	public List<NeatChromosome> operate(List<NeatChromosome> chroms, int numOffspring) {
		
		List<NeatChromosome> offspring = new ArrayList<NeatChromosome>();
		
		for (int i = 0; i<numOffspring; i++) {
			int index = RandGen.rand.nextInt(chroms.size());
			NeatChromosome clone = new NeatChromosome(chroms.get(index));
			offspring.add(clone);
		}
		
		return offspring;
	}
}
