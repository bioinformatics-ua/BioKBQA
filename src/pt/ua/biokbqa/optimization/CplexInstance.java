package pt.ua.biokbqa.optimization;

public class CplexInstance implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public int scenarioID;
	public int instanceID;
	public int P;
	public int M;
	public int T;
	public int O;
	public int N;
	public int[] beta;
	public int[] lt;
	public int[] d;
	public int[][] initU;
	public int[][][] al;
	public int[][][] aal;
	public double gamma;
	public double delta;
	public double budget;
	public double[] alpha;
	public double[] f;
	public double[] s;
	public double[][] initI;
	public double[][] initB;
	public double[] eps;
	public double[][][] r;
	public double[][] h;
	public double[][][] b;
	public double[][] c;
	public double[][][] D;
	public double[][][][] initWIP;
	public boolean f1Allowed;
	public boolean f2Allowed;

	CplexInstance() {
		scenarioID = 0;
		instanceID = 0;
		P = 0;
		M = 0;
		T = 0;
		O = 0;
		N = 0;
		f1Allowed = false;
		f2Allowed = false;
	}

	CplexInstance(int sID, int iID) {
		scenarioID = sID;
		instanceID = iID;
		P = 0;
		M = 0;
		T = 0;
		O = 0;
		N = 0;
		f1Allowed = false;
		f2Allowed = false;
	}
}
