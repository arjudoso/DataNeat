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

import java.util.HashMap;
import java.util.Map;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import dataneat.genome.NeuronGene;
import dataneat.genome.NeuronType;
import dataneat.transferFunction.IdentityFunction;
import dataneat.transferFunction.SigmoidFunction;
import dataneat.transferFunction.TransferFunction;
import dataneat.utils.PropertiesHolder;

public class GeneralNeuron extends BaseNeuron {

	// Integer value = fromNeuron ID not link ID
	private Map<Integer, Double> linkWeights = new HashMap<Integer, Double>();
	private INDArray stabilMatrix;
	private TransferFunction transferFunction;
	private boolean stable = true;

	public GeneralNeuron(NeuronGene gene, PropertiesHolder p, INDArray stabilMatrix) {
		super(gene, p);
		this.stabilMatrix = stabilMatrix;
		
		switch (gene.getNeuronType()) {
		case INPUT:
			transferFunction = new IdentityFunction();
			break;
		case BIAS:			
			output = Nd4j.ones(batchSize,1);
			prevOutput = output;
			break;
		case HIDDEN:
			transferFunction = new SigmoidFunction();
			break;
		case OUTPUT:
			transferFunction = new SigmoidFunction();
			break;
		default:
			break;
		}
	}

	public INDArray compute() {
		//shape = (batchSize,numInputsToNeuron)
		output = transferFunction.compute(input);			
		return output;
	}
	
	public INDArray computeRecurr() {
		output = transferFunction.compute(input);
		delta = Transforms.abs(output.sub(prevOutput), false);
		//will produce a 1 if delta is ever greater than the stability threshold
		INDArray stableBinaryArray = Transforms.greaterThanOrEqual(delta, stabilMatrix);		
		setStable(!(stableBinaryArray.sumNumber().intValue() > 0));		
		return output;
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

	public INDArray getDelta() {
		return delta;
	}

	public boolean isStable() {
		return stable;
	}

	public void setStable(boolean stable) {
		this.stable = stable;
	}

}
