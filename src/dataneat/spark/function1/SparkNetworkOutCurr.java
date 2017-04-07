package dataneat.spark.function1;

import org.apache.spark.api.java.function.Function;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import dataneat.base.BaseNeat;
import dataneat.genome.NeatChromosome;
import dataneat.phenotype.Network;
import dataneat.utils.PropertiesHolder;

public class SparkNetworkOutCurr extends BaseNeat implements Function<DataSet, INDArray> {
	private static final String STABIL_THRESH = "stabilizationDelta";
	
	private NeatChromosome chrom = null;
	private INDArray stabil = null;
	private double stabilDelta = 0.01;
	
	public SparkNetworkOutCurr(PropertiesHolder p) {
		super(p);	
		stabilDelta = Double.parseDouble(getParams().getProperty(STABIL_THRESH));
	}

	private static final long serialVersionUID = 1L;

	@Override
	public INDArray call(DataSet data) throws Exception {
		stabil = Nd4j.zeros(data.numExamples(), 1);
		stabil.addi(stabilDelta);
		Network net = new Network(chrom, getHolder(), stabil);
		net.computeNetCurrentTimestep(data.getFeatures());
		return net.getOutput();
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
