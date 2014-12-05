import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NNBenchmark {
	public static void main(String[] args) throws IOException, InterruptedException {
		getAverageError(new HistoricalDataStockTrader(), Main.loadAllStockInfo().get(0));
	}

	private static void exploreConfigurations() {
		List<StockTrader> traders = new ArrayList<>();

		for (double learningRate = 0.01; learningRate < 0.3; learningRate += 0.01)
			for (int historyDepth = 1; historyDepth <= 10; historyDepth++) {
				traders.add(new HistoricalDataStockTrader(historyDepth, null, null, learningRate));
				for (int firstNeuronLayerCount = 1; firstNeuronLayerCount <= 20 && firstNeuronLayerCount <= 1.5 * historyDepth; firstNeuronLayerCount++)
					//				for (int secondNeuronLayerCount = 0; secondNeuronLayerCount < 20 && secondNeuronLayerCount <= firstNeuronLayerCount; secondNeuronLayerCount++)
					traders.add(new HistoricalDataStockTrader(historyDepth, firstNeuronLayerCount, null, learningRate));
			}
		Collections.shuffle(traders);

		System.out.println("Total configurations to explore: " + traders.size());

		ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
		for (StockTrader t : traders)
			service.submit(() -> printPerformanceResults(t));
	}

	private static void printPerformanceResults(StockTrader t) {
		try {
			long start = System.currentTimeMillis();
			System.out.printf("%s\t%s\t%.5f%s\n", new SimpleDateFormat("MM/dd/yyyy h:mm:ss a").format(new Date()), t.toString(), getAverageError(t), (System.currentTimeMillis() - start) / 1000);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static double getAverageError(StockTrader trader) throws IOException {
		return Main.loadAllStockInfo().stream()
				.mapToDouble(data -> getAverageError(trader, data))
				.sum();
	}

	public static double getAverageError(StockTrader trader, DailyStockInfo[] data) {
		int present = 3 * data.length / 4;
		DailyStockInfo[] historicalInfo = Arrays.copyOfRange(data, 0, present);
		trader.processHistoricalInfo(historicalInfo);

		double totalError = 0, totalOutput = 0;

		for (int i = present; i < data.length - 1; i++) {
			double expectedOpeningPrice = trader.getExpectedOpeningPrice(data[i]);
			double actualOpeningPrice = trader.getNormalizedOutput(data[i + 1].openingPrice);

			totalError += Math.abs(expectedOpeningPrice - actualOpeningPrice);
			totalOutput += actualOpeningPrice;

			System.out.printf("%.3f\t%.3f\n", expectedOpeningPrice, actualOpeningPrice);
		}

		double averageError = totalError / totalOutput;
		System.out.printf("\nTotal Error: %.3f\n\n", averageError);
		return averageError;
	}
}