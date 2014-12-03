import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.util.data.norm.Normalizer;

public class PersistentMaxMinNormalizer implements Normalizer {
	private double[] maxInput, minInput, maxOutput, minOutput;

	@Override
	public void normalize(DataSet dataSet) {
		maxInput = buildDefaultArray(dataSet.getInputSize(), Double.MIN_VALUE);
		minInput = buildDefaultArray(dataSet.getInputSize(), Double.MAX_VALUE);
		maxOutput = buildDefaultArray(dataSet.getOutputSize(), Double.MIN_VALUE);
		minOutput = buildDefaultArray(dataSet.getOutputSize(), Double.MAX_VALUE);

		for (DataSetRow row : dataSet.getRows()) {
			double[] input = row.getInput();
			for (int i = 0; i < input.length; i++) {
				maxInput[i] = Math.max(maxInput[i], input[i]);
				minInput[i] = Math.min(minInput[i], input[i]);
			}
			double[] output = row.getDesiredOutput();
			for (int o = 0; o < output.length; o++) {
				maxOutput[o] = Math.max(maxOutput[o], output[o]);
				minOutput[o] = Math.min(minOutput[o], output[o]);
			}
		}

		for (DataSetRow row : dataSet.getRows()) {
			normalizeInput(row.getInput());
			normalizeOutput(row.getDesiredOutput());
		}
	}

	private double[] buildDefaultArray(int size, double defaultValue) {
		double[] ret = new double[size];
		for (int i = 0; i < size; i++) {
			ret[i] = defaultValue;
		}

		return ret;
	}

	public void normalizeInput(double[] input) {
		for (int i = 0; i < input.length; i++)
			input[i] = normalizeInput(i, input[i]);
	}

	private double normalizeInput(int column, double denormalizedValue) {
		return normalize(denormalizedValue, maxInput[column], minInput[column]);
	}

	public void normalizeOutput(double[] output){
		for (int i = 0; i < output.length; i++) {
			output[i] = normalizeOutput(i, output[i]);
		}
	}

	private double normalizeOutput(int column, double denormalizedValue) {
		return normalize(denormalizedValue, maxOutput[column], minOutput[column]);
	}

	private double normalize(double denormalizedValue, double maxValue, double minValue) {
		return (denormalizedValue - minValue) / (maxValue - minValue);
	}
}
