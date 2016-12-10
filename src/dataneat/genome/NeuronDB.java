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
package dataneat.genome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataneat.utils.RandGen;

public class NeuronDB {

	private List<NeuronGene> neuronList = new ArrayList<NeuronGene>();
	private Map<Integer, NeuronGene> neuronMap = new HashMap<Integer, NeuronGene>();
	private List<Integer> inputIds = new ArrayList<Integer>();
	private List<Integer> outputIds = new ArrayList<Integer>();
	private List<Integer> hiddenIds = new ArrayList<Integer>();
	private int biasId;

	public NeuronDB() {
	}
	
	public NeuronDB (NeuronDB parent) {
		for (NeuronGene parentNeuron : parent.neuronList) {
			NeuronGene newNeuron = new NeuronGene(parentNeuron);
			addNeuron(newNeuron, newNeuron.getNeuronType());
		}
	}

	public void addNeuron(NeuronGene neuron, NeuronType type) {

		neuronList.add(neuron);
		neuronMap.put(neuron.getID(), neuron);

		switch (type) {
		case INPUT:
			inputIds.add(neuron.getID());
			break;
		case OUTPUT:
			outputIds.add(neuron.getID());
			break;
		case HIDDEN:
			hiddenIds.add(neuron.getID());
		case BIAS:
			biasId = neuron.getID();
		default:
			break;
		}
	}

	public void addAll(NeuronDB neuronsToAdd) {

		for (NeuronGene gene : neuronsToAdd.getNeuronList()) {
			addNeuron(gene, gene.getNeuronType());
		}
	}

	public void removeNeuron(int id) {

		NeuronGene neuron = neuronMap.get(id);

		switch (neuron.getNeuronType()) {
		case INPUT:
			inputIds.remove(neuron);
			break;
		case OUTPUT:
			outputIds.remove(neuron);
			break;
		case HIDDEN:
			hiddenIds.remove(neuron);
		case BIAS:
			biasId = Integer.MIN_VALUE;
		default:
			break;
		}

		neuronMap.remove(id);

		neuronList.remove(neuron);
	}
	
	public void sortBySplitY() {
		neuronList.sort((neuron1, neuron2) -> Double.compare(neuron1.getSplitY(), neuron2.getSplitY()));
	}

	public NeuronGene randomNeuron() {
		int index = RandGen.rand.nextInt(neuronList.size());
		return neuronList.get(index);
	}
	
	public NeuronGene randomInput() {
		int index = RandGen.rand.nextInt(inputIds.size());
		int id = inputIds.get(index);
		return getById(id);
	}
	
	public NeuronGene randomOutput() {
		int index = RandGen.rand.nextInt(outputIds.size());
		int id = outputIds.get(index);
		return getById(id);
	}

	public NeuronGene getByIndex(int index) {
		return neuronList.get(index);
	}

	public NeuronGene getById(int id) {
		return neuronMap.get(id);
	}

	public List<NeuronGene> getNeuronList() {
		return neuronList;
	}

	public void setNeuronList(List<NeuronGene> neuronList) {
		this.neuronList = neuronList;
	}

	public Map<Integer, NeuronGene> getNeuronMap() {
		return neuronMap;
	}

	public void setNeuronMap(Map<Integer, NeuronGene> neuronMap) {
		this.neuronMap = neuronMap;
	}

	public int sizeWithBias() {
		// return size of all neurons, including bias neuron, the network
		// calls this method to know how many neurons to build
		return neuronList.size();
	}
	
	public int sizeWithoutBias() {
		return (neuronList.size() - 1);
	}

	public List<Integer> getInputIds() {
		return inputIds;
	}

	public List<Integer> getOutputIds() {
		return outputIds;
	}

	public List<Integer> getHiddenIds() {
		return hiddenIds;
	}

	public int getBiasId() {
		return biasId;
	}
}
