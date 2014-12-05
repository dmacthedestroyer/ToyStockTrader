import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Implements an automated stock trader.  The trader is fed daily information about
 * a single stock and decides whether to invest in the stock.
 */
public class StockTrader {
	static final double buyLowThreshold = 0.15;
	static final double sellHighThreshold = 0.8;

	private final ForecastStockNN nn;
	private Double currentPurchasedStockPrice;

	public StockTrader(){
		nn = new ForecastStockNN(0, 1, 1, 0, 0.1);
	}

	/**
	 * Processes historical (past) stock information.
	 *
	 * @param historicalInfo historical stock information in chronological order.
	 */
	public void processHistoricalInfo(DailyStockInfo[] historicalInfo) {
		nn.learn(historicalInfo);
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
		nn.addStockInfo(todaysInfo);
		double[] forecast = nn.getForecastHorizon();

		if (currentPurchasedStockPrice == null) { //we're not currently invested
			boolean buy = forecast[0] < 0.7;
			for (int i = 1; i < forecast.length; i++)
				buy = buy && forecast[0] < forecast[i]; //has the market bottomed out yet?
			if (buy)
				currentPurchasedStockPrice = forecast[0];
			return buy;
		} else { //we're currently invested
			boolean sell = currentPurchasedStockPrice > forecast[0] + 0.1;
			for (int i = 1; i < forecast.length; i++)
				sell = sell && forecast[0] > forecast[i]; //has the market peaked yet?
			if (sell) {
				currentPurchasedStockPrice = null;
				return false;
			}
			return true;
		}
	}

	public double getNormalizedOutput(double output){
		throw new NotImplementedException();
	}

	public double getExpectedOpeningPrice(DailyStockInfo todaysInfo) {
		throw new NotImplementedException();
	}
}