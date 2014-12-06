import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockTraderNN {
	public static void main(String[] args) {
		DailyStockInfo[] ds = Main.loadAllStockInfo().get(0);

		StockTraderNN nn = new StockTraderNN(3, 2, 0, 0.1);
		nn.learn(Arrays.copyOfRange(ds, 0, 3 * ds.length / 4));
	}

	private boolean[] optimalTrainingDecisions = new boolean[]{false, false, true, true, true, true, true, false, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, true, true, false, false, false, true, true, true, true, true, true, true, false, false, false, false, false, true, true, false, false, false, false, true, true, true, true, true, true, true, true, true, false, false, true, false, false, false, true, true, true, true, true, false, false, false, false, true, true, true, true, false, true, true, true, true, true, true, true, false, true, true, false, false, false, false, true, false, false, true, true, false, true, true, false, false, false, false, false, false, false, true, true, false, false, true, true, false, false, true, false, true, true, true, true, false, false, false, true, true, true, true, true, true, true, false, true, true, true, false, false, false, false, true, false, true, true, true, true, false, true, true, true, true, true, false, false, false, true, false, false, true, true, false, true, true, false, true, true, false, false, true, true, false, false, false, false, false, true, true, true, true, false, false, false, false, false, false, false};

	protected double learningRate;
	protected int hiddenFirstLayerPerceptronCount, hiddenSecondLayerPerceptronCount;

	protected MultiLayerPerceptron neuralNetwork;
	protected PersistentMaxMinNormalizer normalizer;

	private int historyDepth;
	private List<DailyStockInfo> history;

	public StockTraderNN(int historyDepth, int hiddenFirstLayerPerceptronCount, int hiddenSecondLayerPerceptronCount, double learningRate) {
		this.historyDepth = historyDepth;
		this.hiddenFirstLayerPerceptronCount = hiddenFirstLayerPerceptronCount;
		this.hiddenSecondLayerPerceptronCount = hiddenSecondLayerPerceptronCount;
		this.learningRate = learningRate;
	}

	public StockTraderNN(NNConfiguration nn) {
		this(nn.getHistoryDepth(), nn.getL1PerceptronCount(), nn.getL2PerceptronCount(), nn.getLearningRate());
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

	private void outputTrainingResults() {
		for (int i = historyDepth; i < history.size()-1; i++) {
			neuralNetwork.setInput(buildDataSetInputRow(i));
			neuralNetwork.calculate();
			double[] calculated = neuralNetwork.getOutput();
			boolean actual = optimalTrainingDecisions[i];

			System.out.printf("%f\t%f\t%s\n", calculated[0],calculated[1], actual ? 1 : 0);
		}
	}

//	public double getTotalError(DailyStockInfo[] testingData) {
//		double[] totalCalculated = new double[futureDepth], totalActual = new double[futureDepth];
//		history.addAll(Arrays.asList(testingData));
//
//		for (int i = history.size() - testingData.length; i < history.size() - futureDepth; i++) {
//			double[] actual = buildDataSetOutputRow(i);
//			normalizer.normalizeOutput(actual);
//
//			double[] calculated = getForecastHorizon(i);
//
////			for (int j = 0; j < calculated.length; j++) {
////				System.out.printf("%.4f\t", Math.abs((calculated[j] - actual[j]) / actual[j]));
////				System.out.printf("%.4f\t%.4f\n", calculated[j], actual[j]);
////			}
////			System.out.println();
//
//			for (int j = 0; j < calculated.length; j++) {
//				totalCalculated[j] += calculated[j];
//				totalActual[j] += actual[j];
//			}
//		}
//
//		double[] totalError = new double[futureDepth];
//		for (int i = 0; i < totalError.length; i++)
//			totalError[i] = Math.abs((totalCalculated[i] - totalActual[i]) / totalActual[i]);
//
////		System.out.printf("Total Error: %s\n", Arrays.toString(totalError));
//
//		double totalErrorSum = 0;
//		for (double v : totalError) totalErrorSum += v;
//
//		return totalErrorSum;
//	}

	protected DataSet buildDataSet(DailyStockInfo[] data) {
//		System.out.printf("%s\t%s\n", data.length, optimalTrainingDecisions.length);
		normalizer = new PersistentMaxMinNormalizer();
		history = new ArrayList<>(Arrays.asList(data));

		//todo: build optimal training decisions

		DataSet ds = new DataSet(2 * historyDepth, 2);
		for (int rowIndex = historyDepth; rowIndex < history.size()-1; rowIndex++) {
			double[] input = buildDataSetInputRow(rowIndex);
			double[] output = buildDataSetOutputRow(rowIndex);

			ds.addRow(input, output);
		}

		normalizer.normalize(ds);

//		for (DataSetRow row : ds.getRows())
//			System.out.printf("%s\t%s\n", Arrays.toString(row.getInput()), Arrays.toString(row.getDesiredOutput()));
//		System.out.println();

		return ds;
	}

	private double[] buildDataSetInputRow(int rowIndex) {
		List<DailyStockInfo> data = history.subList(rowIndex - historyDepth, rowIndex);
		List<Double> inputList = new ArrayList<>();
		data.stream().forEach(dsi -> inputList.addAll(Arrays.asList(dsi.openingPrice, (double) dsi.volume)));

		double[] inputArray = new double[inputList.size()];
		for (int i = 0; i < inputList.size(); i++)
			inputArray[i] = inputList.get(i);

		return inputArray;
	}

	private double[] buildDataSetOutputRow(int rowIndex) {
		return optimalTrainingDecisions[rowIndex] ? new double[]{1, 0} : new double[]{0, 1};
	}

	public boolean shouldInvest(DailyStockInfo todaysInfo){
		history.add(todaysInfo);

		neuralNetwork.setInput(buildDataSetInputRow(history.size()-1));
		neuralNetwork.calculate();
		double[] output = neuralNetwork.getOutput();
		return output[0] > output[1];
	}
}