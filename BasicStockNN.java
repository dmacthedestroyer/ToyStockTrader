import org.neuroph.core.data.DataSet;

public class BasicStockNN extends StockNN {
	public BasicStockNN(int hiddenFirstLayerPerceptronCount, double learningRate) {
		super(hiddenFirstLayerPerceptronCount, 0, learningRate);
	}

	public double getTotalError(DailyStockInfo[] testingData) {
		double totalCalculated = 0, totalActual = 0;

		for (int i = 0; i < testingData.length - 1; i++) {
			DailyStockInfo d = testingData[i];
			double[] input = new double[]{d.openingPrice, d.highPrice, d.lowPrice, d.closingPrice, d.volume};
			normalizer.normalizeInput(input);

			double[] output = new double[]{testingData[i + 1].openingPrice};
			normalizer.normalizeOutput(output);

			neuralNetwork.setInput(input);
			neuralNetwork.calculate();
			double calculated = neuralNetwork.getOutput()[0];
			double actual = output[0];

//			if(calculated < 0.1 || calculated > 0.8 || actual < 0 || actual > 1)
//				System.out.printf("%f\t%f\n", calculated, actual);

			totalCalculated += calculated;
			totalActual += actual;
		}

		return Math.abs(totalCalculated - totalActual) / totalActual;
	}

	protected DataSet buildDataSet(DailyStockInfo[] data) {
		normalizer = new PersistentMaxMinNormalizer();
		DataSet ds = new DataSet(5, 1);
		for (int i = 0; i < data.length - 1; i++) {
			DailyStockInfo d = data[i];
			ds.addRow(new double[]{d.openingPrice, d.highPrice, d.lowPrice, d.closingPrice, d.volume}, new double[]{data[i + 1].openingPrice});
		}
		normalizer.normalize(ds);

		return ds;
	}

	public String getName() {
		return "Basic";
	}
}