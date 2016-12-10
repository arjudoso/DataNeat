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

import dataneat.data.DataManipulation;

public class DataManipChecker {

	public static DataManipulation checkDataManip(Integer dataManip) {
		DataManipulation manipulation;
		
		switch (dataManip) {
		case 0:
			manipulation = DataManipulation.NONE;
			break;
		case 1:
			manipulation = DataManipulation.RANDOMIZE;
			break;
		case 2:
			manipulation = DataManipulation.BOOTSTRAP;
			break;
		case 3:
			manipulation = DataManipulation.CONTINUOUS_RANDOMIZE;
			break;
		default:
			manipulation = DataManipulation.NONE;
			break;
		}
		
		return manipulation;
	}
}
