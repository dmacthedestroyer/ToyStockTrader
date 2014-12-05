import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NNEvaluator {
	public static void main(String[] args) {
//		Set<NNConfiguration> nns = loadNNConfigurations();
//		final double learningRate = 0.1;
//		for (int historyDepth = 0; historyDepth <= 10; historyDepth += 2)
//			for (int futureDepth = 1; futureDepth <= 1; futureDepth += 2)
//				for (int l1PerceptronCount = 2; l1PerceptronCount < 3; l1PerceptronCount++)
//					for (int l2PerceptronCount = 0; l2PerceptronCount < 1; l2PerceptronCount++) {
//						NNConfiguration nn = new NNConfiguration(historyDepth, futureDepth, l1PerceptronCount, l2PerceptronCount, learningRate);
//						if(!nns.contains(nn))
//							nns.add(nn);
//					}
//		System.out.printf("%s total configurations to explore\n", nns.size());
//
//		ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
//		for (NNConfiguration nnc : nns)
//			service.submit(() -> {
//				long start = System.currentTimeMillis();
//				System.out.printf("%s\t%f\t%s\n", nnc, getTotalAverageError(new ForecastStockNN(nnc)), (System.currentTimeMillis() - start) / 1000);
//			});

		System.out.println("Total Avg. Error: " + getTotalAverageError(new ForecastStockNN(2, 5, 1, 0, 0.1)));
	}

	public static double getTotalAverageError(ForecastStockNN nn) {
		return getAverageErrors(nn).stream()
				.mapToDouble(d -> d)
				.sum();
	}

	public static List<Double> getAverageErrors(ForecastStockNN nn) {
		return Main.loadAllStockInfo().stream()
				.map(data -> getAverageError(nn, data))
				.collect(Collectors.toList());
	}

	public static double getAverageError(ForecastStockNN nn, DailyStockInfo[] data) {
		int present = 3 * data.length / 4;
		DailyStockInfo[] train = Arrays.copyOfRange(data, 0, present);
		nn.learn(train);

		DailyStockInfo[] test = Arrays.copyOfRange(data, present - 1, data.length);
		return nn.getTotalError(test);
	}
}