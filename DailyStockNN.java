import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;

public class DailyStockNN {
	NeuralNetwork nn;
	PersistentMaxMinNormalizer normalizer;

	public void learn(DailyStockInfo[] historicalInfo) {
		nn = new MultiLayerPerceptron(5, 12, 1);
		BackPropagation lr = new BackPropagation();
		lr.setMaxIterations(5000);
		nn.setLearningRule(lr);

		nn.learn(buildDataSet(historicalInfo));
	}

	private DataSet buildDataSet(DailyStockInfo[] historicalInfo) {
		DataSet ds = new DataSet(5, 1);
		for (int i = 0; i < historicalInfo.length - 1; i++) {
			DailyStockInfo dsi = historicalInfo[i];
			double nextDayOpeningPrice = historicalInfo[i + 1].openingPrice;
			ds.addRow(new double[]{dsi.openingPrice, dsi.highPrice, dsi.lowPrice, dsi.closingPrice, dsi.volume}, new double[]{nextDayOpeningPrice});
		}

		normalizer = new PersistentMaxMinNormalizer();
		normalizer.normalize(ds);

		return ds;
	}

	public boolean shouldInvest(DailyStockInfo todaysInfo) {
		double[] input = new double[]{todaysInfo.openingPrice, todaysInfo.highPrice, todaysInfo.lowPrice, todaysInfo.closingPrice, todaysInfo.volume};
		normalizer.normalizeInput(input);
		nn.setInput(input);
		nn.calculate();
		double output = nn.getOutput()[0];
		return output > 0.5;
	}
}
