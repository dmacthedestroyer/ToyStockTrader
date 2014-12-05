import org.neuroph.core.data.DataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForecastStockNN extends StockNN {
	private int historyDepth, futureDepth;
	private List<DailyStockInfo> history;

	public ForecastStockNN(int historyDepth, int futureDepth, int hiddenFirstLayerPerceptronCount, int hiddenSecondLayerPerceptronCount, double learningRate) {
		super(hiddenFirstLayerPerceptronCount, hiddenSecondLayerPerceptronCount, learningRate);
		this.historyDepth = historyDepth;
		this.futureDepth = futureDepth;
	}

	public ForecastStockNN(NNConfiguration nn) {
		this(nn.getHistoryDepth(), nn.getFutureDepth(), nn.getL1PerceptronCount(), nn.getL2PerceptronCount(), nn.getLearningRate());
	}

	@Override
	public double getTotalError(DailyStockInfo[] testingData) {
		double[] totalCalculated = new double[futureDepth], totalActual = new double[futureDepth];
		history.addAll(Arrays.asList(testingData));

		for (int i = history.size() - testingData.length; i < history.size() - futureDepth; i++) {
			double[] actual = buildDataSetOutputRow(i);
			normalizer.normalizeOutput(actual);

			double[] input = buildDataSetInputRow(i);
			normalizer.normalizeInput(input);
			neuralNetwork.setInput(input);
			neuralNetwork.calculate();
			double[] calculated = neuralNetwork.getOutput();

//			for (int j = 0; j < calculated.length; j++)
//				System.out.printf("%.4f\t", Math.abs((calculated[j] - actual[j]) / actual[j]));
//			System.out.println();

			for (int j = 0; j < calculated.length; j++) {
				totalCalculated[j] += calculated[j];
				totalActual[j] += actual[j];
			}
		}

		double[] totalError = new double[futureDepth];
		for (int i = 0; i < totalError.length; i++)
			totalError[i] = Math.abs((totalCalculated[i] - totalActual[i]) / totalActual[i]);

//		System.out.printf("Total Error: %s\n", Arrays.toString(totalError));

		double totalErrorSum = 0;
		for (double v : totalError) totalErrorSum += v;

		return totalErrorSum;
	}

	@Override
	protected String getName() {
		return "Forecast";
	}

	@Override
	protected DataSet buildDataSet(DailyStockInfo[] data) {
		normalizer = new PersistentMaxMinNormalizer();
		history = new ArrayList<>(Arrays.asList(data));

		DataSet ds = new DataSet(getDataSetInputSize(), getDataSetOutputSize());
		for (int rowIndex = historyDepth; rowIndex < history.size() - futureDepth; rowIndex++) {
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
		data.stream().forEach(dsi -> inputList.addAll(Arrays.asList(dsi.openingPrice, dsi.highPrice, dsi.lowPrice, dsi.closingPrice, (double) dsi.volume)));

		double[] inputArray = new double[inputList.size()];
		for (int i = 0; i < inputList.size(); i++)
			inputArray[i] = inputList.get(i);

		return inputArray;
	}

	private double[] buildDataSetOutputRow(int rowIndex) {
		List<DailyStockInfo> data = history.subList(rowIndex, rowIndex + futureDepth);
		double[] input = new double[getDataSetOutputSize()];
		for (int i = 0; i < data.size(); i++)
			input[i] = data.get(i).openingPrice;

		return input;
	}

	private int getFeaturesPerRecord() {
		return 5;
	}

	private int getDataSetInputSize() {
		return getFeaturesPerRecord() * historyDepth;
	}

	private int getDataSetOutputSize() {
		return futureDepth;
	}

	public String toString() {
		return String.format("%s\t%s\t%s", historyDepth, futureDepth, super.toString());
	}
}