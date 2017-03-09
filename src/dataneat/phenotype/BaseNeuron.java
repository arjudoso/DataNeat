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

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import dataneat.base.BaseNeat;
import dataneat.genome.NeuronGene;
import dataneat.genome.NeuronType;
import dataneat.utils.PropertiesHolder;

public abstract class BaseNeuron extends BaseNeat {	
	
	private static final String BATCHSIZE = "batchSize";
	Integer batchSize = 50;
	//Id of the link split during evolution
	protected int id;
	protected INDArray output, input, prevOutput, delta;
	protected NeuronType type;
	protected double splitY = 0.0;
	
	public BaseNeuron(PropertiesHolder p) {
		super(p);		
		batchSize = Integer.parseInt(getParams().getProperty(BATCHSIZE));
		init();
	}

	public BaseNeuron(NeuronGene gene, PropertiesHolder p) {
		super(p);		
		this.id = gene.getID();
		this.type = gene.getNeuronType();
		this.splitY = gene.getSplitY();
		batchSize = Integer.parseInt(getParams().getProperty(BATCHSIZE));
		init();
	}
	
	private void init() {
		output = Nd4j.zeros(batchSize, 1);
		input = Nd4j.zeros(batchSize, 1);
		prevOutput = output;
		delta = Nd4j.zeros(batchSize, 1);
	}
	
	public void step() {
		prevOutput = output;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public INDArray getPreviousOutput() {
		return prevOutput;
	}
	
	public void setPreviousOutput(INDArray out) {
		prevOutput = out;
	}

	public INDArray getOutput() {
		return output;
	}

	public void setOutput(INDArray output) {
		this.output = output;
	}

	public INDArray getInput() {
		return input;
	}

	public void setInput(INDArray input) {
		this.input = input;
	}

	public NeuronType getType() {
		return type;
	}

	public void setType(NeuronType type) {
		this.type = type;
	}	
	
	public double getSplitY() {
		return splitY;
	}
}
