import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OptimumStockTrader {
	public static void main(String[] args) {
		DailyStockInfo[] dailyStockInfos = Main.loadAllStockInfo().get(0);
		OptimumStockTrader t = new OptimumStockTrader();
		t.processHistoricalInfo(Arrays.copyOfRange(dailyStockInfos, 0, 3 * dailyStockInfos.length / 4));
	}

	private List<List<Boolean>> investmentPaths;
	private DailyStockInfo[] data;

	public void processHistoricalInfo(DailyStockInfo[] historicalInfo) {
		data = historicalInfo;
		investmentPaths = new ArrayList<>();
		investmentPaths.add(new ArrayList<>());
		investmentPaths.get(0).add(true);
		investmentPaths.add(new ArrayList<>());
		investmentPaths.get(1).add(false);

		for (int i = 1; i < data.length - 2; i++) {
			List<List<Boolean>> newPaths = new ArrayList<>();
			for (List<Boolean> t : investmentPaths) {
				List<Boolean> f = new ArrayList<>(t);
				f.add(false);
				newPaths.add(f);

				t.add(true);
				newPaths.add(t);
			}

			List<List<Boolean>> keepers = newPaths.stream()
					.filter(l -> !l.get(l.size() - 1))
					.sorted((l1, l2) -> (int) ((totalCash(l2) - totalCash(l1)) * 1000))
					.limit(1000)
					.collect(Collectors.toList());
			newPaths.stream().filter(l -> l.get(l.size() - 1)).forEach(keepers::add);


			investmentPaths = keepers;
			System.out.printf("%s\t%s\t%s\t%s\n", i+1,
					investmentPaths.stream().mapToInt(List::size).max().getAsInt(),
					investmentPaths.stream().filter(l -> l.get(l.size()-1)).count(),
					investmentPaths.stream().filter(l -> !l.get(l.size()-1)).count());
		}

		for (List<Boolean> investmentPath : investmentPaths)
			investmentPath.add(false);

		System.out.println("Top traders:");
		investmentPaths.stream().sorted((l1, l2) -> (int) ((totalCash(l2) - totalCash(l1)) * 1000))
				.limit(5)
				.forEach(l -> System.out.printf("%.2f\t%s\n", totalCash(l), Arrays.toString(l.toArray())));
	}

	private double totalCash(List<Boolean> investmentPath){
		double cash = Main.STARTING_CASH;
		int sharesOwned = 0;

		for (int i = 0; i < investmentPath.size(); i++) {
			boolean shouldBeInvested = investmentPath.get(i);
			if (shouldBeInvested && sharesOwned == 0) { // Buy
				double buyPrice = data[i + 1].openingPrice;
				sharesOwned = Math.max(0, (int) ((cash - Main.TRADE_COST) / buyPrice));
				if (sharesOwned > 0) {
					cash -= sharesOwned * buyPrice + Main.TRADE_COST;
				}
			} else if (!shouldBeInvested && sharesOwned > 0) { // Sell
				double sellPrice = data[i + 1].openingPrice;
				double newCash = cash + sharesOwned * sellPrice - Main.TRADE_COST;
				if (newCash >= 0) {
					cash = newCash;
					sharesOwned = 0;
				}
			}
		}

		return cash;
	}

	private boolean madeBadTrade(List<Boolean> investmentPath) {
		double cash = Main.STARTING_CASH;
		int sharesOwned = 0;

		double priorInvestmentCash = 0;

		for (int i = 0; i < investmentPath.size()-1; i++) {
			boolean shouldBeInvested = investmentPath.get(i);
			if (shouldBeInvested && sharesOwned == 0) { // Buy
				double buyPrice = data[i + 1].openingPrice;
				sharesOwned = Math.max(0, (int) ((cash - Main.TRADE_COST) / buyPrice));
				priorInvestmentCash = cash;
				if (sharesOwned > 0) {
					cash -= sharesOwned * buyPrice + Main.TRADE_COST;
				}
			} else if (!shouldBeInvested && sharesOwned > 0) { // Sell
				double sellPrice = data[i + 1].openingPrice;
				double newCash = cash + sharesOwned * sellPrice - Main.TRADE_COST;
				if (newCash >= 0) {
					cash = newCash;
					sharesOwned = 0;
				}
				if(priorInvestmentCash > newCash)
					return true;
			}
		}
		return false;
	}
}
