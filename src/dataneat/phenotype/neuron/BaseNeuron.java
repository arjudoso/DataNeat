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

import dataneat.base.BaseNeat;
import dataneat.genome.NeuronGene;
import dataneat.genome.NeuronType;
import dataneat.utils.PropertiesHolder;

public abstract class BaseNeuron extends BaseNeat {

	private static final String CLASSIFICATION = "classification";
	
	//Id of the link split during evolution
	protected int id;
	protected double output = -1.0, input = 0.0, prevOutput = -1.0, delta = 0.0, externalInput = 0.0;
	protected NeuronType type;
	protected boolean classification = false;

	public BaseNeuron(PropertiesHolder p) {
		super(p);
		classification = Boolean.parseBoolean(getParams().getProperty(CLASSIFICATION));
	}

	public BaseNeuron(NeuronGene gene, PropertiesHolder p) {
		super(p);
		classification = Boolean.parseBoolean(getParams().getProperty(CLASSIFICATION));
		this.id = gene.getID();
		this.type = gene.getNeuronType();
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
	
	public double getPreviousOutput() {
		return prevOutput;
	}
	
	public void setPreviousOutput(double out) {
		prevOutput = out;
	}

	public double getOutput() {
		return output;
	}

	public void setOutput(double output) {
		this.output = output;
	}

	public double getInput() {
		return input;
	}

	public void setInput(double input) {
		this.input = input;
	}

	public NeuronType getType() {
		return type;
	}

	public void setType(NeuronType type) {
		this.type = type;
	}
	
	public double getExternalInput() {
		return externalInput;
	}

	public void setExternalInput(double externalInput) {
		this.externalInput = externalInput;
	}
	
}
