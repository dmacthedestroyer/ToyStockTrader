import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class ProfitExplorer {
	public static void main(String[] args) throws IOException {
		Map<Double, Double> investmentThresholdsByProfit = new HashMap<>();

		List<Double> thresholds = DoubleStream.iterate(0, n -> n + 0.01).limit(100).boxed().collect(Collectors.toList());
		for (int i = 0; i < thresholds.size(); i++) System.out.print("-");
		System.out.println();

		for (double t: thresholds) {
			investmentThresholdsByProfit.put(t, getAverageProfit(new StockTrader(t)));
			System.out.print("*");
		}
		System.out.println("|");

		investmentThresholdsByProfit.entrySet().stream()
				.sorted((e1, e2) -> {
					int byValue = (int)((e2.getValue() - e1.getValue())*1000);
					if(byValue != 0)
						return byValue;
					return (int)((e2.getKey() - e1.getKey())*1000);
				})
				.forEach(e -> System.out.printf("%.2f -> %.2f\n", e.getKey(), e.getValue()));
	}

	public static double getAverageProfit(StockTrader trader) throws IOException {
		double averageProfit = Main.loadAllStockInfo().stream()
				.mapToDouble(si -> Main.simulateTrading(trader, si) - Main.STARTING_CASH)
				.average().getAsDouble();

//		System.out.printf("Average Profit: %.2f", averageProfit);

		return averageProfit;
	}
}
