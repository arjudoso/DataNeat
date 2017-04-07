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
package main.java.dataneat.genome;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.dataneat.utils.RandGen;

public class LinkDB {

	public List<LinkGene> linkGeneList = new ArrayList<LinkGene>();
	public Map<Integer, LinkGene> linkGeneMap = new HashMap<Integer, LinkGene>();
	public Map<Point,LinkGene> linksByTermination = new HashMap<Point,LinkGene>();
	
	public LinkDB() {
	}
	
	public LinkDB (LinkDB parent) {
		for (LinkGene parentLink : parent.linkGeneList) {
			LinkGene newLink = new LinkGene(parentLink);
			add(newLink);
		}
	}

	public void add(LinkGene linkGene) {
		linkGeneList.add(linkGene);
		linkGeneMap.put(linkGene.getInnovationID(), linkGene);		
		linksByTermination.put(new Point(linkGene.getFromNeuronID(),linkGene.getToNeuronID()), linkGene);
	}	

	public void removeLinkByID(int id) {
		LinkGene link = linkGeneMap.get(id);
		linkGeneMap.remove(id);
		linkGeneList.remove(link);
		linksByTermination.remove(link.getFromNeuronID(),link.getToNeuronID());
	}		

	public void removeLinkByIndex(int index) {		
		LinkGene link = linkGeneList.get(index);
		int id = link.getInnovationID();
		linkGeneMap.remove(id);
		linkGeneList.remove(index);
		linksByTermination.remove(new Point(link.getFromNeuronID(),link.getToNeuronID()));
	}	
	
	public void removeByTerminations(int fromId, int toId) {
		LinkGene link = getByTerminations(fromId, toId);
		linkGeneList.remove(link);
		linkGeneMap.remove(link.getInnovationID());
		linksByTermination.remove(new Point(fromId,toId));
	}

	public LinkGene randomLink() {
		int index = RandGen.rand.nextInt(linkGeneList.size());
		return linkGeneList.get(index);
	}	

	public LinkGene getByIndex(int index) {
		return linkGeneList.get(index);
	}

	public LinkGene getById(int id) {
		return linkGeneMap.get(id);
	}
	
	public LinkGene getByTerminations(int fromNeuronId, int toNeuronId) {
		return linksByTermination.get(new Point(fromNeuronId, toNeuronId));
	}	

	public int size() {
		return linkGeneList.size();
	}		
	
	public void set(int index, LinkGene link) {
		linkGeneList.set(index, link);
		linkGeneMap.put(link.getInnovationID(), link);
		linksByTermination.put(new Point(link.getFromNeuronID(),link.getToNeuronID()), link);
	}
	
	public void sortById() {
		//only sorts the list
		linkGeneList.sort((link1, link2) -> link1.getInnovationID().compareTo(link2.getInnovationID()));
	}	
}
