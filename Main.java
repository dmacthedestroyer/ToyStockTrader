import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
	public static final double STARTING_CASH = 1000;
	public static final double TRADE_COST = 5;
	private static final boolean LOG_TRADES = false;

	public static void main(String[] args) throws IOException {
		double totalProfit = 0;
		for (DailyStockInfo[] data : loadAllStockInfo()) {
			double profit = simulateTrading(data) - STARTING_CASH;
			totalProfit += profit;
			System.out.println("Profit: " + String.format("%.2f", profit));
		}

		System.out.printf("Total Profit: %.2f\n", totalProfit);
	}

	public static List<DailyStockInfo[]> loadAllStockInfo() {
		List<DailyStockInfo[]> stockInfo = new ArrayList<>();

		File[] files = new File(".\\src").listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".csv");
			}
		});
		for (File file : files) {
			try {
				stockInfo.add(loadStockInfo(file));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return stockInfo;
	}

	private static DailyStockInfo[] loadStockInfo(File filename) throws IOException {
		ArrayList<DailyStockInfo> data = new ArrayList<>(245);
		BufferedReader input = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = input.readLine()) != null) {
			Scanner sc = new Scanner(line);
			sc.useDelimiter(",");
			while (sc.hasNext())
				data.add(new DailyStockInfo(sc.nextDouble(), sc.nextDouble(), sc.nextDouble(), sc.nextDouble(), sc.nextInt()));
		}
		input.close();
		return data.toArray(new DailyStockInfo[data.size()]);
	}

	public static double simulateTrading(DailyStockInfo[] data) {
		return simulateTrading(new StockTrader(), data);
	}

	public static double simulateTrading(StockTrader trader, DailyStockInfo[] data) {
		int present = 3 * data.length / 4;
		DailyStockInfo[] historicalInfo = Arrays.copyOfRange(data, 0, present);
		trader.processHistoricalInfo(historicalInfo);

		double cash = STARTING_CASH;
		int sharesOwned = 0;
		int elapsedDays = 1;
		for (; present < data.length - 1; present++, elapsedDays++) {
			boolean shouldBeInvested = trader.shouldInvest(data[present]);
			if (shouldBeInvested && sharesOwned == 0) { // Buy
				double buyPrice = data[present + 1].openingPrice;
				sharesOwned = Math.max(0, (int) ((cash - TRADE_COST) / buyPrice));
				if (sharesOwned > 0) {
					if (LOG_TRADES)
						System.out.println("Day " + elapsedDays + ": bought " + sharesOwned + " shares @ " + buyPrice);
					cash -= sharesOwned * buyPrice + TRADE_COST;
				}
			} else if (!shouldBeInvested && sharesOwned > 0) { // Sell
				double sellPrice = data[present + 1].openingPrice;
				double newCash = cash + sharesOwned * sellPrice - TRADE_COST;
				if (newCash >= 0) {
					if (LOG_TRADES)
						System.out.println("Day " + elapsedDays + ": sold " + sharesOwned + " shares @ " + sellPrice);
					cash = newCash;
					sharesOwned = 0;
				}
			}
		}
		// Force sale at the end of the last day.
		if (sharesOwned > 0) {
			double sellPrice = data[present].closingPrice;
			if (LOG_TRADES)
				System.out.println("Day " + elapsedDays + ": sold " + sharesOwned + " shares @ " + sellPrice);
			cash += sharesOwned * sellPrice - TRADE_COST;
		}

		return cash;
	}
}