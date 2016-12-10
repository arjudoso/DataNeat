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
import java.util.List;

import dataneat.base.BaseNeat;
import dataneat.utils.DataManipChecker;
import dataneat.utils.PropertiesHolder;

public class SplitDatasetManager extends BaseNeat {
	//this class is not fully general, it assumes you are splitting the data into 2 subsets: training and testing
	//TODO generalize for more than 2 splits
	
	
	private static final String SPLIT = "splitPercent";
	private static final String RANDOMIZE = "randomizeBeforeSplit";
	private static final String NORMALIZE = "normalize";
	private static final String MANIP_TEST = "dataManipTest";
	private static final String REDUCTION_TRAIN = "percentOriginalSizeTrain";
	private static final String REDUCTION_TEST = "percentOriginalSizeTest";

	TargetDataset trainingBase;
	double splitPercent = 0.0;
	List<TargetDataset> subsets = new ArrayList<TargetDataset>();
	boolean randomizeBeforeSplit = true;
	private Integer normalize = 0, dataManipTest = 0;
	private double percentOriginalSizeTrain = 1.0, percentOriginalSizeTest = 1.0;	

	public SplitDatasetManager(PropertiesHolder p) {
		super(p);
		splitPercent = Double.parseDouble(getParams().getProperty(SPLIT));
		randomizeBeforeSplit = Boolean.parseBoolean(getParams().getProperty(RANDOMIZE));
		normalize = Integer.parseInt(getParams().getProperty(NORMALIZE));
		dataManipTest = Integer.parseInt(getParams().getProperty(MANIP_TEST));
		percentOriginalSizeTrain  = Double.parseDouble(getParams().getProperty(REDUCTION_TRAIN));
		percentOriginalSizeTest = Double.parseDouble(getParams().getProperty(REDUCTION_TEST));
	}

	public void addTrainingDataFromFile(String file, int numColTotal, int numOutputs, boolean header) {

		// build the dataset from the input file

		try {
			trainingBase = new TargetDataset(getHolder(), file, numColTotal, numOutputs, header);	
			manip();
			splitData();
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	private void manip() {
		checkNormalize();			
	}	
	
	private void normalizeInput() {
		trainingBase.normalizeInput();
	}
	
	private void checkNormalize() {
		switch (normalize) {
		case 0:
			break;
		case 1:
			normalizeInput();
			break;
		default:
			break;
		}
	}

	private void splitData() throws IOException {		

		if (splitPercent < 0.0 || splitPercent > 1.0) {
			// no split has been requested
			subsets.add(trainingBase);
			return;
		}

		trainingBase.splitData(splitPercent);

		if (randomizeBeforeSplit) {
			trainingBase.shuffleRows();
		}

		for (TargetDataset td : trainingBase.createSubsets()) {
			subsets.add(td);
		}
		
		subsets.get(0).setPercentOriginalSize(percentOriginalSizeTrain);
		subsets.get(1).setPercentOriginalSize(percentOriginalSizeTest);
		
		adjustTestManipulation();
	}

	private void adjustTestManipulation() {
		// targetdatasets inherit global data manipulation settings. We have
		// created a test dataset, which needs to have its data manipulation
		// settings adjusted independently
		
		subsets.get(1).setManipulation(DataManipChecker.checkDataManip(dataManipTest));
	}

	public TargetDataset getTrainingData() {
		// will return the base dataset if no split requested
		return subsets.get(0);
	}

	public TargetDataset getTestingData() {
		return subsets.get(1);
	}

	public int numInputColumns() {
		return trainingBase.numInputColumns();
	}

	public int numIdealColumns() {
		return trainingBase.numIdealColumns();
	}

	public void shuffleTrainingRows() {
		getTrainingData().shuffleRows();
	}

	public void bootstrapTraining() {
		getTrainingData().bootStrap();
	}

	public void shuffleTestingRows() {
		getTestingData().shuffleRows();
	}

	public void bootstrapTesting() {
		getTestingData().bootStrap();
	}

}
