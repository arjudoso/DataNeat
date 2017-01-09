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
package dataneat.phenotype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataneat.base.BaseNeat;
import dataneat.genome.LinkGene;
import dataneat.genome.NeatChromosome;
import dataneat.phenotype.neuron.GeneralNeuron;
import dataneat.utils.PropertiesHolder;

public class Network extends BaseNeat {

	private static final String STABILIZATION_DELTA = "stabilizationDelta";
	private static final String CLASSIFICATION = "classification";

	private Map<Integer, GeneralNeuron> neurons = new HashMap<Integer, GeneralNeuron>();

	private List<Integer> inputIds = new ArrayList<Integer>();
	private List<Integer> outputIds = new ArrayList<Integer>();
	private List<Integer> hiddenIds = new ArrayList<Integer>();
	private int biasId, size = 2;
	private boolean classification = false;
	private double stabilizationDelta = 0.01;

	public Network(PropertiesHolder p) {
		super(p);
	}

	public Network(NeatChromosome chrom, PropertiesHolder p) {
		super(p);
		stabilizationDelta = Double.parseDouble(getParams().getProperty(STABILIZATION_DELTA));
		classification = Boolean.parseBoolean(getParams().getProperty(CLASSIFICATION));
		// size = chrom.getNeurons().sizeWithBias();

		// first create the nodes

		for (int i = 0; i < chrom.getNeurons().sizeWithBias(); i++) {
			GeneralNeuron neuron = new GeneralNeuron(chrom.getNeurons().getByIndex(i), getHolder());
			addNeuron(neuron);
		}

		size = neurons.size();

		// then link the nodes
		for (LinkGene l : chrom.getLinks()) {
			// skip disabled links
			if (l.isEnabled()) {
				// get the neuron that this link is going to and stash the id of
				// the
				// neuron it is from along with the links weight
				int to = l.getToNeuronID();
				int from = l.getFromNeuronID();

				neurons.get(to).addLink(from, l.getWeight());
			}
		}
	}

	public void computeNet(List<Double> inputRow) {

		// computes the network based on the given inputs.

		boolean stable = false;

		// flush through the inputs

		for (int i = 0; i < inputIds.size(); i++) {

			// get the current input into the system on the first time
			// through
			// the (possibly) recurrent network
			GeneralNeuron n = neurons.get(inputIds.get(i));
			n.setExternalInput(inputRow.get(i));
			evalGenNeuron(n);
			n.step();
		}

		int count = 0;

		// evaluates the network and checks for stability. It is possible that a
		// network will not stabilize, currently we break the loop after
		// flushing through the network 3 times, however consider weeding
		// networks that do not stabilize out of the population
		while (!stable && count < (size * 3))

		{

			// get neurons ready for timestep
			for (int i = 0; i < hiddenIds.size(); i++) {
				GeneralNeuron n = neurons.get(hiddenIds.get(i));
				n.step();
			}

			for (int i = 0; i < outputIds.size(); i++) {
				GeneralNeuron n = neurons.get(outputIds.get(i));
				n.step();
			}

			computeSinglePass();

			stable = checkDeltaMet();
			count++;
		}

	}

	public ArrayList<Double> getOutput() {

		ArrayList<Double> outs = new ArrayList<Double>();

		for (int i = 0; i < outputIds.size(); i++) {

			int id = outputIds.get(i);
			outs.add(neurons.get(id).getOutput());
		}

		return outs;
	}

	private void addNeuron(GeneralNeuron neuron) {
		switch (neuron.getType()) {
		case BIAS:
			biasId = neuron.getId();
			break;
		case HIDDEN:
			hiddenIds.add(neuron.getId());
			break;
		case INPUT:
			inputIds.add(neuron.getId());
			break;
		case OUTPUT:
			outputIds.add(neuron.getId());
			break;
		default:
			break;
		}

		neurons.put(neuron.getId(), neuron);
	}

	private void computeSinglePass() {

		for (int i = 0; i < hiddenIds.size(); i++) {
			evalGenNeuron(neurons.get(hiddenIds.get(i)));
		}

		if (classification) {
			//apply softmax 
			double sum = 0.0;

			for (int i = 0; i < outputIds.size(); i++) {
				sum += Math.exp(evalGenNeuron(neurons.get(outputIds.get(i))));
			}

			for (int i = 0; i < outputIds.size(); i++) {
				GeneralNeuron n = neurons.get(outputIds.get(i));
				double temp = n.getOutput();
				n.setOutput(Math.exp(temp) / sum);
			}

		} else {

			for (int i = 0; i < outputIds.size(); i++) {
				evalGenNeuron(neurons.get(outputIds.get(i)));
			}
		}
	}

	private double evalGenNeuron(GeneralNeuron n) {

		double accum = 0.0;

		for (Map.Entry<Integer, Double> e : n.getLinkWeights().entrySet()) {

			double output = neurons.get(e.getKey()).getPreviousOutput();
			double weight = e.getValue();
			accum += (output * weight);
		}
		n.setInput(accum);
		return n.compute();
	}

	private boolean checkDeltaMet() {
		// checks the delta of each neuron to see if it is below the threshhold.
		// If any delta is above threshhold, function returns false

		for (int i = 0; i < hiddenIds.size(); i++) {

			if (stabilizationDelta < neurons.get(hiddenIds.get(i)).getDelta()) {
				return false;
			}
		}

		for (int i = 0; i < outputIds.size(); i++) {

			if (stabilizationDelta < neurons.get(outputIds.get(i)).getDelta()) {
				return false;
			}
		}

		return true;
	}
}
