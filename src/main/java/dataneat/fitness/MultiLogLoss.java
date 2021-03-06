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
package dataneat.fitness;

import java.io.Serializable;

import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.activations.impl.ActivationSoftmax;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.lossfunctions.ILossFunction;
import org.nd4j.linalg.lossfunctions.impl.LossMCXENT;

public class MultiLogLoss implements TargetFitnessFunction, Serializable {

	private static final long serialVersionUID = 1L;
	
	public MultiLogLoss(){}

	@Override
	public double computeFitness(INDArray labels, INDArray outputs) {				
		//INDArray z = Nd4j.getExecutioner().execAndReturn(new SoftMax(outputs));		
		ILossFunction func = new LossMCXENT();
		IActivation act = new ActivationSoftmax();
		return func.computeScore(labels, outputs,act , null, true);		
	}
}
