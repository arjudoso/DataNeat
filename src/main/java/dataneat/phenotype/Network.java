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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import dataneat.base.BaseNeat;
import dataneat.genome.LinkGene;
import dataneat.genome.NeatChromosome;
import dataneat.utils.PropertiesHolder;

public class Network extends BaseNeat {

	private static final long serialVersionUID = 1L;	
	Integer batchSize = 50;
	private Map<Integer, GeneralNeuron> neurons = new HashMap<Integer, GeneralNeuron>();
	private List<GeneralNeuron> inputNeurons = new ArrayList<GeneralNeuron>();
	private List<GeneralNeuron> hiddenNeurons = new ArrayList<GeneralNeuron>();
	private List<GeneralNeuron> outputNeurons = new ArrayList<GeneralNeuron>();
	private int biasId, size = 2;
	INDArray stabilMatrix;
	
	public Network(){}

	public Network(NeatChromosome chrom, PropertiesHolder p, INDArray stabilMatrix, int batchSize) {
		super(p);
		this.stabilMatrix = stabilMatrix;		
		//batchSize = Integer.parseInt(getParams().getProperty(BATCHSIZE));		

		// first create the nodes

		for (int i = 0; i < chrom.getNeurons().sizeWithBias(); i++) {
			GeneralNeuron neuron = new GeneralNeuron(chrom.getNeurons().getByIndex(i), getHolder(), this.stabilMatrix, batchSize);
			addNeuron(neuron);
		}

		size = neurons.size();

		// then link the nodes
		for (int i = 0; i < chrom.getLinks().size(); i++) {
			LinkGene l = chrom.getLinks().getByIndex(i);
			// skip disabled links
			if (l.isEnabled()) {
				// get the neuron that this link is going to and stash the id of
				// the neuron it is from along with the links weight
				int to = l.getToNeuronID();
				int from = l.getFromNeuronID();

				neurons.get(to).addLink(from, l.getWeight());
			}
		}
	}

	// shape = (batchSize,numInputs)
	public void computeNetPrevTimestep(INDArray inputs) {

		// computes the network based on the given inputs.

		boolean stable = false;

		// flush through the inputs

		for (int i = 0; i < inputNeurons.size(); i++) {

			// get the current input into the system on the first time
			// through
			// the (possibly) recurrent network
			GeneralNeuron n = inputNeurons.get(i);

			// row vector so transpose
			n.setOutput(inputs.tensorAlongDimension(i, 0).transpose());
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
			for (GeneralNeuron n : hiddenNeurons) {
				n.step();
			}

			for (GeneralNeuron n : outputNeurons) {
				n.step();
			}

			computeSinglePassPrev();

			stable = checkDeltaMet();
			count++;
		}
	}

	// shape = (batchSize,numInputs)
	public void computeNetCurrentTimestep(INDArray inputs) {

		// computes the network based on the given inputs.

		for (int i = 0; i < inputNeurons.size(); i++) {

			GeneralNeuron n = inputNeurons.get(i);

			// row vector so transpose
			n.setOutput(inputs.tensorAlongDimension(i, 0).transpose());
			n.step();
		}

		Collections.sort(hiddenNeurons, (neuron1, neuron2) -> Double.compare(neuron1.getSplitY(), neuron2.getSplitY()));
		computeSinglePassCurr();
	}

	public INDArray getOutput() {
		// outputs should always be columns vectors from individual neurons
		INDArray outs = null;
		List<INDArray> outputs = new ArrayList<INDArray>();		

		for (GeneralNeuron n : outputNeurons) {
			outputs.add(n.getOutput());			
		}

		outs = Nd4j.hstack(outputs);
		// shape = (batchSize,numOutputs)
		return outs;
	}

	private void addNeuron(GeneralNeuron neuron) {
		switch (neuron.getType()) {
		case BIAS:
			biasId = neuron.getId();
			break;
		case HIDDEN:
			hiddenNeurons.add(neuron);
			break;
		case INPUT:
			inputNeurons.add(neuron);
			break;
		case OUTPUT:
			outputNeurons.add(neuron);
			break;
		default:
			break;
		}

		neurons.put(neuron.getId(), neuron);
	}

	private void computeSinglePassPrev() {

		for (GeneralNeuron n : hiddenNeurons) {
			evalGenNeuronPrev(n);
		}

		for (GeneralNeuron n : outputNeurons) {
			evalGenNeuronPrev(n);
		}
	}

	private void computeSinglePassCurr() {
		for (GeneralNeuron n : hiddenNeurons) {
			evalGenNeuronCurr(n);
		}

		for (GeneralNeuron n : outputNeurons) {
			evalGenNeuronCurr(n);
		}
	}

	
	private INDArray evalGenNeuronPrev(GeneralNeuron n) {
		int i = 0;
		INDArray accum = null;
		List<INDArray> temp = new ArrayList<INDArray>();

		if (n.getLinkWeights().size() == 0) {			
			return n.computeRecurr();
		}

		double[] weights = new double[n.getLinkWeights().size()];

		for (Map.Entry<Integer, Double> e : n.getLinkWeights().entrySet()) {
			// output is always a column vector
			INDArray output = neurons.get(e.getKey()).getPreviousOutput();
			weights[i] = e.getValue();
			temp.add(output);
			i++;
		}

		// shape = (batchSize,numInputsToNeuron)
		accum = Nd4j.hstack(temp);
		// shape (numInputsToNeuron, 1)
		INDArray ndWeights = Nd4j.create(weights, new int[] { weights.length, 1 });
		n.setInput(accum.mmul(ndWeights));
		return n.computeRecurr();
	}
	
	private INDArray evalGenNeuronCurr(GeneralNeuron n) {
		int i = 0;
		INDArray accum = null;
		List<INDArray> temp = new ArrayList<INDArray>();

		if (n.getLinkWeights().size() == 0) {			
			return n.compute();
		}

		double[] weights = new double[n.getLinkWeights().size()];

		for (Map.Entry<Integer, Double> e : n.getLinkWeights().entrySet()) {
			// output is always a column vector
			INDArray output = neurons.get(e.getKey()).getOutput();
			weights[i] = e.getValue();
			temp.add(output);
			i++;
		}

		// shape = (batchSize,numInputsToNeuron)
		accum = Nd4j.hstack(temp);
		// shape (numInputsToNeuron, 1)
		INDArray ndWeights = Nd4j.create(weights, new int[] { weights.length, 1 });
		n.setInput(accum.mmul(ndWeights));
		return n.compute();
	}

	private boolean checkDeltaMet() {
		// checks the delta of each neuron to see if it is below the threshhold.
		// If any delta is above threshhold, function returns false

		for (GeneralNeuron n : hiddenNeurons) {

			if (!n.isStable()) {
				return false;
			}
		}

		for (GeneralNeuron n : outputNeurons) {

			if (!n.isStable()) {
				return false;
			}
		}
		return true;
	}
	
	public void setBatchSize(int size) {
		batchSize = size;
	}
}
