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
package dataneat.genome;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import dataneat.base.BaseNeat;
import dataneat.innovation.InnovationDatabase;
import dataneat.utils.PropertiesHolder;
import dataneat.utils.RandGen;

public class NeatChromosome extends BaseNeat implements Comparable<NeatChromosome> {
	private static final String MAXIMIZE = "maximize";
	private static int chromCounter = 0;

	private int species = 0;
	private boolean isSolution = false, maximize = false;

	private double fitness = 0.0, adjustedFitness = 0.0, testFitness = 0.0;
	private int id = -1;

	// neurons & links in this chromosome

	private NeuronDB neurons;
	private List<LinkGene> links = new ArrayList<LinkGene>();

	public NeatChromosome(PropertiesHolder p) {
		super(p);
		init();

		neurons = new NeuronDB();
	}

	public NeatChromosome(int inputs, int outputs, boolean connected, PropertiesHolder p) {
		super(p);
		init();
		// create a new chromosome with the given number of IO,
		// with random link weights
		neurons = new NeuronDB();

		// inputs & outputs get negative ids to differentiate them
		int neuronId = -1;

		for (int i = 0; i < inputs; i++) {
			NeuronGene in = new NeuronGene(neuronId, NeuronType.INPUT);
			addNeuron(in, NeuronType.INPUT);
			neuronId--;
		}

		for (int i = 0; i < outputs; i++) {
			NeuronGene out = new NeuronGene(neuronId, NeuronType.OUTPUT);
			addNeuron(out, NeuronType.OUTPUT);
			neuronId--;
		}

		// create a bias neuron
		NeuronGene bias = new NeuronGene(neuronId, NeuronType.BIAS);
		addNeuron(bias, NeuronType.BIAS);
		neuronId--;

		if (connected) {
			// connect inputs to outputs
			for (Integer inputId : neurons.getInputIds()) {
				for (Integer outputId : neurons.getOutputIds()) {
					connectNodes(inputId, outputId, 0, true);
				}
			}
		} else {
			// need at least 1 connection for NEAT to function

			// connect a random input to a random output
			NeuronGene in = neurons.randomInput();
			NeuronGene out = neurons.randomOutput();
			connectNodes(in.getID(), out.getID(), 0, true);
		}
	}

	public NeatChromosome(NeatChromosome parent) {
		super(parent.getHolder());
		init();

		this.species = parent.getSpecies();
		this.fitness = parent.getFitness();
		this.adjustedFitness = parent.getAdjustedFitness();

		// make copies of the parent's links
		for (LinkGene parentLink : parent.getLinks()) {
			LinkGene newLink = new LinkGene(parentLink);
			this.links.add(newLink);
		}

		this.neurons = new NeuronDB(parent.getNeurons());
	}

	/*
	 * tried to be clever, had issues
	 * 
	 * public boolean addLinkEligibleRecur(double mutationRate) {
	 * 
	 * if (checkConnections() == links.size()) { // simple check to avoid trying
	 * to add links to a network that is // fully connected return false; }
	 * 
	 * return (RandGen.rand.nextDouble() < mutationRate);
	 * 
	 * }
	 */

	public boolean checkEligible(double mutationRate) {

		return (RandGen.rand.nextDouble() < mutationRate);

	}

	private void assignID() {
		id = chromCounter;
		chromCounter++;
	}

	private void init() {
		assignID();
		maximize = Boolean.parseBoolean(getParams().getProperty(MAXIMIZE));
		initFitness();
	}

	private void initFitness() {
		if (!maximize) {
			this.adjustedFitness = Double.MAX_VALUE;
			this.fitness = Double.MAX_VALUE;
			this.testFitness = Double.MAX_VALUE;
		}
	}

	/*
	 * 
	 * private int checkConnections() { int possibleConnections = 0;
	 * 
	 * // current model is unrestrained connectivity // returns the number of
	 * possible connections in this network // including // loopbacks and bias
	 * connections // directed connections: n choose 2 -> (n*(n-1))/2 // with
	 * unrestrained recurrence: (n*(n-1))/2 * 2 = (n*n-n) // now add in "n"
	 * possible bias connections: n*n-n+n= n*n // add in "n" possible loopbacks:
	 * n*n+n
	 * 
	 * int size = (neurons.sizeWithBias() - 1); // size includes bias node
	 * possibleConnections = (size * size + size);
	 * 
	 * return possibleConnections; }
	 */

	public boolean mutateAddLink(int connectionAttemptLimit, ConnectivityType connectionType) {

		// just grabs 2 random nodes and tries to make a connection

		// create holders for the 2 nodes to be connected
		NeuronGene node1, node2;

		for (int i = 0; i < connectionAttemptLimit; i++) {

			node1 = neurons.randomNeuron();

			node2 = neurons.randomNeuron();

			if (connectionType == ConnectivityType.FORWARD) {

				if (node1 == node2) {
					continue;
				}

				// check to see if node 1 can make a valid connection to node 2
				if (!node2.checkInputInvalid(node1.getID(), node1.getSplitY(), node1.getNeuronType())) {
					// if node 1 is not an invalid input, connect the nodes and
					// break the loop
					connectNodes(node1, node2);

					return true;
				}
			} else {
				if (connectionType == ConnectivityType.RECURRENT) {
					// check to see if node 1 can make a valid connection to
					// node 2
					if (!node2.checkInputInvalidRecur(node1.getID())) {
						// if node 1 is not an invalid input, connect the nodes
						// and break the loop
						connectNodes(node1, node2);
						// System.out.println("success");
						return true;
					}
				}
			}

			// no valid connection formed if we get down here, repeat loop
		}

		return false;
	}

	private void connectNodes(int fromID, int toID, double weight, boolean randomize) {
		// create a new connection, set the two nodes, pass the connection
		// to the innovation database and have it return an Innovation ID,
		// add the connection to the chromosome

		neurons.getById(toID).addInput(fromID);

		LinkGene connection = new LinkGene();
		connection.setFromNeuronID(fromID);
		connection.setToNeuronID(toID);

		if (randomize) {
			connection.randomizeWeight();
		} else {
			connection.setWeight(weight);
		}

		if (neurons.getById(fromID).getNeuronType() == NeuronType.BIAS) {

			connection.setBias(true);
		}

		// deal with assigning the connections innovation id
		connection.setInnovationID(InnovationDatabase.getInnovationDatabase().evaluateGene(connection));

		links.add(connection);

		// deal with setting loopback flag
		if (fromID == toID) {
			neurons.getById(toID).setloopBack(true);
		}
	}

	private void connectNodes(NeuronGene node1, NeuronGene node2) {

		connectNodes(node1.getID(), node2.getID(), 0, true);
	}

	private void connectNodes(NeuronGene node1, NeuronGene node2, double weight) {

		connectNodes(node1.getID(), node2.getID(), weight, false);
	}

	private void calculateSplits(NeuronType type, int splitLinkId) {
		// determine the position of a neuron in the network. Useful
		// for visual rendering, and for determining if a connection is forward
		// or recurrent.
		NeuronGene neuron = neurons.getById(splitLinkId);

		switch (type) {
		case INPUT:
		case BIAS:
			neuron.setSplitY(0.0);// inputs are at the start
			break;
		case OUTPUT:
			neuron.setSplitY(1.0); // outputs at the end
			break;
		case HIDDEN:
			double start = 0.0, end = 0.0;
			for (LinkGene l : links) {
				if (l.getInnovationID() == splitLinkId) {
					start = neurons.getById(l.getFromNeuronID()).getSplitY();
					end = neurons.getById(l.getToNeuronID()).getSplitY();
				}
			}

			// hidden neurons are half way inbetween the input and outputs
			neuron.setSplitY(((end - start) / 2) + start);
		}
	}

	public void sortBySplitY() {
		neurons.sortBySplitY();
	}

	private void addNeuron(NeuronGene neuron, NeuronType type) {
		neurons.addNeuron(neuron, type);
		calculateSplits(type, neuron.getID());
	}

	public void mutateAddNode() {
		// System.out.println("addNode");
		// if called, assume this chromosome has been selected for node addition

		int splitLink = 0;

		// grab a random link
		do {
			splitLink = RandGen.rand.nextInt(links.size());
		} while (links.get(splitLink).isBias() || links.get(splitLink).isAlreadySplit());

		// create new node
		NeuronGene newNode = new NeuronGene(links.get(splitLink).getInnovationID(), NeuronType.HIDDEN);
		addNeuron(newNode, NeuronType.HIDDEN);

		// disable old link
		links.get(splitLink).setEnabled(false);
		links.get(splitLink).setAlreadySplit(true);

		// create the 2 new links
		int startNodeId = links.get(splitLink).getFromNeuronID();
		int endNodeId = links.get(splitLink).getToNeuronID();

		connectNodes(neurons.getById(startNodeId), newNode, 1.00);
		connectNodes(newNode, neurons.getById(endNodeId), links.get(splitLink).getWeight());

	}

	public void mutateLinkWeight(double power) {
		// if called, assume this chromosome has been selected for weight
		// mutation, mutate all the chromosomes links using the specified power

		for (LinkGene gene : links) {
			gene.perturbWeight(power);
		}
	}

	public Graph buildGraph() {
		String styleSheet = "node.HIDDEN {" + "	fill-color: black;" + "}" + "node.INPUT {" + "	fill-color: blue;" + "}"
				+ "node.OUTPUT {" + "	fill-color: red;" + "}" + "node.BIAS {" + "	fill-color: green;" + "}";

		Graph graph = new SingleGraph(Integer.toString(id));
		graph.addAttribute("ui.stylesheet", styleSheet);
		for (NeuronGene n : neurons.getNeuronList()) {
			graph.addNode(Integer.toString(n.getID())).addAttribute("ui.class", n.getNeuronType().name());
		}

		for (LinkGene l : links) {
			if (l.isEnabled()) {
				graph.addEdge(l.getInnovationID().toString(), Integer.toString(l.getFromNeuronID()),
						Integer.toString(l.getToNeuronID()), true);				
			}
		}

		return graph;
	}

	public int getSpecies() {
		return species;
	}

	public void setSpecies(int species) {
		this.species = species;
	}

	public List<LinkGene> getLinks() {
		return links;
	}

	public void setLinks(List<LinkGene> links) {
		this.links = links;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public NeuronDB getNeurons() {
		return neurons;
	}

	public void setNeurons(NeuronDB neurons) {
		this.neurons = neurons;
	}

	public int getGenomeSize() {
		return links.size() + neurons.sizeWithoutBias();
	}

	public int getId() {
		return id;
	}

	public double getAdjustedFitness() {
		return adjustedFitness;
	}

	public void setAdjustedFitness(double adjustedFitness) {
		this.adjustedFitness = adjustedFitness;
	}

	public void setSolution(boolean b) {
		isSolution = b;
	}

	public boolean getSolution() {
		return isSolution;
	}

	public String getLinkWeights() {
		StringBuilder sb = new StringBuilder();

		for (LinkGene l : links) {
			sb.append(l.getWeight());
			sb.append(" ");
		}

		return sb.toString();
	}

	public int getDisabledCount() {
		int count = 0;
		for (LinkGene l : links) {
			if (!l.isEnabled()) {
				count++;
			}
		}
		return count;
	}

	public double getTestFitness() {
		return testFitness;
	}

	public void setTestFitness(double testFitness) {
		this.testFitness = testFitness;
	}

	@Override
	public int compareTo(NeatChromosome o) {
		//Note: not consistent with equals
		//will cause lists of chroms to sort smallest adjusted fitness to largest
		//this means best fitness is at the end of the list
		return Double.compare(this.adjustedFitness, o.adjustedFitness);
	}
}
