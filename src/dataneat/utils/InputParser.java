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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import dataneat.data.TableData;

public class InputParser {

		// general class for parsing text data, reads from a csv file and builds
		// input and ideal tableData		

		public static ArrayList<TableData> parse(boolean header, String regex, String inputFile, int numColTotal, int numOutputs) {

			TableData input = new TableData(numColTotal - numOutputs);
			TableData ideal = new TableData(numOutputs);
			
			try (BufferedReader inputStream = new BufferedReader(new FileReader(inputFile))) {

				String l;
				boolean isFirst = true;

				while ((l = inputStream.readLine()) != null) {

					if (isFirst && header) {
						isFirst = false;
						continue;
					}

					String[] line = l.split(regex);

					ArrayList<Double> inputRow = new ArrayList<Double>();
					ArrayList<Double> idealRow = new ArrayList<Double>();

					for (int i = 0; i < line.length; i++) {

						if (i >= (numColTotal - numOutputs)) {
							// finished reading inputs in this row
							idealRow.add(Double.parseDouble(line[i]));
						} else {
							// still reading in inputs
							inputRow.add(Double.parseDouble(line[i]));
						}
					}
					input.addRow(inputRow);
					ideal.addRow(idealRow);
				}

			} catch (IOException e) {				
				e.printStackTrace();
			}
			
			ArrayList<TableData> data = new ArrayList<TableData>();
			data.add(input);
			data.add(ideal);
			return data;
		}
	}

