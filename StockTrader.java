import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;

/**
 * Implements an automated stock trader.  The trader is fed daily information about
 * a single stock and decides whether to invest in the stock.
 */
public class StockTrader {
	NeuralNetwork nn;
	PersistentMaxMinNormalizer normalizer;

	private final double investmentThreshold;

	public StockTrader() {
		this(0.2);
	}

	public StockTrader(double investmentThreshold) {
		this.investmentThreshold = investmentThreshold;
	}

	/**
	 * Processes historical (past) stock information.
	 *
	 * @param historicalInfo historical stock information in chronological order.
	 */
	public void processHistoricalInfo(DailyStockInfo[] historicalInfo) {
		nn = new MultiLayerPerceptron(5, 10, 1);
		BackPropagation lr = new BackPropagation();
		lr.setMaxIterations(10000);
		nn.setLearningRule(lr);

		nn.learn(buildDataSet(historicalInfo));
	}

	/**
	 * Determines whether the trader should be invested in the implied stock
	 * on the following day.
	 *
	 * @param todaysInfo stock information for the current day
	 *                   after the stock market has closed.
	 * @return <code>true</code> if the trader should invest in the stock
	 * on the following day, or <code>false</code> otherwise.
	 */
	public boolean shouldInvest(DailyStockInfo todaysInfo) {
		if (nn == null)
			throw new IllegalStateException("Must call processHistoricalInfo prior to calling shouldInvest");

		double[] input = new double[]{todaysInfo.openingPrice, todaysInfo.highPrice, todaysInfo.lowPrice, todaysInfo.closingPrice, todaysInfo.volume};
		normalizer.normalizeInput(input);
		nn.setInput(input);
		nn.calculate();
		double output = nn.getOutput()[0];
		System.out.println(output);

		return output > getInvestmentThreshold();
	}

	protected double getInvestmentThreshold() {
		return investmentThreshold;
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
}