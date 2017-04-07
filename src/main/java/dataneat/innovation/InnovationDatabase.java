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
package main.java.dataneat.innovation;

import java.util.ArrayList;
import java.util.List;

import main.java.dataneat.genome.LinkGene;

public class InnovationDatabase {
	
	private int innovationCount = 0;
	private List<LinkInnovation> links = new ArrayList<LinkInnovation>();	

	private InnovationDatabase(){};
	
	private static class InnovationDatabaseHolder {
		
		private static final InnovationDatabase INSTANCE = new InnovationDatabase();
	}
	
	public static InnovationDatabase getInnovationDatabase() {
		
		return InnovationDatabaseHolder.INSTANCE;
	}
	
	private void addInnovation(int fromNeuron, int toNeuron) {
		
		LinkInnovation innovation = new LinkInnovation();
		innovation.setInnovationID(innovationCount);
		
		innovationCount++;
		
		innovation.setFromNeuron(fromNeuron);
		innovation.setToNeuron(toNeuron);
		
		links.add(innovation);		
	}	
	
	public int evaluateGene (LinkGene link) {
		
		int fromNeuron = link.getFromNeuronID();
		int toNeuron = link.getToNeuronID();
		int ID = -1;
		
		for (LinkInnovation l : links) {
			
			if (fromNeuron == (l.getFromNeuron()) && toNeuron == (l.getToNeuron())) {
				
				ID = l.getInnovationID();
				break;
			}
		}
		
		if (ID < 0) {
			
			ID = innovationCount;
			addInnovation(fromNeuron, toNeuron);
		}
		
		return ID;
	}	
	
	public int getInnovationCount() {
		return innovationCount;
	}
	
}
