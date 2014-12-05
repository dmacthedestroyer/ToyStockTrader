import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Implements an automated stock trader.  The trader is fed daily information about
 * a single stock and decides whether to invest in the stock.
 */
public class StockTrader {
	/**
	 * Processes historical (past) stock information.
	 *
	 * @param historicalInfo historical stock information in chronological order.
	 */
	public void processHistoricalInfo(DailyStockInfo[] historicalInfo) {
		throw new NotImplementedException();
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
		throw new NotImplementedException();
	}

	public double getNormalizedOutput(double output){
		throw new NotImplementedException();
	}

	public double getExpectedOpeningPrice(DailyStockInfo todaysInfo) {
		throw new NotImplementedException();
	}
}