public final class NNConfiguration {
	private final int historyDepth, futureDepth;
	private final int l1PerceptronCount, l2PerceptronCount;
	private final double learningRate;

	public NNConfiguration(int historyDepth, int futureDepth, int l1PerceptronCount, int l2PerceptronCount, double learningRate) {
		this.historyDepth = historyDepth;
		this.futureDepth = futureDepth;
		this.l1PerceptronCount = l1PerceptronCount;
		this.l2PerceptronCount = l2PerceptronCount;
		this.learningRate = learningRate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		NNConfiguration that = (NNConfiguration) o;

		if (futureDepth != that.futureDepth) return false;
		if (historyDepth != that.historyDepth) return false;
		if (l1PerceptronCount != that.l1PerceptronCount) return false;
		if (l2PerceptronCount != that.l2PerceptronCount) return false;
		if (Double.compare(that.learningRate, learningRate) != 0) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = historyDepth;
		result = 31 * result + futureDepth;
		result = 31 * result + l1PerceptronCount;
		result = 31 * result + l2PerceptronCount;
		temp = Double.doubleToLongBits(learningRate);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public int getHistoryDepth() {
		return historyDepth;
	}

	public int getFutureDepth() {
		return futureDepth;
	}

	public int getL1PerceptronCount() {
		return l1PerceptronCount;
	}

	public int getL2PerceptronCount() {
		return l2PerceptronCount;
	}

	public double getLearningRate() {
		return learningRate;
	}

	@Override
	public String toString() {
		return String.format("%s\t%s\t%s\t%s\t%s", getHistoryDepth(), getFutureDepth(), getL1PerceptronCount(), getL2PerceptronCount(), getLearningRate());
	}
}