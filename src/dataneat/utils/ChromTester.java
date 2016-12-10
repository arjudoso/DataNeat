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
package dataneat.utils;

import java.io.IOException;

import dataneat.base.BaseNeat;
import dataneat.data.TableData;
import dataneat.data.TargetDataset;
import dataneat.genome.LinkGene;
import dataneat.genome.NeatChromosome;
import dataneat.genome.NeuronGene;
import dataneat.phenotype.Network;

public class ChromTester extends BaseNeat {

	private static String newline = System.getProperty("line.separator");

	private String file = "networkOutput.txt";
	private String fileTest = "networkTestingOutput.txt";

	public ChromTester(PropertiesHolder p) {
		super(p);
	}

	public void test(NeatChromosome chrom, int numOuts, TableData input) {

		System.out.println(
				"Id " + chrom.getId() + " Fitness: " + chrom.getFitness() + " Test: " + chrom.getTestFitness());

		// create phenotype
		Network net = new Network(chrom, getHolder());

		// create a data object for the network's outputs
		TableData outputs = new TableData(numOuts);

		// compute network output for each input row
		for (int i = 0; i < input.numRows(); i++) {
			net.computeNet(input.getRow(i));
			try {
				outputs.addRow(net.getOutput());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// print out data about the chromosome

		StringBuilder sb = new StringBuilder();
		sb.append("Out");

		for (int i = 0; i < outputs.numRows(); i++) {
			sb.append(newline);
			sb.append(outputs.getRow(i));
		}

		IO.addToFile(sb.toString(), fileTest);
	}

	public void test(NeatChromosome chrom, TargetDataset data) {

		int rows = data.numRowsReduced();

		System.out.println(
				"Id " + chrom.getId() + " Fitness: " + chrom.getFitness() + " Test: " + chrom.getTestFitness());

		// create phenotype
		Network net = new Network(chrom, getHolder());

		// create a data object for the network's outputs
		TableData outputs = new TableData(data.numIdealColumns());

		// compute network output for each input row
		for (int i = 0; i < rows; i++) {
			net.computeNet(data.getInputRow(i));
			try {
				outputs.addRow(net.getOutput());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// print out data about the chromosome

		StringBuilder sb = new StringBuilder();
		sb.append("Ideal, Out");

		for (int i = 0; i < data.numRowsReduced(); i++) {
			sb.append(newline);
			sb.append(data.getIdealRow(i) + "," + outputs.getRow(i));
		}

		IO.addToFile(sb.toString(), file);

		for (LinkGene l : chrom.getLinks()) {
			System.out.println("From: " + l.getFromNeuronID() + " To: " + l.getToNeuronID() + " Ena: " + l.isEnabled()
					+ " Wei: " + l.getWeight() + " Id: " + l.getInnovationID());
		}

		for (NeuronGene n : chrom.getNeurons().getNeuronList()) {
			System.out.println("Id " + n.getID() + " Type" + n.getNeuronType() + " Ins: " + n.getInputs());
		}
	}
}
