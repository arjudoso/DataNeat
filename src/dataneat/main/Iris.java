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
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.storage.StorageLevel;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.writable.Writable;
import org.datavec.spark.transform.Normalization;
import org.datavec.spark.transform.misc.StringToWritablesFunction;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.spark.datavec.DataVecDataSetFunction;
import org.deeplearning4j.spark.datavec.RecordReaderFunction;
import org.deeplearning4j.spark.util.SparkUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.TestDataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dataneat.engine.Engine;
import dataneat.parameterTuner.Tuner;
import dataneat.spark.SparkEngine;
import dataneat.utils.PropertiesHolder;
import dataneat.utils.RandGen;

public class Iris {

	private static Logger log = LoggerFactory.getLogger(Iris.class);

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
		recordReader.initialize(new FileSplit(new File("datasets/iris.txt")));

		// Second: the RecordReaderDataSetIterator handles conversion to DataSet
		// objects, ready for use in neural network
		int labelIndex = 4;
		int numOuts = 3;
		int batchSize = 150;
		int numInputs = 4;
		int epochs = 1500;

		DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, numOuts);		
		
		
		DataSet allData = iterator.next();		
        allData.shuffle();
        SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65);  //Use 65% of data for training

        DataSet trainingData = testAndTrain.getTrain();
        DataSet testData = testAndTrain.getTest();

        //We need to normalize our data. We'll use NormalizeStandardize (which gives us mean 0, unit variance):
        DataNormalization normalizer = new NormalizerStandardize();
        normalizer.fit(trainingData);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
        normalizer.transform(trainingData);     //Apply normalization to the training data
        normalizer.transform(testData);         //Apply normalization to the test data. This is using statistics calculated from the *training* set
		
		DataSetIterator iterator2 = new TestDataSetIterator((org.nd4j.linalg.dataset.DataSet) trainingData, batchSize);
		
		/*
		DataNormalization normalizer = new NormalizerStandardize();
		normalizer.fit(iterator);
		iterator.setPreProcessor(normalizer);
		*/
		
		Engine engine = new Engine(p);
		engine.autoConfig(numInputs, numOuts, batchSize);

		engine.runMulti(iterator2, testData, epochs);

		engine.displayBestTrainingNetwork();
		engine.getRunData().toCSV();

		System.out.println("Evaluate model....");
		Evaluation eval = new Evaluation(numOuts); 
		//DataSet data = iterator.next();
		INDArray output = engine.generatePredictions(testData.getFeatures(), "test");
		eval.eval(testData.getLabels(), output);
		System.out.println(eval.stats());
		System.out.println("****************Example finished********************");
		
	}
	
	private static void sparkBuildAndRun() throws FileNotFoundException, IOException, InterruptedException {
		// configure parameters
		System.out.println(("loading properties"));
		PropertiesHolder p = new PropertiesHolder();
		p.load();
		
		//build spark context
		SparkConf conf = new SparkConf().setAppName("DataNeat Iris");
		JavaSparkContext sc = new JavaSparkContext(conf);		

		//define some variables
		int numLinesToSkip = 0;
		String delimiter = ",";	
		int labelIndex = 4;
		int numOuts = 3;
		int batchSize = 10;
		int numInputs = 4;
		int epochs = 1500;
		int numPartitions = 10;
		double[] splits = {0.8,0.2};
		
		//build schema
		Schema schema = new Schema.Builder()
	            .addColumnsDouble("Sepal length", "Sepal width", "Petal length", "Petal width")
	            .addColumnInteger("Species")
	            .build();
		
		//read data in from file
		System.out.println("reading data");
		JavaRDD<String> stringData = sc.textFile("datasets/iris.txt");
		
		//We first need to parse this comma-delimited (CSV) format; we can do this using CSVRecordReader:
        RecordReader rr = new CSVRecordReader();
        JavaRDD<List<Writable>> parsedInputData = stringData.map(new StringToWritablesFunction(rr));
        
        //normalize data
        JavaRDD<List<Writable>> normalizedData = Normalization.normalize(schema, parsedInputData);
        
        //convert to Datasets        
        DataVecDataSetFunction readerFunction = new DataVecDataSetFunction(labelIndex, numOuts, false);       
		JavaRDD<org.nd4j.linalg.dataset.DataSet> allData = normalizedData.map(readerFunction);
		
		//shuffle data & repartition
		JavaRDD<org.nd4j.linalg.dataset.DataSet> shuffled = SparkUtils.shuffleExamples(allData, batchSize, numPartitions);
		
		//split into test and train
		JavaRDD<org.nd4j.linalg.dataset.DataSet>[] splitData = shuffled.randomSplit(splits, RandGen.getSeed());
		JavaRDD<org.nd4j.linalg.dataset.DataSet> train = splitData[0];
		JavaRDD<org.nd4j.linalg.dataset.DataSet> test = splitData[1];    		
		
		SparkEngine engine = new SparkEngine(p);
		engine.autoConfig(numInputs, numOuts, batchSize);

		engine.runMulti(train, test, epochs);

		engine.displayBestTrainingNetwork();
		engine.getRunData().toCSV();

		System.out.println("Evaluate model....");
		Evaluation eval = engine.combinedEval(test, "test");
		System.out.println(eval.stats());
		System.out.println("****************Example finished********************");		
	}

	private static void runTuner() throws IOException, InterruptedException {
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
		recordReader.initialize(new FileSplit(new File("datasets/iris.txt")));

		// Second: the RecordReaderDataSetIterator handles conversion to DataSet
		// objects, ready for use in neural network
		int labelIndex = 4;
		int numOuts = 3;
		int batchSize = 150;
		int numInputs = 4;
		int epochs = 200;
		int tunerRounds = 25;

		DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, numOuts);
		DataNormalization normalizer = new NormalizerStandardize();
		normalizer.fit(iterator);
		iterator.setPreProcessor(normalizer);
		Engine engine = new Engine(p);
		engine.autoConfig(numInputs, numOuts, batchSize).runMulti(iterator, epochs);
		Tuner tuner = new Tuner(p);

		for (int i = 0; i < tunerRounds; i++) {
			tuner.runTuner(engine, iterator, epochs);
			//engine.displayBestTrainingNetwork();
			engine.tuningReset();
		}
		tuner.writeTuneData();
	}

}
