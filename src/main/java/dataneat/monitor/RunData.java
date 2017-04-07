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

import java.util.ArrayList;
import java.util.List;

import dataneat.base.BaseNeat;
import dataneat.utils.IO;
import dataneat.utils.PropertiesHolder;

public class RunData extends BaseNeat {

	private static final String FILE = "runDataFile";

	private String file;
	private int count = 0;
	private double movingAvgTrain = 0.0, movingAvgTest = 0.0;
	private List<Double> avgTrain = new ArrayList<Double>();
	private static String newline = System.getProperty("line.separator");
	private List<RoundData> rounds = new ArrayList<RoundData>();
	private List<Double> avgTest = new ArrayList<Double>();


	public RunData(PropertiesHolder p) {
		super(p);
		file = getParams().getProperty(FILE);
	}

	
	public void addRound(int roundNumber, int speciesCount, double bestFitness, double bestTest, double speciesThresh) {

		RoundData round = new RoundData(roundNumber, speciesCount, bestFitness, bestTest, speciesThresh);
		rounds.add(round);

		updateMovingAverage(bestFitness, bestTest, 100);
	}

	private void updateMovingAverage(double bestFitness, double bestTest, int window) {
		avgTrain.add(bestFitness);
		avgTest.add(bestTest);

		if (count < window) {
			count++;
		} else {
			avgTrain.remove(0);
			avgTest.remove(0);
			
			double accumTrain = 0.0, accumTest = 0.0;			
			
			for (Double d : avgTrain) {
				accumTrain += d;
			}			
			movingAvgTrain = (accumTrain/window);
			
			for (Double d : avgTest) {
				accumTest += d;
			}
			
			movingAvgTest = (accumTest/1000);
		}		
	}

	public List<RoundData> getRounds() {
		return rounds;
	}

	public void setRounds(List<RoundData> rounds) {
		this.rounds = rounds;
	}

	public void toCSV() {
		StringBuilder sb = new StringBuilder();
		sb.append("RoundNumber, SpeciesCount, BestFitness, BestTest, SpeciesThresh");

		for (RoundData rd : rounds) {
			sb.append(newline);
			sb.append(rd.toString());
		}

		IO.addToFile(sb.toString(), file);

	}

	public void reset() {
		rounds.clear();
	}

	public String printHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("RoundNumber, SpeciesCount, BestFitness, BestTest, SpeciesThresh");
		return sb.toString();
	}

	public void printCurrentRound() {
		System.out.println(printHeader());
		System.out.println(rounds.get(rounds.size() - 1).toString());
	}

	public double getMovingAvgTrain() {
		return movingAvgTrain;
	}

	public void setMovingAvgTrain(double movingAvg) {
		this.movingAvgTrain = movingAvg;
	}
	
	public double getMovingAvgTest() {
		return movingAvgTest;
	}
}
