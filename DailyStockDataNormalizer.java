import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.util.data.norm.Normalizer;

public class DailyStockDataNormalizer implements Normalizer {
	private static final double[] max = new double[]{213.0, 213.65, 211.1, 213.07, 1629940.0};
	private static final double[] min = new double[]{3.53, 3.71, 3.51, 3.7, 1434.0};

	@Override
	public void normalize(DataSet dataSet) {
		for (DataSetRow row : dataSet.getRows())
			normalize(row.getInput());
	}

	public static double[] normalize(DailyStockInfo data) {
		double[] d = new double[]{data.openingPrice, data.highPrice, data.lowPrice, data.closingPrice, data.volume};
		normalize(d);
		return d;
	}

	private static void normalize(double[] dataSet) {
		if (max.length != dataSet.length)
			throw new IllegalArgumentException("input must be length " + max.length);

		for (int i = 0; i < dataSet.length; i++)
			dataSet[i] = (dataSet[i] - min[i]) / (max[i] - min[i]);
	}
}
