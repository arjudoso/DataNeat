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

import java.io.IOException;
import java.util.Scanner;

import dataneat.engine.Engine;
import dataneat.parameterTuner.Tuner;
import dataneat.utils.PropertiesHolder;

public class Main {
	private static boolean done = false;

	public static void main(String[] args) throws IOException, InterruptedException {

		Scanner sc = new Scanner(System.in);

		while (!done) {
			printMenu();
			Integer selection = sc.nextInt();
			executeSelection(selection);
		}
		sc.close();
	}

	private static void printMenu() {
		System.out.println();
		System.out.println("Main Menu");
		System.out.println("0: Exit");
		System.out.println("1: Build and run evolution from parameter files");
		System.out.println("2: Run parameter tuner");
	}

	private static void executeSelection(Integer selection) {

		switch (selection) {
		case 0:
			done = true;
			break;
		case 1:
			buildAndRun();
			break;
		case 2:
			runTuner();
			break;
		default:
			break;
		}
	}

	private static void buildAndRun() {
		// configure parameters
		PropertiesHolder p = new PropertiesHolder();
		p.load();

		// build engine & run it
		Engine engine = new Engine(p);
		engine.autoConfig();
		engine.run();
		engine.getRunData().toCSV();
		engine.generatePredictions();
		engine.printChrom();
	}

	private static void runTuner() {
		// configure parameters
		PropertiesHolder p = new PropertiesHolder();
		p.load();
		Engine engine = new Engine(p);
		
		Tuner tuner = new Tuner(p);
		tuner.runTuner(engine);
	}

}
