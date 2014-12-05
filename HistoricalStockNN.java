import org.neuroph.core.data.DataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoricalStockNN extends StockNN {
	private int historyDepth;
	private List<DailyStockInfo> history;


	protected HistoricalStockNN(int historyDepth, int hiddenFirstLayerPerceptronCount, int hiddenSecondLayerPerceptronCount, double learningRate) {
		super(hiddenFirstLayerPerceptronCount, hiddenSecondLayerPerceptronCount, learningRate);
		this.historyDepth = historyDepth;
	}

	@Override
	public double getTotalError(DailyStockInfo[] testingData) {
		double totalCalculated = 0, totalActual = 0;
		for (int i = 0; i < testingData.length - 1; i++) {
			history.add(testingData[i]);

			double[] input = buildDataSetInputRow(history.size());
			normalizer.normalizeInput(input);

			double[] output = new double[]{testingData[i + 1].openingPrice};
			normalizer.normalizeOutput(output);

			neuralNetwork.setInput(input);
			neuralNetwork.calculate();
			double calculated = neuralNetwork.getOutput()[0];
			double actual = output[0];

//			if (calculated < 0.1 || calculated > 0.8 || actual < 0 || actual > 1)
//				System.out.printf("%f\t%f\n", calculated, Math.max(Math.min(1, actual), 0));

			totalCalculated += calculated;
			totalActual += actual;
		}

		return Math.abs(totalCalculated - totalActual) / totalActual;
	}

	@Override
	protected String getName() {
		return "Historical";
	}

	@Override
	protected DataSet buildDataSet(DailyStockInfo[] data) {
		normalizer = new PersistentMaxMinNormalizer();
		history = new ArrayList<>(Arrays.asList(data));

		DataSet ds = new DataSet(getDataSetInputSize(), 1);
		for (int rowIndex = historyDepth; rowIndex < history.size() - 1; rowIndex++) {
			double[] input = buildDataSetInputRow(rowIndex);
			double[] output = new double[]{data[rowIndex + 1].openingPrice};

			ds.addRow(input, output);
		}

		normalizer.normalize(ds);

		return ds;
	}

	private double[] buildDataSetInputRow(int rowIndex) {
		List<DailyStockInfo> data = history.subList(rowIndex - historyDepth, rowIndex);
		double[] input = new double[getDataSetInputSize()];
		for (int i = 0; i < data.size(); i++) {
			DailyStockInfo row = data.get(i);
			double[] rowInputs = new double[]{row.openingPrice, row.highPrice, row.lowPrice, row.closingPrice, row.volume};
			for (int j = 0; j < rowInputs.length; j++)
				input[i + (j * getFeaturesPerRecord())] = rowInputs[j];
		}

		return input;
	}

	private int getFeaturesPerRecord() {
		return 5;
	}

	private int getDataSetInputSize() {
		return getFeaturesPerRecord() * historyDepth;
	}

	public String toString(){
		return String.format("%s\t%s", historyDepth, super.toString());
	}
}