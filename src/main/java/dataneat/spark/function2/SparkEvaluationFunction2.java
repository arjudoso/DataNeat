package main.java.dataneat.spark.function2;

import org.apache.spark.api.java.function.Function2;
import org.deeplearning4j.eval.Evaluation;

public class SparkEvaluationFunction2 implements Function2<Evaluation, Evaluation, Evaluation> {

	private static final long serialVersionUID = 1L;

	@Override
	public Evaluation call(Evaluation eval1, Evaluation eval2) throws Exception {		
		eval1.merge(eval2);
		return eval1;
	}

}
