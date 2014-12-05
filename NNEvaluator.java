import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class NNEvaluator {
	private List<DailyStockInfo> historicalInfo;

	public static void main(String[] args) {
		Set<NNConfiguration> nns = loadNNConfigurations();
		final double learningRate = 0.1;
		for (int historyDepth = 0; historyDepth <= 10; historyDepth += 2)
			for (int futureDepth = 3; futureDepth <= 5; futureDepth += 2)
				for (int l1PerceptronCount = 0; l1PerceptronCount < historyDepth * 4; l1PerceptronCount++)
					for (int l2PerceptronCount = 0; l2PerceptronCount < l1PerceptronCount; l2PerceptronCount++) {
						NNConfiguration nn = new NNConfiguration(historyDepth, futureDepth, l1PerceptronCount, l2PerceptronCount, learningRate);
						if(!nns.contains(nn))
							nns.add(nn);
					}
		System.out.printf("%s total configurations to explore\n", nns.size());

		ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
		for (NNConfiguration nnc : nns)
			service.submit(() -> {
				long start = System.currentTimeMillis();
				System.out.printf("%s\t%f\t%s\n", nnc, getTotalAverageError(new ForecastStockNN(nnc)), (System.currentTimeMillis() - start) / 1000);
			});

//		System.out.println("Total Avg. Error: " + getTotalAverageError(new ForecastStockNN(6, 3, 16, 2, 0.1)));
	}

	private static Set<NNConfiguration> loadNNConfigurations(){
		Set<NNConfiguration> nns = new HashSet<>();

		//todo: load from a data file

		return nns;
	}

	public static double getTotalAverageError(StockNN nn) {
		return getAverageErrors(nn).stream()
				.mapToDouble(d -> d)
				.sum();
	}

	public static List<Double> getAverageErrors(StockNN nn) {
		return Main.loadAllStockInfo().stream()
				.map(data -> getAverageError(nn, data))
				.collect(Collectors.toList());
	}

	public static double getAverageError(StockNN nn, DailyStockInfo[] data) {
		int present = 3 * data.length / 4;
		DailyStockInfo[] train = Arrays.copyOfRange(data, 0, present);
		nn.learn(train);

		DailyStockInfo[] test = Arrays.copyOfRange(data, present - 1, data.length);
		return nn.getTotalError(test);
	}
}