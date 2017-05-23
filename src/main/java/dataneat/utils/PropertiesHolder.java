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
import java.io.Serializable;
import java.util.Properties;

public class PropertiesHolder implements Serializable {

	private static final long serialVersionUID = 1L;

	private String defaultFile = "/home/ricardo_fr_rivero/dn/defaultProp.properties", appFile = "/home/ricardo_fr_rivero/dn/appProp.properties";

	private Properties defaultProps = new Properties(), appProps;

	public PropertiesHolder() {}
	
	public PropertiesHolder(String df, String af) {
		defaultFile = df;
		appFile = af;
	}

	public void load() {

		// create and load default properties

		try (BufferedReader br = new BufferedReader(new FileReader(defaultFile))) {

			defaultProps.load(br);			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}

		appProps = new Properties(defaultProps);

		if (appFile != null) {

			try (BufferedReader in = new BufferedReader(new FileReader(appFile))) {

				appProps.load(in);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getDefaultFile() {
		return defaultFile;
	}

	public void setDefaultFile(String defaultFile) {
		this.defaultFile = defaultFile;
	}

	public String getAppFile() {
		return appFile;
	}

	public void setAppFile(String appFile) {
		this.appFile = appFile;
	}

	public Properties getDefaultProps() {
		return defaultProps;
	}

	public void setDefaultProps(Properties defaultProps) {
		this.defaultProps = defaultProps;
	}

	public Properties getAppProps() {
		return appProps;
	}

	public void setAppProps(Properties appProps) {
		this.appProps = appProps;
	}

}
