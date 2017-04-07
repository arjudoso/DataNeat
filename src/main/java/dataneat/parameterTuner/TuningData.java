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
package main.java.dataneat.parameterTuner;

import java.util.List;

import main.java.dataneat.base.BaseNeat;
import main.java.dataneat.utils.IO;
import main.java.dataneat.utils.PropertiesHolder;

public class TuningData extends BaseNeat {
	private static final String FILE = "tunerDataFile";

	private String file;
	private StringBuilder sb = new StringBuilder();
	private boolean first = true;
	private static String newline = System.getProperty("line.separator");

	public TuningData(PropertiesHolder p) {
		super(p);
		file = getParams().getProperty(FILE);
	}

	public void addRound(int round, List<TuningParam> tuneParams, double fitness, double testFitness) {
		if (first) {
			sb.append("round,");
			for (TuningParam param : tuneParams) {
				sb.append(param.getPropCode() + ",");
			}
			sb.append("fitness,");
			sb.append("testFitness");
			sb.append(newline);
			first = false;
		}

		sb.append(round+",");
		for (TuningParam param : tuneParams) {
			if (param.getIsInt()) {
				sb.append((int) param.getCurrent() + ",");
			} else {
				sb.append(param.getCurrent() + ",");
			}
		}
		sb.append(fitness+",");
		sb.append(testFitness);
		sb.append(newline);
	}

	public void toCSV() {
		IO.addToFile(sb.toString(), file);
	}

}
