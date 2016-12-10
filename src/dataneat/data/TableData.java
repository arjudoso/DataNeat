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
package dataneat.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dataneat.utils.RandGen;

public class TableData {

	// basic data class for table type data
	//can find range, randomize itself, split into training/test, and generate bootstrapped data sets
	
	private List<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
	private int numCol = 0;
	private int splitIndex = 0; // index to split data at	
	private double colHigh = 0.0, colLow = 0.0;

	public TableData(int numCol) {
		this.numCol = numCol;
	}	
	
	public void splitData(double trainPercent) {
		// method for creating a training/testing data split

		if (trainPercent > 1.0) {
			return;
		}
		
		setSplitIndex((int) (trainPercent * data.size()));
	}

	public List<ArrayList<Double>> getTrainingData() {
		return data.subList(0, splitIndex);
	}

	public List<ArrayList<Double>> getTestData() {
		return data.subList(splitIndex, data.size());
	}

	public void shuffleRows() {
		Collections.shuffle(data, RandGen.rand);
	}

	public List<ArrayList<Double>> bootStrapData(double percentOriginalSize) {
		// method for creating a bootstrapped data set. Returns a new data set
		// that is a random sampling, with replacement, from the original
		
		//can optionally reduce the dataset size
		
		List<ArrayList<Double>> boot = new ArrayList<ArrayList<Double>>();
		
		for (int i = 0; i < (int)(data.size() * percentOriginalSize); i++) {
			int index = RandGen.rand.nextInt(data.size());
			boot.add(data.get(index));
		}
		return boot;
	}

	public List<Double> getRow(int row) {
		return data.get(row);
	}

	public void addRow(List<Double> row) throws IOException {
		if (row.size() != numCol) {
			IOException e = new IOException();
			throw e;
		}
		
		ArrayList<Double> newRow = new ArrayList<Double>();
		
		for (Double d : row) {
			newRow.add(d);
		}

		data.add(newRow);
	}

	public double getRange(int col) {
		double low = Double.MAX_VALUE;
		double high = Double.MIN_VALUE;
		double current;
		double range = 0.0;

		for (ArrayList<Double> row : data) {
			current = row.get(col);

			if (current > high) {
				high = current;
			}

			if (current < low) {
				low = current;
			}
		}

		range = (high - low);
		colHigh = high;
		colLow = low;
		
		return range;
	}
	
	public void normalizeCol(double normHigh, double normLow, int col) {
		getRange(col);
		double current = 0.0, norm = 0.0;
				
		for (ArrayList<Double> row : data) {
			current = row.get(col);
			
			norm = ((current - colLow)*(normHigh - normLow)) / (colHigh - colLow) + normLow;
			row.set(col, norm);
		}
	}
	
	public void normalizeData(double normHigh, double normLow) {
		for (int i = 0; i < numCol; i++) {
			normalizeCol(normHigh, normLow, i);
		}
	}

	public int numRows() {		
		return data.size();
	}

	public int numColumns() {
		return numCol;
	}

	public List<ArrayList<Double>> getData() {
		return data;
	}	

	public int getSplitIndex() {
		return splitIndex;
	}

	public void setSplitIndex(int splitIndex) {
		this.splitIndex = splitIndex;
	}	
}
