import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NNEvaluator {
	public static void main(String[] args) {
		List<NNConfiguration> nns = new ArrayList<>();
		final double learningRate = 0.1;
		for (int historyDepth = 0; historyDepth <= 10; historyDepth += 2)
			for (int futureDepth = 3; futureDepth <= 6; futureDepth += 2)
				for (int l1PerceptronCount = 1; l1PerceptronCount < 20; l1PerceptronCount++)
					for (int l2PerceptronCount = 0; l2PerceptronCount < l1PerceptronCount; l2PerceptronCount++)
						nns.add(new NNConfiguration(historyDepth, futureDepth, l1PerceptronCount, l2PerceptronCount, learningRate));

		Collections.shuffle(nns);
		System.out.printf("%s total configurations to explore\n", nns.size());

		ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
		for (NNConfiguration nnc : nns)
			service.submit(() -> {
				long start = System.currentTimeMillis();
				System.out.printf("%s\t%f\t%s\n", nnc, getTotalAverageError(new ForecastStockNN(nnc)), (System.currentTimeMillis() - start) / 1000);
			});

//		System.out.println("Total Avg. Error: " + getTotalAverageError(new ForecastStockNN(4, 1, 2, 0, 0.1)));
	}

	public static double getTotalAverageError(ForecastStockNN nn) {
		return Main.loadAllStockInfo().stream()
				.mapToDouble(data -> getAverageError(nn, data))
				.sum();
	}

	public static double getAverageError(ForecastStockNN nn, DailyStockInfo[] data) {
		int present = 3 * data.length / 4;
		DailyStockInfo[] train = Arrays.copyOfRange(data, 0, present);
		nn.learn(train);

		DailyStockInfo[] test = Arrays.copyOfRange(data, present - 1, data.length);
		return nn.getTotalError(test);
	}
}