import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoricalDataStockTrader extends StockTrader {
	private final int HISTORY_DEPTH = 10;

	private List<DailyStockInfo> historicalInfo;
	private List<DailyStockInfo> previousQueriedDays = new ArrayList<>();

	public void processHistoricalInfo(DailyStockInfo[] historicalInfo) {
		this.historicalInfo = Arrays.asList(historicalInfo);

		DataSet ds = buildDataSet(historicalInfo);
		NeuralNetwork<BackPropagation> neuralNetwork = new MultiLayerPerceptron(ds.getInputSize(), 15, 15, ds.getOutputSize());
		neuralNetwork.getLearningRule().setMaxIterations(1000000);
		neuralNetwork.learn(ds);

		nn = neuralNetwork;
	}

	public boolean shouldInvest(DailyStockInfo todaysInfo) {
		previousQueriedDays.add(todaysInfo);
		return getExpectedOpeningPrice(todaysInfo) < 0.3;
	}

	public double getExpectedOpeningPrice(DailyStockInfo todaysInfo) {
		previousQueriedDays.add(todaysInfo);
		List<DailyStockInfo> historyDays = new ArrayList<>();
		if (previousQueriedDays.size() < HISTORY_DEPTH) {
			historyDays.addAll(historicalInfo.subList(historicalInfo.size() - HISTORY_DEPTH + previousQueriedDays.size(), historicalInfo.size()));
		}
		historyDays.addAll(previousQueriedDays.subList(Math.max(0, previousQueriedDays.size() - HISTORY_DEPTH), previousQueriedDays.size()));

		double[] inputs = new double[historyDays.size() * 2];
		for (int i = 0; i < historyDays.size(); i++) {
			inputs[i] = historyDays.get(i).openingPrice;
			inputs[i + 1] = historyDays.get(i).volume;
		}
		normalizer.normalizeInput(inputs);

		nn.setInput(inputs);
		nn.calculate();
		double output = nn.getOutput()[0];
		return output;
	}

	private DataSet buildDataSet(DailyStockInfo[] historicalInfo) {
		normalizer = new PersistentMaxMinNormalizer();

		DataSet ds = new DataSet(HISTORY_DEPTH * 2, 1);
		for (int i = 0; i < historicalInfo.length - HISTORY_DEPTH; i++) {
			double[] inputs = new double[HISTORY_DEPTH * 2];
			for (int j = 0; j < HISTORY_DEPTH; j++) {
				inputs[j] = (historicalInfo[i + j].openingPrice);
				inputs[j + HISTORY_DEPTH] = (double) historicalInfo[i + j].volume;
			}
			ds.addRow(inputs, new double[]{historicalInfo[i + HISTORY_DEPTH].openingPrice});
		}

		normalizer.normalize(ds);
		return ds;
	}
}