import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;

public abstract class StockNN {
	protected double learningRate;
	protected int hiddenFirstLayerPerceptronCount, hiddenSecondLayerPerceptronCount;

	protected MultiLayerPerceptron neuralNetwork;
	protected PersistentMaxMinNormalizer normalizer;

	protected StockNN(int hiddenFirstLayerPerceptronCount, int hiddenSecondLayerPerceptronCount, double learningRate) {
		this.hiddenFirstLayerPerceptronCount = hiddenFirstLayerPerceptronCount;
		this.hiddenSecondLayerPerceptronCount = hiddenSecondLayerPerceptronCount;
		this.learningRate = learningRate;
	}

	public void learn(DailyStockInfo[] trainingData) {
		DataSet ds = buildDataSet(trainingData);
		if (hiddenFirstLayerPerceptronCount <= 0)
			if (hiddenSecondLayerPerceptronCount <= 0)
				neuralNetwork = new MultiLayerPerceptron(ds.getInputSize(), hiddenFirstLayerPerceptronCount, hiddenSecondLayerPerceptronCount, ds.getOutputSize());
			else
				neuralNetwork = new MultiLayerPerceptron(ds.getInputSize(), hiddenFirstLayerPerceptronCount, ds.getOutputSize());
		else neuralNetwork = new MultiLayerPerceptron(ds.getInputSize(), ds.getOutputSize());

		neuralNetwork.getLearningRule().setMaxIterations(10000);
		neuralNetwork.getLearningRule().setMaxError(0.0001);

		neuralNetwork.getLearningRule().setLearningRate(learningRate);
//		neuralNetwork.getLearningRule().addListener(e -> System.out.printf("%s\t%s\n", ((BackPropagation)e.getSource()).getCurrentIteration(), ((BackPropagation)e.getSource()).getTotalNetworkError()));
		neuralNetwork.learn(ds);
	}

	public abstract double getTotalError(DailyStockInfo[] testingData);

	protected abstract String getName();

	protected abstract DataSet buildDataSet(DailyStockInfo[] data);

	public String toString() {
		return String.format("%s\t%s\t%s\t%.3f", getName(), hiddenFirstLayerPerceptronCount, hiddenSecondLayerPerceptronCount, learningRate);
	}
}