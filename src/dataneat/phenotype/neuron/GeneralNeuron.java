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
package dataneat.phenotype.neuron;

import java.util.HashMap;
import java.util.Map;

import dataneat.genome.NeuronGene;
import dataneat.genome.NeuronType;
import dataneat.transferFunction.BiasFunction;
import dataneat.transferFunction.IdentityFunction;
import dataneat.transferFunction.SigmoidFunction;
import dataneat.transferFunction.SoftMax;
import dataneat.transferFunction.TransferFunction;
import dataneat.utils.PropertiesHolder;

public class GeneralNeuron extends BaseNeuron {

	// Integer value = fromNeuron ID not link ID
	private Map<Integer, Double> linkWeights = new HashMap<Integer, Double>();

	private TransferFunction transferFunction;

	public GeneralNeuron(NeuronGene gene, PropertiesHolder p) {
		super(gene, p);

		switch (gene.getNeuronType()) {
		case INPUT:
			transferFunction = new IdentityFunction();
			break;
		case BIAS:
			transferFunction = new BiasFunction();
			output = 1.0;
			prevOutput = 1.0;
			break;
		case HIDDEN:
			transferFunction = new SigmoidFunction();
			break;
		case OUTPUT:
			if (classification) {
				transferFunction = new SoftMax();
			} else {
				transferFunction = new SigmoidFunction();
			}
			break;
		default:
			break;
		}
	}

	public void compute() {
		output = transferFunction.compute(input + externalInput);
		delta = Math.abs(output - prevOutput);
	}
	
	public void compute(double divisor) {
		//used for softmax output neurons
		output = (transferFunction.compute(input + externalInput) / divisor);
		delta = Math.abs(output - prevOutput);
	}

	public TransferFunction getTransferFunction() {
		return transferFunction;
	}

	public void setTransferFunction(TransferFunction transferFunction) {
		this.transferFunction = transferFunction;
	}

	public Map<Integer, Double> getLinkWeights() {
		return linkWeights;
	}

	public void setLinkWeights(Map<Integer, Double> linkWeights) {
		this.linkWeights = linkWeights;
	}

	public void addLink(int from, double weight) {
		linkWeights.put(from, weight);
	}

	public double getWeight(int from) {
		return linkWeights.get(from);
	}

	public NeuronType getType() {
		return type;
	}

	public void setType(NeuronType type) {
		this.type = type;
	}

	public double getDelta() {
		return delta;
	}

}
