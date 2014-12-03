import java.io.IOException;
import java.util.Arrays;

public class NNBenchmark {
	public static void main(String[] args) throws IOException {
		DailyStockInfo[] data = Main.loadAllStockInfo().stream().findFirst().get();
		StockTrader trader = new HistoricalDataStockTrader();

		int present = 3 * data.length / 4;
		DailyStockInfo[] historicalInfo = Arrays.copyOfRange(data, 0, present);
		trader.processHistoricalInfo(historicalInfo);

		for (int i = present; i < data.length - 1; i++) {
			System.out.printf("%f\t%f\n", trader.denormalizeOutput(trader.getExpectedOpeningPrice(data[i])), data[i + 1].openingPrice);
		}
	}
}
