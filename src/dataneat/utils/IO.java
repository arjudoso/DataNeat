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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class IO {

	public static void addToFile(String stringToAdd, String file) {

		PrintWriter outputStream = null;

		try {
			outputStream = new PrintWriter(new FileWriter(file, false));
			outputStream.println(stringToAdd);
		} catch (IOException e) {

			e.printStackTrace();
		} finally {

			if (outputStream != null) {
				outputStream.close();
			}
		}

	}
}
