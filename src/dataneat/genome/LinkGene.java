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

import dataneat.utils.RandGen;

public class LinkGene {
	
	private int innovationID = 0;

	private int fromNeuronID = 0, toNeuronID = 0;
	private double weight = 0.0, maxWeight = 10.0, minWeight = -10.0;
	private boolean isEnabled = true, isBias = false, isAlreadySplit = false;

	public LinkGene() {
		
	}
	
	public LinkGene(LinkGene parentLink) {
		
		this.innovationID = parentLink.getInnovationID();
		this.fromNeuronID = parentLink.fromNeuronID;
		this.toNeuronID = parentLink.toNeuronID;
		this.weight = parentLink.getWeight();
		this.maxWeight = parentLink.getMaxWeight();
		this.minWeight = parentLink.getMinWeight();
		this.isEnabled = parentLink.isEnabled;
		this.isBias = parentLink.isBias();
		this.isAlreadySplit = parentLink.isAlreadySplit;
	}

	public void randomizeWeight() {

		weight = (RandGen.rand.nextDouble() * (maxWeight - minWeight) + minWeight);
	}

	public void perturbWeight(double power) {
				
		double maxDelta = Math.abs(weight * power);
		
		weight += (RandGen.rand.nextDouble() * (2 * maxDelta) - maxDelta);
		
		checkBounds();
	}
	
	private void checkBounds() {
		
		if (weight > maxWeight) {
			weight = maxWeight;
		} else {
			if (weight < minWeight) {
				weight = minWeight;
			}
		}
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

	public Integer getInnovationID() {
		return innovationID;
	}

	public void setInnovationID(Integer innovationID) {
		this.innovationID = innovationID;
	}

	public int getFromNeuronID() {
		return fromNeuronID;
	}

	public void setFromNeuronID(int fromNeuron) {
		this.fromNeuronID = fromNeuron;
	}

	public int getToNeuronID() {
		return toNeuronID;
	}

	public void setToNeuronID(int toNeuron) {
		this.toNeuronID = toNeuron;
	}

	public double getMaxWeight() {
		return maxWeight;
	}

	public void setMaxWeight(double maxWeight) {
		this.maxWeight = maxWeight;
	}

	public double getMinWeight() {
		return minWeight;
	}

	public void setMinWeight(double minWeight) {
		this.minWeight = minWeight;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean isBias() {
		return isBias;
	}

	public void setBias(boolean isBias) {
		this.isBias = isBias;
	}
	
	
	public void print() {
		System.out.println("LinkId: " + innovationID);
		System.out.println("To: " + toNeuronID);	
		System.out.println("From: " + fromNeuronID);
		System.out.println("Weight: " + weight);
	}

	public boolean isAlreadySplit() {
		return isAlreadySplit;
	}

	public void setAlreadySplit(boolean isAlreadySplit) {
		this.isAlreadySplit = isAlreadySplit;
	}	
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LinkGene)) {
			return false;
		}
		
		LinkGene l = (LinkGene)o;
		if (Integer.compare(l.innovationID,this.innovationID) == 0) {
			return true;
		}else {
			return false;
		}		
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + innovationID;
		return result;
	}

}
