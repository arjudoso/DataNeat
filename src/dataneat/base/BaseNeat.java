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
package dataneat.base;

import java.util.Properties;

import dataneat.utils.PropertiesHolder;

public abstract class BaseNeat {

	// defines some basic functionality to retrieve Neat parameters from a properties object

	private PropertiesHolder holder;

	public BaseNeat(PropertiesHolder p) {

		setHolder(p);
	}

	public Properties getParams() {
		return holder.getAppProps();
	}

	public PropertiesHolder getHolder() {
		return holder;
	}

	public void setHolder(PropertiesHolder param) {
		this.holder = param;
	}
}
