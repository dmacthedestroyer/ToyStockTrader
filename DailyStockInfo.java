/**
 * This class encapsulates the daily information for a stock.
 */
public class DailyStockInfo {
	/**
	 * Holds the opening price (price at the start of the trading day) for the stock.
	 */
	public double openingPrice;

	/**
	 * Holds the highest price of the stock over the trading day.
	 */
	public double highPrice;

	/**
	 * Holds the lowest price of the stock over the trading day.
	 */
	public double lowPrice;

	/**
	 * Holds the closing price (price at the end of the trading day) for the stock.
	 */
	public double closingPrice;

	/**
	 * Holds the number of shares traded over the day for the stock.
	 */
	public int volume;

	public DailyStockInfo(double openingPrice, double highPrice, double lowPrice, double closingPrice, int volume) {
		this.openingPrice = openingPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.closingPrice = closingPrice;
		this.volume = volume;
	}
}