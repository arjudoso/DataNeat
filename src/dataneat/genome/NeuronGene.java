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
import java.util.List;

public class NeuronGene {

	NeuronType neuronType;

	private double splitX = 0.0, splitY = 0.0;

	private boolean loopBack = false;

	private List<Integer> inputs = new ArrayList<Integer>();

	// neurons given ID based on the innovation of the link they split
	private int splitLinkID = 0, numOutputs = 0;

	public NeuronGene(int id, NeuronType type) {
		neuronType = type;
		splitLinkID = id;
	}

	public NeuronGene(NeuronGene parentNeuron) {
		this.neuronType = parentNeuron.getNeuronType();
		this.splitX = parentNeuron.getSplitX();
		this.splitY = parentNeuron.getSplitY();
		this.loopBack = parentNeuron.isLoopBack();
		this.splitLinkID = parentNeuron.splitLinkID;
		this.numOutputs = parentNeuron.numOutputs;

		for (Integer input : parentNeuron.inputs) {
			this.inputs.add(input);
		}
	}

	public NeuronType getNeuronType() {
		return neuronType;
	}

	public void setNeuronType(NeuronType neuronType) {
		this.neuronType = neuronType;
	}

	public int getID() {
		return splitLinkID;
	}

	public void setID(int innovationID) {
		this.splitLinkID = innovationID;
	}

	public double getSplitX() {
		return splitX;
	}

	public void setSplitX(double splitX) {
		this.splitX = splitX;
	}

	public double getSplitY() {
		return splitY;
	}

	public void setSplitY(double splitY) {
		this.splitY = splitY;
	}

	public boolean isFullOutputs(int totalNodes) {

		// for recurrent mode, checks if this node is connected to every other
		// node, including itself
		if (numOutputs == totalNodes) {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkInputInvalidRecur(int nodeId) {
		// recurrent networks

		// bias & input neurons can't have input, so return that this neuron is
		// invalid
		if ((neuronType == NeuronType.BIAS) || (neuronType == NeuronType.INPUT)) {
			return true;
		} else {
			// otherwise return invalid if this neuron already has an incomming
			// connection from the parameter neuron
			return inputs.contains(nodeId);
		}
	}

	public boolean checkInputInvalid(int incommingNodeId, double splitY, NeuronType incommingType) {
		// for forward connection only networks

		// output neurons can't make forward connections
		if (incommingType == NeuronType.OUTPUT) {
			return true;
		}

		if ((neuronType == NeuronType.BIAS) || (neuronType == NeuronType.INPUT)) {
			// bias nodes and input nodes do not accept incomming connections
			return true;
		}

		if (incommingType == NeuronType.BIAS) {
			// if the neuron trying to connect to this neuron is a bias node,
			// then just check if this node has a bias connection yet
			return inputs.contains(incommingNodeId);
		}

		
		if (this.neuronType == NeuronType.OUTPUT) {
			// the ending node of this connection is an output neuron, valid
			// as long as connection doesn't already exist
			return (inputs.contains(incommingNodeId));
		} else {

			// we have a hidden neuron
			// return invalid if this neuron already has an incomming
			// connection from the parameter neuron or if the connection
			// would be recurrent
			return (inputs.contains(incommingNodeId) || checkSplitsInvalid(splitY));
		}
	}

	private boolean checkSplitsInvalid(double splitY2) {

		return (compareSplits(splitY, splitY2) != 1);
	}

	private int compareSplits(double split1, double split2) {

		// compares split1 to split2. returns 1 if
		// split1 is larger than or equal to split2, -1 if split1 is less than
		// split2.

		if (split1 >= split2) {
			return 1;
		}

		return -1;
	}

	public void addInput(int connectionFromId) {
		inputs.add(connectionFromId);
	}

	public List<Integer> getInputs() {
		return inputs;
	}

	public boolean isLoopBack() {
		return loopBack;
	}

	public void setloopBack(boolean loopBack) {
		this.loopBack = loopBack;
	}

	public void incrementOutputs() {
		numOutputs++;
	}

	public void decrementOutputs() {
		numOutputs--;
	}
}
