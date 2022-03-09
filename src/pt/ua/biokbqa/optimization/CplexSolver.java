package pt.ua.biokbqa.optimization;

import ilog.concert.IloException;
import ilog.concert.IloIntMap;
import ilog.concert.IloIntRange;
import ilog.concert.IloNumMap;
import ilog.cplex.IloCplex;
import ilog.opl.IloOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

public class CplexSolver {

	void buildInstance(File instancefile, CplexInstance instance) throws Exception {
		FileWriter writer;
		writer = new FileWriter(instancefile);
		writer.write("\n M\t\t= ");
		writer.write(Integer.toString(instance.M));
		writer.write(";\n P\t\t= ");
		writer.write(Integer.toString(instance.P));
		writer.write(";\n N\t\t= ");
		writer.write(Integer.toString(instance.O));
		writer.write(";\n T\t\t= ");
		writer.write(Integer.toString(instance.T));
		writer.write(";\n S\t\t= ");
		writer.write(Integer.toString(instance.N));
		writer.write(";\n\n beta\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write(Integer.toString(instance.beta[p]));
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n gamma\t= ");
		writer.write(Double.toString(instance.gamma));
		if (instance.f2Allowed || instance.f1Allowed) {
			writer.write(";\n budget\t= ");
			writer.write(Double.toString(instance.budget));
		}
		if (instance.f1Allowed) {
			writer.write(";\n delta\t= ");
			writer.write(Double.toString(instance.delta));
			writer.write(";\n eps\t= [");
			for (int p = 0; p < instance.P; p++) {
				writer.write(Double.toString(instance.eps[p]));
				if (p < instance.P - 1) {
					writer.write(" ");
				}
			}
			writer.write("]");
		}
		writer.write(";\n alpha\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write(Double.toString(instance.alpha[p]));
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n L\t\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write(Integer.toString(instance.lt[p]));
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n d\t\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write(Integer.toString(instance.d[p]));
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n mu\t\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write(Double.toString(instance.s[p]));
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n D\t\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write("[");
			for (int t = 0; t < instance.T; t++) {
				writer.write("[");
				for (int o = 0; o < instance.O; o++) {
					writer.write(Double.toString(instance.D[p][t][o]));
					if (t < instance.O - 1) {
						writer.write(" ");
					}
				}
				writer.write("]");
				if (t < instance.T - 1) {
					writer.write(" ");
				}
			}
			writer.write("]");
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n l\t\t= [");
		for (int m = 0; m < instance.M; m++) {
			writer.write("[");
			for (int p = 0; p < instance.P; p++) {
				writer.write("[");
				for (int t = 0; t < instance.T; t++) {
					writer.write(Integer.toString(instance.al[m][p][t]));
					if (t < instance.T - 1) {
						writer.write(" ");
					}
				}
				writer.write("]");
				if (p < instance.P - 1) {
					writer.write(" ");
				}
			}
			writer.write("]");
			if (m < instance.M - 1) {
				writer.write(" ");
			}
		}
		if (instance.f2Allowed) {
			writer.write("];\n al\t\t= [");
			for (int m = 0; m < instance.M; m++) {
				writer.write("[");
				for (int p = 0; p < instance.P; p++) {
					writer.write("[");
					for (int t = 0; t < instance.T; t++) {
						writer.write(Integer.toString(instance.aal[m][p][t]));
						if (t < instance.T - 1) {
							writer.write(" ");
						}
					}
					writer.write("]");
					if (p < instance.P - 1) {
						writer.write(" ");
					}
				}
				writer.write("]");
				if (m < instance.M - 1) {
					writer.write(" ");
				}
			}
		}
		writer.write("];\n initU\t\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write("[");
			for (int w = 0; w < instance.N; w++) {
				writer.write(Integer.toString(instance.initU[p][w]));
				if (w < instance.N - 1) {
					writer.write(" ");
				}
			}
			writer.write("]");
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n r\t\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write("[");
			for (int t = 0; t < instance.T; t++) {
				writer.write("[");
				for (int o = 0; o < instance.O; o++) {
					writer.write(Double.toString(instance.r[p][t][o]));
					if (o < instance.O - 1) {
						writer.write(" ");
					}
				}
				writer.write("]");
				if (t < instance.T - 1) {
					writer.write(" ");
				}
			}
			writer.write("]");
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n h\t\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write("[");
			for (int t = 0; t < instance.T; t++) {
				writer.write(Double.toString(instance.h[p][t]));
				if (t < instance.T - 1) {
					writer.write(" ");
				}
			}
			writer.write("]");
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n b\t\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write("[");
			for (int t = 0; t < instance.T; t++) {
				writer.write("[");
				for (int o = 0; o < instance.O; o++) {
					writer.write(Double.toString(instance.b[p][t][o]));
					if (o < instance.O - 1) {
						writer.write(" ");
					}
				}
				writer.write("]");
				if (t < instance.T - 1) {
					writer.write(" ");
				}
			}
			writer.write("]");
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n c\t\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write("[");
			for (int t = 0; t < instance.T; t++) {
				writer.write(Double.toString(instance.c[p][t]));
				if (t < instance.T - 1) {
					writer.write(" ");
				}
			}
			writer.write("]");
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		if (instance.f2Allowed) {
			writer.write("];\n f\t\t= [");
			for (int p = 0; p < instance.P; p++) {
				writer.write(Double.toString(instance.f[p]));
				if (p < instance.P - 1) {
					writer.write(" ");
				}
			}
		}
		writer.write("];\n initI\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write("[");
			for (int o = 0; o < instance.O; o++) {
				writer.write(Double.toString(instance.initI[p][o]));
				if (o < instance.O - 1) {
					writer.write(" ");
				}
			}
			writer.write("]");
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n initB\t= [");
		for (int p = 0; p < instance.P; p++) {
			writer.write("[");
			for (int o = 0; o < instance.O; o++) {
				writer.write(Double.toString(instance.initB[p][o]));
				if (o < instance.O - 1) {
					writer.write(" ");
				}
			}
			writer.write("]");
			if (p < instance.P - 1) {
				writer.write(" ");
			}
		}
		writer.write("];\n initWIP= [");
		for (int m = 0; m < instance.M; m++) {
			writer.write("[");
			for (int p = 0; p < instance.P; p++) {
				writer.write("[");
				for (int t = 0; t < instance.initWIP[m][p].length; t++) {
					writer.write("[");
					for (int o = 0; o < instance.O; o++) {
						writer.write(Double.toString(instance.initWIP[m][p][t][o]));
						if (t < instance.O - 1) {
							writer.write(" ");
						}
					}
					writer.write("]");
					if (t < instance.initWIP[m][p].length - 1) {
						writer.write(" ");
					}
				}
				writer.write("]");
				if (p < instance.P - 1) {
					writer.write(" ");
				}
			}
			writer.write("]");
			if (m < instance.M - 1) {
				writer.write(" ");
			}
		}
		writer.write("];");
		writer.flush();
		writer.close();
	}

	void getModel(File modelfile, CplexInstance instance) throws Exception {
		FileWriter writer;
		writer = new FileWriter(modelfile);
		writer.write("\n// parameter:\n");
		writer.write(" int M = ...;\n");
		writer.write(" int P = ...;\n");
		writer.write(" int N = ...;\n");
		writer.write(" int T = ...;\n");
		writer.write(" int S = ...;\n\n");
		writer.write(" range rm = 1..M;\n");
		writer.write(" range rp = 1..P;\n");
		writer.write(" range ro = 1..N;\n");
		writer.write(" range rt = 1..T;\n");
		writer.write(" range rs = 1..S;\n\n");
		writer.write(" int beta[rp]       = ...;\n");
		if (instance.f2Allowed || instance.f1Allowed) {
			writer.write(" float budget       = ...;\n");
		}
		writer.write(" float gamma        = ...;\n");
		writer.write(" float alpha[rp]    = ...;\n");
		writer.write(" int L[rp]          = ...;\n");
		writer.write(" float D[rp][rt][ro]= ...;\n");
		writer.write(" int l[rm][rp][rt]  = ...;\n");
		writer.write(" float r[rp][rt][ro]= ...;\n");
		writer.write(" float h[rp][rt]    = ...;\n");
		writer.write(" float b[rp][rt][ro]= ...;\n");
		writer.write(" float c[rp][rt]    = ...;\n");
		writer.write(" float initI[rp][ro]= ...;\n");
		if (instance.f2Allowed) {
			writer.write(" int al[rm][rp][rt] = ...;\n");
			writer.write(" float f[rp]        = ...;\n");
		}
		if (instance.f1Allowed) {
			writer.write(" float delta        = ...;\n");
			writer.write(" float eps[rp]      = ...;\n");
		}
		writer.write(" int d[rp]        = ...;\n");
		writer.write(" float mu[rp]       = ...;\n");
		writer.write(" float initU[rp][rs]    = ...;\n");
		writer.write(" float initB[rp][ro]= ...;\n");
		writer.write(" int maxval = max(p in rp) L[p];\n");
		writer.write(" float initWIP[rm][rp][1..maxval][ro] = ...;\n\n");
		writer.write(" // variables:\n");
		writer.write(" dvar float+ X[rm][rp][rt][ro];\n");
		writer.write(" dvar float+ Y[rm][rp][rt][ro];\n");
		writer.write(" dvar int+   V[rm][rp][rt];\n");
		writer.write(" dvar int+   W[rm][rp][rt][rs];\n");
		writer.write(" dvar int+   U[rp][rt][rs];\n");
		if (instance.f1Allowed) {
			writer.write(" dvar float+ O[rm][rp][rt];\n");
		}
		if (instance.f2Allowed) {
			writer.write(" dvar int+   Z[rm][rp][rt];\n");
		}
		writer.write(" dvar float+ I[rp][rt][ro];\n");
		writer.write(" dvar float+ B[rp][rt][ro];\n\n\n");
		writer.write("// objective:\n");
		writer.write("maximize  sum(m in rm, p in rp, t in rt, o in ro) r[p][t][o]    * Y[m][p][t][o]\n");
		writer.write("\t\t- sum(m in rm, p in rp, t in rt, o in ro) c[p][t]       * X[m][p][t][o]\n");
		if (instance.f2Allowed) {
			writer.write("\t\t- sum(m in rm, p in rp, t in rt         ) f[p]          * Z[m][p][t]\n");
		}
		if (instance.f1Allowed) {
			writer.write("\t\t- sum(m in rm, p in rp, t in rt         ) eps[p]        * O[m][p][t]\n");
		}
		writer.write("\t\t- sum(         p in rp, s in rs, t in 1..T) mu[p]         * U[p][t][s]\n");
		writer.write("\t\t+ sum(         p in rp, s in rs, t in 2..T) mu[p]         * U[p][t-1][s]\n");
		writer.write("\t\t- sum(         p in rp, t in rt, o in ro) h[p][t]         * I[p][t][o]\n");
		writer.write("\t\t- sum(         p in rp, t in rt, o in ro) b[p][t][o]      * B[p][t][o];\n\n");
		writer.write("// constraints:\n");
		writer.write("subject to {\n");
		writer.write("\t// demand\n");
		writer.write(
				"\tforall(p in rp,            o in ro) sum(m in rm) Y[m][p][1][o] + initI[p][o]  - initB[p][o]  - I[p][1][o] + B[p][1][o] == D[p][1][o];\n");
		writer.write(
				"\tforall(p in rp, t in 2..T, o in ro) sum(m in rm) Y[m][p][t][o] + I[p][t-1][o] - B[p][t-1][o] - I[p][t][o] + B[p][t][o] == D[p][t][o];\n\n");
		writer.write("\t// capacity\n");
		if (instance.f1Allowed) {
			writer.write(
					"\tforall(m in rm, p in rp, t in rt) sum(i in 1..minl(L[p],T-t)) (alpha[p] / (L[p] + 1)) * sum(o in ro) Y[m][p][t+i][o] + (alpha[p] / (L[p] + 1)) * sum(o in ro) Y[m][p][t][o] <= gamma * V[m][p][t] + O[m][p][t] / beta[p];\n\n");
		} else {
			writer.write(
					"\tforall(m in rm, p in rp, t in rt) sum(i in 1..minl(L[p],T-t)) (alpha[p] / (L[p] + 1)) * sum(o in ro) Y[m][p][t+i][o] + (alpha[p] / (L[p] + 1)) * sum(o in ro) Y[m][p][t][o] <= gamma * V[m][p][t];\n\n");
		}
		writer.write("\t// input output balance\n");
		writer.write(
				"\tforall(m in rm, p in rp, t in rt, o in ro) if (t - L[p] > 0) X[m][p][t - L[p]][o] == Y[m][p][t][o];\n");
		writer.write(
				"\t                                           else              initWIP[m][p][t][o]  == Y[m][p][t][o];\n\n");
		writer.write("\t// inventory, backlog level balance\n");
		writer.write("\tforall(p in rp, t in rt) minl(sum(o in ro) I[p][t][o], sum(o in ro) B[p][t][o]) == 0;\n\n");
		writer.write("\t// assembly lines\n");
		if (instance.f2Allowed) {
			writer.write("\tforall(m in rm, p in rp, t in rt) V[m][p][t] <= l[m][p][t] + al[m][p][t];\n\n");
		} else {
			writer.write("\tforall(m in rm, p in rp, t in rt) V[m][p][t] <= l[m][p][t];\n\n");
		}
		writer.write("\t// worker assignment\n");
		writer.write("\tforall(t in rt, s in rs) sum(m in rm, p in rp) W[m][p][t][s] <= 1;\n\n");
		writer.write("\t// worker training time\n");
		writer.write(
				"\tforall(p in rp, t in rt, s in rs) if (t - d[p] > 0) sum(m in rm) W[m][p][t][s] <= U[p][t-d[p]][s];\n");
		writer.write(
				"\t                                  else              sum(m in rm) W[m][p][t][s] <= initU[p][s];\n\n");
		writer.write("\t// worker skill\n");
		writer.write("\tforall(p in rp, t in 2..T, s in rs) if (t - d[p] >= 0) U[p][t-1][s] <= U[p][t][s];\n");
		writer.write("\tforall(p in rp, t in rt, s in rs)   if (t - d[p] <= 0) initU[p][s]  <= U[p][t][s];\n");
		writer.write("\t// assembly line worker relation\n");
		writer.write("\tforall(m in rm, p in rp, t in rt) sum (s in rs) W[m][p][t][s] == beta[p] * V[m][p][t];\n\n");
		if (instance.f2Allowed) {
			writer.write("\t// additional assembly lines\n");
			writer.write("\tforall(m in rm, p in rp, t in rt) Z[m][p][t] >= V[m][p][t] - l[m][p][t];\n\n");
		}
		if (instance.f1Allowed) {
			writer.write("\t// additional capacity of workers\n");
			writer.write("\tforall(m in rm, p in rp, t in rt) O[m][p][t] <= delta * sum(s in rs) W[m][p][t][s];\n\n");
		}
		if (instance.f1Allowed || instance.f2Allowed) {
			writer.write("\t// financial limitation for capacity expansions\n");
			writer.write("\tsum(m in rm, p in rp, t in rt) (");
			boolean help = false;
			if (instance.f2Allowed) {
				writer.write("f[p] * Z[m][p][t]");
				help = true;
			}
			if (instance.f1Allowed) {
				if (help) {
					writer.write(" + eps[p] * O[m][p][t]");
				} else {
					writer.write("eps[p] * O[m][p][t]");
				}
				help = true;
			}
			writer.write(") <= budget;\n\n");
		}
		writer.write("\t// variable nonnegativity\n");
		writer.write("\tforall(m in rm, p in rp, t in rt, o in ro) X[m][p][t][o] >= 0.0;\n");
		writer.write("\tforall(m in rm, p in rp, t in rt, o in ro) Y[m][p][t][o] >= 0.0;\n");
		writer.write("\tforall(m in rm, p in rp, t in rt) V[m][p][t] >= 0;\n");
		writer.write("\tforall(m in rm, p in rp, t in rt, s in rs) W[m][p][t][s] >= 0;\n");
		writer.write("\tforall(m in rm, p in rp, t in rt, s in rs) W[m][p][t][s] <= 1;\n");
		writer.write("\tforall(m in rm, p in rp, t in rt, s in rs) U[p][t][s] >= 0;\n");
		writer.write("\tforall(m in rm, p in rp, t in rt, s in rs) U[p][t][s] <= 1;\n");
		if (instance.f1Allowed) {
			writer.write("\tforall(m in rm, p in rp, t in rt) O[m][p][t] >= 0.0;\n");
		}
		if (instance.f2Allowed) {
			writer.write("\tforall(m in rm, p in rp, t in rt) Z[m][p][t] >= 0;\n");
		}
		writer.write("\tforall(p in rp, t in rt, o in ro) I[p][t][o] >= 0;\n");
		writer.write("\tforall(p in rp, t in rt, o in ro) B[p][t][o] >= 0;\n");
		writer.write("}");
		writer.flush();
		writer.close();
	}

	CplexSolution solve(String modelfilename, String instancefilename, CplexInstance instance) throws Exception {
		CplexSolution solution = new CplexSolution();
		IloOplFactory.setDebugMode(false);
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
		IloOplModelSource modelSource = oplF.createOplModelSource(modelfilename);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def = oplF.createOplModelDefinition(modelSource, settings);
		IloCplex cplex = oplF.createCplex();
		cplex.setOut(null);
		IloOplModel opl = oplF.createOplModel(def, cplex);
		IloOplDataSource dataSource = oplF.createOplDataSource(instancefilename);
		opl.addDataSource(dataSource);
		opl.generate();
		// Solve model and access solution
		if (cplex.solve()) {
			solution.objectiveValue = opl.getCplex().getObjValue();
			opl.postProcess();
		} else {
			System.out.println("No solution!");
		}
		oplF.end();
		return solution;
	}

	int[][][] getIlogDecisionVarIntTriple(int M, int P, int T, IloOplModel opl, String decVarName) {
		int[][][] DecVar = new int[M][P][T];
		IloIntMap var = opl.getElement(decVarName).asIntMap();
		IloIntRange m1 = opl.getElement("rm").asIntRange();
		IloIntRange p1 = opl.getElement("rp").asIntRange();
		IloIntRange t1 = opl.getElement("rt").asIntRange();
		int m = 0;
		for (Iterator<?> it1 = m1.iterator(); it1.hasNext();) {
			int p = 0;
			Integer sub1 = (Integer) it1.next();
			try {
				IloIntMap sub1M = var.getSub(sub1);
				for (Iterator<?> it2 = p1.iterator(); it2.hasNext();) {
					int t = 0;
					Integer sub2 = (Integer) it2.next();
					try {
						IloIntMap sub2M = sub1M.getSub(sub2);
						for (Iterator<?> it3 = t1.iterator(); it3.hasNext();) {
							Integer sub3 = (Integer) it3.next();
							DecVar[m][p][t] = sub2M.get(sub3);
							t++;
						}
						p++;
					} catch (Exception ex) {
					}
				}
				m++;
			} catch (Exception ex) {
			}
		}
		return DecVar;
	}

	int[][][] getIlogDecisionVarIntTriple2(int P, int T, int N, IloOplModel opl, String decVarName) {
		int[][][] DecVar = new int[P][T][N];
		IloIntMap var = opl.getElement(decVarName).asIntMap();
		IloIntRange p1 = opl.getElement("rp").asIntRange();
		IloIntRange t1 = opl.getElement("rt").asIntRange();
		IloIntRange n1 = opl.getElement("rs").asIntRange();
		int p = 0;
		for (Iterator<?> it1 = p1.iterator(); it1.hasNext();) {
			int t = 0;
			Integer sub1 = (Integer) it1.next();
			try {
				IloIntMap sub1M = var.getSub(sub1);
				for (Iterator<?> it2 = t1.iterator(); it2.hasNext();) {
					int n = 0;
					Integer sub2 = (Integer) it2.next();
					try {
						IloIntMap sub2M = sub1M.getSub(sub2);
						for (Iterator<?> it3 = n1.iterator(); it3.hasNext();) {
							Integer sub3 = (Integer) it3.next();
							DecVar[p][t][n] = sub2M.get(sub3);
							n++;
						}
						t++;
					} catch (Exception ex) {
					}
				}
				p++;
			} catch (Exception ex) {
			}
		}
		return DecVar;
	}

	int[][][][] getIlogDecisionVarIntQuadriple(int M, int P, int T, int N, IloOplModel opl, String decVarName) {
		int[][][][] DecVar = new int[M][P][T][N];
		IloIntMap var = opl.getElement(decVarName).asIntMap();
		IloIntRange m1 = opl.getElement("rm").asIntRange();
		IloIntRange p1 = opl.getElement("rp").asIntRange();
		IloIntRange t1 = opl.getElement("rt").asIntRange();
		IloIntRange n1 = opl.getElement("rs").asIntRange();
		int m = 0;
		for (Iterator<?> it1 = m1.iterator(); it1.hasNext();) {
			int p = 0;
			Integer sub1 = (Integer) it1.next();
			try {
				IloIntMap sub1M = var.getSub(sub1);
				for (Iterator<?> it2 = p1.iterator(); it2.hasNext();) {
					int t = 0;
					Integer sub2 = (Integer) it2.next();
					try {
						IloIntMap sub2M = sub1M.getSub(sub2);
						for (Iterator<?> it3 = t1.iterator(); it3.hasNext();) {
							int n = 0;
							Integer sub3 = (Integer) it3.next();
							try {
								IloIntMap sub3M = sub2M.getSub(sub3);
								for (Iterator<?> it4 = n1.iterator(); it4.hasNext();) {
									Integer sub4 = (Integer) it4.next();
									DecVar[m][p][t][n] = sub3M.get(sub4);
									n++;
								}
								t++;
							} catch (IloException ex) {
							}
						}
						p++;
					} catch (Exception ex) {
					}
				}
				m++;
			} catch (Exception ex) {
			}
		}
		return DecVar;
	}

	double[][] getIlogDecisionVarDblTuple(int P, int T, IloOplModel opl, String decVarName) {
		double[][] DecVar = new double[P][T];
		IloNumMap var = opl.getElement(decVarName).asNumMap();
		IloIntRange p1 = opl.getElement("rp").asIntRange();
		IloIntRange t1 = opl.getElement("rt").asIntRange();
		int p = 0;
		for (Iterator<?> it1 = p1.iterator(); it1.hasNext();) {
			int t = 0;
			Integer sub1 = (Integer) it1.next();
			try {
				IloNumMap sub1M = var.getSub(sub1);
				for (Iterator<?> it2 = t1.iterator(); it2.hasNext();) {
					Integer sub2 = (Integer) it2.next();
					DecVar[p][t] = sub1M.get(sub2);
					t++;
				}
				p++;
			} catch (Exception ex) {
			}
		}
		return DecVar;
	}

	double[][][] getIlogDecisionVarDblTriple(int M, int P, int T, IloOplModel opl, String decVarName) {
		double[][][] DecVar = new double[M][P][T];
		IloNumMap var = opl.getElement(decVarName).asNumMap();
		IloIntRange m1 = opl.getElement("rm").asIntRange();
		IloIntRange p1 = opl.getElement("rp").asIntRange();
		IloIntRange t1 = opl.getElement("rt").asIntRange();
		int m = 0;
		for (Iterator<?> it1 = m1.iterator(); it1.hasNext();) {
			int p = 0;
			Integer sub1 = (Integer) it1.next();
			try {
				IloNumMap sub1M = var.getSub(sub1);
				for (Iterator<?> it2 = p1.iterator(); it2.hasNext();) {
					int t = 0;
					Integer sub2 = (Integer) it2.next();
					try {
						IloNumMap sub2M = sub1M.getSub(sub2);
						for (Iterator<?> it3 = t1.iterator(); it3.hasNext();) {
							Integer sub3 = (Integer) it3.next();
							DecVar[m][p][t] = sub2M.get(sub3);
							t++;
						}
						p++;
					} catch (Exception ex) {
					}
				}
				m++;
			} catch (Exception ex) {
			}
		}
		return DecVar;
	}

	double[][][] getIlogDecisionVarDblTriple2(int P, int T, int O, IloOplModel opl, String decVarName) {
		double[][][] DecVar = new double[P][T][O];
		IloNumMap var = opl.getElement(decVarName).asNumMap();
		IloIntRange m1 = opl.getElement("rp").asIntRange();
		IloIntRange p1 = opl.getElement("rt").asIntRange();
		IloIntRange t1 = opl.getElement("ro").asIntRange();
		int p = 0;
		for (Iterator<?> it1 = m1.iterator(); it1.hasNext();) {
			int t = 0;
			Integer sub1 = (Integer) it1.next();
			try {
				IloNumMap sub1M = var.getSub(sub1);
				for (Iterator<?> it2 = p1.iterator(); it2.hasNext();) {
					int o = 0;
					Integer sub2 = (Integer) it2.next();
					try {
						IloNumMap sub2M = sub1M.getSub(sub2);
						for (Iterator<?> it3 = t1.iterator(); it3.hasNext();) {
							Integer sub3 = (Integer) it3.next();
							DecVar[p][t][o] = sub2M.get(sub3);
							o++;
						}
						t++;
					} catch (Exception ex) {
					}
				}
				p++;
			} catch (Exception ex) {
			}
		}
		return DecVar;
	}

	double[][][][] getIlogDecisionVarDblQuadriple(int M, int P, int O, int T, IloOplModel opl, String decVarName) {
		double[][][][] DecVar = new double[M][P][T][O];
		IloNumMap var = opl.getElement(decVarName).asNumMap();
		IloIntRange m1 = opl.getElement("rm").asIntRange();
		IloIntRange p1 = opl.getElement("rp").asIntRange();
		IloIntRange o1 = opl.getElement("ro").asIntRange();
		IloIntRange t1 = opl.getElement("rt").asIntRange();
		int m = 0;
		for (Iterator<?> it1 = m1.iterator(); it1.hasNext();) {
			int p = 0;
			Integer sub1 = (Integer) it1.next();
			try {
				IloNumMap sub1M = var.getSub(sub1);
				for (Iterator<?> it2 = p1.iterator(); it2.hasNext();) {
					int t = 0;
					Integer sub2 = (Integer) it2.next();
					try {
						IloNumMap sub2M = sub1M.getSub(sub2);
						for (Iterator<?> it3 = t1.iterator(); it3.hasNext();) {
							int o = 0;
							Integer sub3 = (Integer) it3.next();
							try {
								IloNumMap sub3M = sub2M.getSub(sub3);
								for (Iterator<?> it4 = o1.iterator(); it4.hasNext();) {
									Integer sub4 = (Integer) it4.next();
									DecVar[m][p][t][o] = sub3M.get(sub4);
									o++;
								}
								t++;
							} catch (Exception ex) {
							}
						}
						p++;
					} catch (Exception ex) {
					}
				}
				m++;
			} catch (Exception ex) {
			}
		}
		return DecVar;
	}
}
