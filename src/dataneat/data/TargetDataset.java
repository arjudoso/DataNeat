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

import dataneat.base.BaseNeat;
import dataneat.utils.DataManipChecker;
import dataneat.utils.InputParser;
import dataneat.utils.PropertiesHolder;
import dataneat.utils.RandGen;

public class TargetDataset extends BaseNeat {

	// deals with data for supervised learning, holds 2 tableData objects, one
	// for the inputs and one for the ideal outputs. Keeps them in lockstep with
	// each other

	// randomizing and bootstrapping are initiated by the Engine

	// can split itself into subsets

	private static final String DATA_MANIPULATION = "dataManipulation";	
	private String regex = ",";
	private Integer dataManip = 0;
	private DataManipulation manipulation;
	private TableData input;
	private TableData ideal;
	private List<Integer> dataOrder = new ArrayList<Integer>();
	private List<Integer> permutedOrder = new ArrayList<Integer>();
	private List<Integer> bootstrapOrder;
	private int splitIndex = -1;
	private double percentOriginalSize = 1.0;

	public TargetDataset(PropertiesHolder p, String inputFile, int numColTotal, int numOutputs, boolean header)
			throws IOException {
		super(p);		

		ArrayList<TableData> data = InputParser.parse(header, regex, inputFile, numColTotal, numOutputs);			
		input = data.get(0);
		ideal = data.get(1);
		init(input, ideal);
	}

	public TargetDataset(PropertiesHolder p, TableData input, TableData ideal) throws IOException {
		super(p);
		this.setInput(input);
		this.setIdeal(ideal);

		init(input, ideal);
	}

	private void init(TableData input, TableData ideal) throws IOException {
		if (input.numRows() != ideal.numRows()) {
			throw new IOException();
		}

		for (int i = 0; i < input.numRows(); i++) {
			dataOrder.add(i);
			permutedOrder.add(i);
		}	

		dataManip = Integer.parseInt(getParams().getProperty(DATA_MANIPULATION));

		manipulation = DataManipChecker.checkDataManip(dataManip);
	}

	public int numIdealColumns() {
		return ideal.numColumns();
	}

	public int numInputColumns() {
		return input.numColumns();
	}

	public int numRowsOriginal() {
		// rows in input and ideal must be the same
		return input.numRows();
	}
	
	public int numRowsReduced() {
		return (int) (input.numRows() * percentOriginalSize);
	}	

	public List<Double> getInputRow(int index) {

		switch (manipulation) {

		case BOOTSTRAP:
			return getBootstrapInputRow(index);

		case RANDOMIZE:
		case CONTINUOUS_RANDOMIZE:
			return getShuffledInputRow(index);

		case NONE:
		default:
			return getOrderedInputRow(index);
		}
	}

	public List<Double> getIdealRow(int index) {

		switch (manipulation) {

		case BOOTSTRAP:
			return getBootstrapIdealRow(index);

		case RANDOMIZE:
		case CONTINUOUS_RANDOMIZE:
			return getShuffledIdealRow(index);

		case NONE:
		default:
			return getOrderedIdealRow(index);
		}
	}

	public DataManipulation getManipulation() {
		return manipulation;
	}

	public void setManipulation(DataManipulation manipulation) {
		this.manipulation = manipulation;
	}

	private List<Double> getOrderedInputRow(int index) {
		return input.getRow(dataOrder.get(index));
	}

	private List<Double> getOrderedIdealRow(int index) {
		return ideal.getRow(dataOrder.get(index));
	}

	private List<Double> getShuffledInputRow(int index) {
		return input.getRow(permutedOrder.get(index));
	}

	private List<Double> getShuffledIdealRow(int index) {
		return ideal.getRow(permutedOrder.get(index));
	}

	private List<Double> getBootstrapInputRow(int index) {
		return input.getRow(bootstrapOrder.get(index));
	}

	private List<Double> getBootstrapIdealRow(int index) {
		return ideal.getRow(bootstrapOrder.get(index));
	}

	public void shuffleRows() {
		// permutes the ordering list, which is used to fetch rows in both
		// the input and ideal data sets
		Collections.shuffle(permutedOrder, RandGen.rand);
	}
	
	public void normalizeInput() {
		input.normalizeData(1.0, -1.0);
	}

	public void splitData(double trainPercent) {
		// method for creating a training/testing data split

		if (trainPercent > 1.0) {
			return;
		}

		setSplitIndex(((int) (trainPercent * dataOrder.size())));
	}

	public List<TargetDataset> createSubsets() throws IOException {
		// method for creating training & testing data subsets
		TableData trainingInput = new TableData(numInputColumns());
		TableData trainingIdeal = new TableData(numIdealColumns());
		TableData testInput = new TableData(numInputColumns());
		TableData testIdeal = new TableData(numIdealColumns());

		for (int i = 0; i < permutedOrder.size(); i++) {
			// if the data has not been randomized, permuted order will still be
			// equal to ordered, if it has been then it will be shuffled. So
			// just use permuted order either way

			int index = permutedOrder.get(i);

			if (i < splitIndex) {
				trainingInput.addRow(input.getRow(index));
				trainingIdeal.addRow(ideal.getRow(index));
			} else {
				testInput.addRow(input.getRow(index));
				testIdeal.addRow(ideal.getRow(index));
			}
		}

		TargetDataset train = new TargetDataset(getHolder(), trainingInput, trainingIdeal);
		TargetDataset test = new TargetDataset(getHolder(), testInput, testIdeal);

		List<TargetDataset> subsets = new ArrayList<TargetDataset>();
		subsets.add(train);
		subsets.add(test);
		return subsets;
	}

	public void bootStrap() {

		List<Integer> boot = new ArrayList<Integer>();

		for (int i = 0; i < (int)(dataOrder.size()*percentOriginalSize); i++) {
			int index = RandGen.rand.nextInt(dataOrder.size());
			boot.add(dataOrder.get(index));
		}
		bootstrapOrder = boot;
	}

	public void setInput(TableData input) {
		this.input = input;
	}

	public void setIdeal(TableData ideal) {
		this.ideal = ideal;
	}

	public int getSplitIndex() {
		return splitIndex;
	}

	public void setSplitIndex(int splitIndex) {
		this.splitIndex = splitIndex;
	}
	
	public double getPercentOriginalSize() {
		return percentOriginalSize;
	}
	
	public void setPercentOriginalSize(double percent) {
		percentOriginalSize = percent;
	}	
}
