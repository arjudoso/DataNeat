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
package dataneat.parameterTuner;

import java.util.ArrayList;
import java.util.List;

import dataneat.base.BaseNeat;
import dataneat.engine.Engine;
import dataneat.utils.IO;
import dataneat.utils.PropertiesHolder;

public class Tuner extends BaseNeat {
	private static final String TUNER_LIMIT = "parameterTunerRounds";
	private static final String TUNER_TYPE = "tunerType";
	private static final String THRESH_MAX = "sMax";
	private static final String THRESH_MIN = "sMin";
	private static final String TOURN_MAX = "tMax";
	private static final String TOURN_MIN = "tMin";
	private static final String SL_MAX = "slMax";
	private static final String SL_MIN = "slMin";
	private static final String DROP_MAX = "dropMax";
	private static final String DROP_MIN = "dropMin";
	private static final String ST_MIN = "stMin";
	private static final String ST_MAX = "stMax";
	private static final String MP_MIN = "mpMin";
	private static final String MP_MAX = "mpMax";
	private static final String WMR_MIN = "wmrMin";
	private static final String WMR_MAX = "wmrMax";
	private static final String ALR_MAX = "alrMax";
	private static final String ALR_MIN = "alrMin";
	private static final String ANR_MAX = "anrMax";
	private static final String ANR_MIN = "anrMin";
	private static final String SPLIT = "splitPercent";

	private int round = 0, roundLimit = 0, type = 0;
	private Updater updater;
	private List<TuningParam> tuneParams = new ArrayList<TuningParam>();
	private static String newline = System.getProperty("line.separator");
	private TuningData tuneData;
	double splitPercent = -1.0;

	public Tuner(PropertiesHolder p) {
		super(p);
		tuneData = new TuningData(p);
		roundLimit = Integer.parseInt(getParams().getProperty(TUNER_LIMIT));
		type = Integer.parseInt(getParams().getProperty(TUNER_TYPE));
		splitPercent = Double.parseDouble(getParams().getProperty(SPLIT));
		initializeUpdater();
		initTuningParams();
	}

	private void initTuningParams() {
		TuningParam thresh = new TuningParam(Double.parseDouble(getParams().getProperty(THRESH_MAX)),
				Double.parseDouble(getParams().getProperty(THRESH_MIN)), false, "survivalThresh");

		TuningParam tSize = new TuningParam(Double.parseDouble(getParams().getProperty(TOURN_MAX)),
				Double.parseDouble(getParams().getProperty(TOURN_MIN)), true, "tournamentSize");

		TuningParam speciesLimit = new TuningParam(Double.parseDouble(getParams().getProperty(SL_MAX)),
				Double.parseDouble(getParams().getProperty(SL_MIN)), true, "speciesLimit");

		TuningParam dropAge = new TuningParam(Double.parseDouble(getParams().getProperty(DROP_MAX)),
				Double.parseDouble(getParams().getProperty(DROP_MIN)), true, "speciesDropAge");

		TuningParam speciesThresh = new TuningParam(Double.parseDouble(getParams().getProperty(ST_MAX)),
				Double.parseDouble(getParams().getProperty(ST_MIN)), false, "speciesThreshold");

		TuningParam mutationPower = new TuningParam(Double.parseDouble(getParams().getProperty(MP_MAX)),
				Double.parseDouble(getParams().getProperty(MP_MIN)), false, "mutationPower");

		TuningParam weightRate = new TuningParam(Double.parseDouble(getParams().getProperty(WMR_MAX)),
				Double.parseDouble(getParams().getProperty(WMR_MIN)), false, "weightMutationRate");

		TuningParam addLink = new TuningParam(Double.parseDouble(getParams().getProperty(ALR_MAX)),
				Double.parseDouble(getParams().getProperty(ALR_MIN)), false, "addLinkRate");

		TuningParam addNode = new TuningParam(Double.parseDouble(getParams().getProperty(ANR_MAX)),
				Double.parseDouble(getParams().getProperty(ANR_MIN)), false, "addNodeRate");

		thresh.setCurrent(Double.parseDouble(getParams().getProperty("survivalThresh")));
		tSize.setCurrent(Double.parseDouble(getParams().getProperty("tournamentSize")));
		speciesLimit.setCurrent(Double.parseDouble(getParams().getProperty("speciesLimit")));
		dropAge.setCurrent(Double.parseDouble(getParams().getProperty("speciesDropAge")));
		speciesThresh.setCurrent(Double.parseDouble(getParams().getProperty("speciesThreshold")));
		mutationPower.setCurrent(Double.parseDouble(getParams().getProperty("mutationPower")));
		weightRate.setCurrent(Double.parseDouble(getParams().getProperty("weightMutationRate")));
		addLink.setCurrent(Double.parseDouble(getParams().getProperty("addLinkRate")));
		addNode.setCurrent(Double.parseDouble(getParams().getProperty("addNodeRate")));

		tuneParams.add(tSize);
		tuneParams.add(thresh);
		tuneParams.add(speciesLimit);
		tuneParams.add(dropAge);
		tuneParams.add(speciesThresh);
		tuneParams.add(mutationPower);
		tuneParams.add(weightRate);
		tuneParams.add(addLink);
		tuneParams.add(addNode);
	}

	public void runTuner(Engine engine) {
		// the tuner is going to run multiple rounds of evolution, attempting to
		// optimize various parameters, it will use whatever round limit is set
		// in the parameter file

		engine.autoConfig();

		while (round < roundLimit) {
			System.out.println();
			System.out.println("Tuning run: " + round);
			System.out.println();
			engine.run();
			updateData(engine, round);

			updater.update(tuneParams);
			writeParams();
			engine.tuningReconfig();
			round++;
		}

		tuneData.toCSV();

	}

	private void updateData(Engine engine, int round) {
		tuneData.addRound(round, tuneParams, engine.getPop().getTrainingBest().getFitness(),
				engine.getPop().getBestTestFitness());
	}

	private void initializeUpdater() {
		switch (type) {
		case 0:
			updater = new RandomUpdater();
			break;
		}
	}

	private void writeParams() {
		String appFile = "src/appProp.properties";

		StringBuilder sb = new StringBuilder();

		for (TuningParam p : tuneParams) {
			sb.append(newline);
			sb.append(p.toString());
		}

		IO.addToFile(sb.toString(), appFile);
		System.out.println("Updated Params");
		System.out.println(sb.toString());
	}
}
