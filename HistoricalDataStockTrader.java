import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HistoricalDataStockTrader extends StockTrader {
	protected MultiLayerPerceptron nn;
	protected final DailyStockDataNormalizer normalizer = new DailyStockDataNormalizer();
	protected int historyDepth;
	protected Integer firstLayerPerceptronCount, secondLayerPerceptronCount;
	protected double learningRate;

	private List<DailyStockInfo> historicalInfo;
	private List<DailyStockInfo> previousQueriedDays = new ArrayList<>();

	public HistoricalDataStockTrader() {
		this(5, 2, null, 0.1);
	}

	public HistoricalDataStockTrader(int historyDepth, Integer firstLayerPerceptronCount, Integer secondLayerPerceptronCount, double learningRate) {
		this.historyDepth = historyDepth;
		this.firstLayerPerceptronCount = firstLayerPerceptronCount;
		this.secondLayerPerceptronCount = secondLayerPerceptronCount;
		this.learningRate = learningRate;
	}

	public void processHistoricalInfo(DailyStockInfo[] historicalInfo) {
		this.historicalInfo = Arrays.asList(historicalInfo);

		DataSet ds = buildDataSet(historicalInfo);
		if (firstLayerPerceptronCount != null)
			if (secondLayerPerceptronCount != null)
				nn = new MultiLayerPerceptron(ds.getInputSize(), firstLayerPerceptronCount, secondLayerPerceptronCount, ds.getOutputSize());
			else
				nn = new MultiLayerPerceptron(ds.getInputSize(), firstLayerPerceptronCount, ds.getOutputSize());
		else nn = new MultiLayerPerceptron(ds.getInputSize(), ds.getOutputSize());

		nn.randomizeWeights();
		nn.getLearningRule().setMaxIterations(5000);
		nn.getLearningRule().setLearningRate(learningRate);
		nn.getLearningRule().addListener(e -> System.out.printf("%s\t%s\n", ((BackPropagation) e.getSource()).getCurrentIteration(), ((BackPropagation) e.getSource()).getTotalNetworkError()));
		nn.learn(ds);

		System.out.println(Arrays.toString(nn.getWeights()));
	}

	public boolean shouldInvest(DailyStockInfo todaysInfo) {
		throw new NotImplementedException();
	}

	public int getDataSetInputSize() {
		return historyDepth;
	}

	private List<DailyStockInfo> getHistoryInput(){
		List<DailyStockInfo> historyDays = new ArrayList<>();
		if (previousQueriedDays.size() < historyDepth) {
			historyDays.addAll(historicalInfo.subList(historicalInfo.size() - historyDepth + previousQueriedDays.size(), historicalInfo.size()));
		}
		historyDays.addAll(previousQueriedDays.subList(Math.max(0, previousQueriedDays.size() - historyDepth), previousQueriedDays.size()));

		return historyDays;
	}

	public double getExpectedOpeningPrice(DailyStockInfo todaysInfo) {
		previousQueriedDays.add(todaysInfo);

		Double[] normalized = getHistoryInput().stream()
				.map((t) -> normalizer.normalize(t)[0])
				.collect(Collectors.toList())
				.toArray(new Double[0]);
		double[] inputs = new double[getDataSetInputSize()];
		for (int i = 0; i < normalized.length; i++)
			inputs[i] = normalized[i];

		nn.setInput(inputs);
		nn.calculate();
		double output = getNormalizedOutput(nn.getOutput()[0]);

		System.out.printf("%s\t%s\n", Arrays.toString(inputs), Arrays.toString(nn.getOutput()));

		return output;
	}

	public double getNormalizedOutput(double output) {
		return normalizer.normalize(new DailyStockInfo(output, 0, 0, 0, 0))[0];
	}

	private DataSet buildDataSet(DailyStockInfo[] historicalInfo) {
		DataSet ds = new DataSet(getDataSetInputSize(), 1);
		for (int i = 0; i < historicalInfo.length - historyDepth; i++) {
			double[] inputs = new double[getDataSetInputSize()];
			for (int j = 0; j < historyDepth; j++)
				inputs[j] = (historicalInfo[i + j].openingPrice);
			ds.addRow(inputs, new double[]{normalizer.normalize(historicalInfo[i + historyDepth])[0]});
		}

		normalizer.normalize(ds);

		for (DataSetRow row : ds.getRows())
			System.out.printf("%s %s\n", Arrays.toString(row.getInput()), Arrays.toString(row.getDesiredOutput()));
		System.out.println();

		return ds;
	}

	public String toString() {
		return String.format("HistoricalDataStockTrader\t%s\t%s\t%s\t%.3f", historyDepth, firstLayerPerceptronCount, secondLayerPerceptronCount, learningRate);
	}
}