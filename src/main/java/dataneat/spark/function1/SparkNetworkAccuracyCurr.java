package main.java.dataneat.spark.function1;

import org.apache.spark.api.java.function.Function;
import org.deeplearning4j.eval.Evaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import main.java.dataneat.base.BaseNeat;
import main.java.dataneat.genome.NeatChromosome;
import main.java.dataneat.phenotype.Network;
import main.java.dataneat.utils.PropertiesHolder;

public class SparkNetworkAccuracyCurr extends BaseNeat implements Function<DataSet, Evaluation> {
	private static final String STABIL_THRESH = "stabilizationDelta";
	
	private NeatChromosome chrom = null;
	private INDArray stabil = null;
	private double stabilDelta = 0.01;
	
	public SparkNetworkAccuracyCurr(PropertiesHolder p) {
		super(p);	
		stabilDelta = Double.parseDouble(getParams().getProperty(STABIL_THRESH));
	}

	private static final long serialVersionUID = 1L;

	@Override
	public Evaluation call(DataSet data) throws Exception {
		stabil = Nd4j.zeros(data.numExamples(), 1);
		stabil.addi(stabilDelta);
		Network net = new Network(chrom, getHolder(), stabil);
		net.computeNetCurrentTimestep(data.getFeatures());
		Evaluation eval = new Evaluation(data.getLabels().columns());
		eval.eval(net.getOutput(), data.getLabels());
		return eval;
	}

	public NeatChromosome getChrom() {
		return chrom;
	}

	public void setChrom(NeatChromosome chrom) {
		this.chrom = chrom;
	}

	public INDArray getStabil() {
		return stabil;
	}

	public void setStabil(INDArray stabil) {
		this.stabil = stabil;
	}	
}
