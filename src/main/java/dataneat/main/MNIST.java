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
package dataneat.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import dataneat.engine.Engine;
import dataneat.parameterTuner.Tuner;
import dataneat.utils.PropertiesHolder;

public class MNIST {

	// private static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		 buildAndRun();
		//runTuner();
	}

	private static void buildAndRun() throws FileNotFoundException, IOException, InterruptedException {
		// configure parameters

		System.out.println(("loading properties"));
		PropertiesHolder p = new PropertiesHolder();
		p.load();

		// First: get the dataset using the record reader. CSVRecordReader
		// handles loading/parsing
		System.out.println("reading csv");
		int numLinesToSkip = 0;
		String delimiter = ",";
		RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
		recordReader.initialize(new FileSplit(new File("datasets/mnist.csv")));

		// Second: the RecordReaderDataSetIterator handles conversion to DataSet
		// objects, ready for use in neural network
		int labelIndex = 784; // 3 values in each row of the xor CSV: 2 input
							// features followed by an integer label (class)
							// index.
		int numOuts = 10;
		int batchSize = 1000;
		int numInputs = 784;
		int epochs = 1000;

		DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize,labelIndex,numOuts);

		Engine engine = new Engine(p);
		engine.autoConfig(numInputs, numOuts, batchSize);

		engine.runMulti(iterator, epochs);

		engine.displayBestTrainingNetwork();
		engine.getRunData().toCSV();
		System.out.println(engine.generatePredictions(iterator.next().getFeatures(), "train").toString());
	}

	private static void runTuner() throws IOException, InterruptedException {
		// configure parameters

		System.out.println(("loading properties"));
		PropertiesHolder p = new PropertiesHolder();
		p.load();

		// First: get the dataset using the record reader. CSVRecordReader
		// handles loading/parsing
		System.out.println("reading csv");
		int numLinesToSkip = 1;
		String delimiter = ",";
		RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
		recordReader.initialize(new FileSplit(new File("datasets/xor.csv")));

		// Second: the RecordReaderDataSetIterator handles conversion to DataSet
		// objects, ready for use in neural network
		int labelIndex = 2; // 3 values in each row of the xor CSV: 2 input
							// features followed by an integer label (class)
							// index.
		int numOuts = 1;
		int batchSize = 4;
		int numInputs = 2;
		int epochs = 100;
		int tunerRounds = 10;

		DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, labelIndex,
				true);
		Engine engine = new Engine(p);
		engine.autoConfig(numInputs, numOuts, batchSize).runMulti(iterator, epochs);
		Tuner tuner = new Tuner(p);

		for (int i = 0; i < tunerRounds; i++) {
			tuner.runTuner(engine, iterator, epochs);
			engine.displayBestTrainingNetwork();
			engine.tuningReset();
		}
	}

}
