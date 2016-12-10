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
package dataneat.monitor;

public class RoundData {

	private int round = 0, speciesCount = 0;
	private double bestFitness = 0.0, speciesThresh = 0.0, bestTest = 0.0;
	
	public RoundData (int round, int speciesCount, double fitness, double testFitness, double speciesThresh) {
		this.round = round;
		this.speciesCount = speciesCount;
		this.bestFitness = fitness;
		this.speciesThresh = speciesThresh;
		this.setBestTest(testFitness);
	}
	
	@Override
	public String toString() {
		String s = round + "," + speciesCount + "," + bestFitness + "," + bestTest + "," + speciesThresh;
		return s;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getSpeciesCount() {
		return speciesCount;
	}

	public void setSpeciesCount(int speciesCount) {
		this.speciesCount = speciesCount;
	}

	public double getBestFitness() {
		return bestFitness;
	}

	public void setBestFitness(double bestFitness) {
		this.bestFitness = bestFitness;
	}

	public double getSpeciesThresh() {
		return speciesThresh;
	}

	public void setSpeciesThresh(double speciesThresh) {
		this.speciesThresh = speciesThresh;
	}

	public double getBestTest() {
		return bestTest;
	}

	public void setBestTest(double bestTest) {
		this.bestTest = bestTest;
	}

}
