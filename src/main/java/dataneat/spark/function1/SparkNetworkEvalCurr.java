package main.java.dataneat.spark.function1;

import org.apache.spark.api.java.function.Function;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import main.java.dataneat.base.BaseNeat;
import main.java.dataneat.fitness.TargetFitnessFunction;
import main.java.dataneat.genome.NeatChromosome;
import main.java.dataneat.phenotype.Network;
import main.java.dataneat.utils.PropertiesHolder;

public class SparkNetworkEvalCurr extends BaseNeat implements Function<DataSet, Double> {
	private static final String STABIL_THRESH = "stabilizationDelta";
	
	private double stabilDelta = 0.01;
	private NeatChromosome chrom = null;
	private INDArray stabil = null;
	private TargetFitnessFunction fitnessFunction;
	
	public SparkNetworkEvalCurr(PropertiesHolder p) {
		super(p);		
		stabilDelta = Double.parseDouble(getParams().getProperty(STABIL_THRESH));
	}

	private static final long serialVersionUID = 1L;

	@Override
	public Double call(DataSet data) throws Exception {
		stabil = Nd4j.zeros(data.numExamples(), 1);
		stabil.addi(stabilDelta);
		Network net = new Network(chrom, getHolder(), stabil);
		net.computeNetCurrentTimestep(data.getFeatures());
		return fitnessFunction.computeFitness(data.getLabels(), net.getOutput());
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

	public TargetFitnessFunction getFitnessFunction() {
		return fitnessFunction;
	}

	public void setFitnessFunction(TargetFitnessFunction fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}	
}
