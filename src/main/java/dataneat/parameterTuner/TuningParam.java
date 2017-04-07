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

import java.util.ArrayList;
import java.util.List;

import main.java.dataneat.base.BaseNeat;
import main.java.dataneat.utils.PropertiesHolder;
import main.java.dataneat.utils.RandGen;

public class TuningParam extends BaseNeat {

	double max = 0.0, min = 0.0;
	Double current = 0.0;
	List<Double> previousValues = new ArrayList<Double>();
	boolean isInt = false;
	String propCode = "";

	public TuningParam(double max, double min, boolean isInt, String propCode, PropertiesHolder p) {
		super(p);
		this.max = max;
		this.min = min;
		this.isInt = isInt;
		this.propCode = propCode;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public List<Double> getPreviousValues() {
		return previousValues;
	}

	public void setPreviousValues(List<Double> previousValues) {
		this.previousValues = previousValues;
	}

	public void setCurrent(Double current) {
		this.current = current;
	}

	public double getCurrent() {
		return current;
	}

	private double generateNew() {

		double range = max - min;

		double toAdd = range * RandGen.rand.nextDouble();

		return min + toAdd;
	}

	public void update() {
		previousValues.add(current);
		current = generateNew();
		
		if (isInt) {
			getParams().setProperty(propCode, Integer.toString(current.intValue()));
		}else {
			getParams().setProperty(propCode, current.toString());
		}
		
	}

	public void setPropCode(String code) {
		propCode = code;
	}

	public String getPropCode() {
		return propCode;
	}

	@Override
	public String toString() {
		if (isInt) {
			return (propCode + " = " + (int) getCurrent());
		} else {
			return (propCode + " = " + getCurrent());
		}
	}	
	
	public boolean getIsInt() {
		return isInt;
	}

}
