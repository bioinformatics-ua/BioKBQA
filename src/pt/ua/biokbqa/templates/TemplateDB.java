package pt.ua.biokbqa.templates;

public class TemplateDB {
	private java.sql.Connection conn = null;
	private String path = "";

	public TemplateDB() {
		String currentOs = System.getProperty("os.name").toUpperCase();
		if (currentOs.contains("WIN")) {
			path = ".\\";
		} else {
			path = ".//";
		}
		String url = "jdbc:sqlite:" + path + "templates.db";
		String path1 = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		path1 = new java.io.File(path1).getParent();
		try {
			String driverName = "org.sqlite.JDBC";
			Class.forName(driverName);
			conn = java.sql.DriverManager.getConnection(url);
			conn.createStatement().execute(
					"CREATE TABLE IF NOT EXISTS Template ([question] VARCHAR, [questionSkeleton] VARCHAR, [querySkeleton] VARCHAR, [questionTemplate] VARCHAR, [queryTemplate] VARCHAR, [query] VARCHAR);");
		} catch (Exception e) {
		}
	}

	public void insert(String cmd) throws java.sql.SQLException {
		conn.createStatement().executeUpdate(cmd);
	}

	public java.sql.ResultSet query(String cmd) throws java.sql.SQLException {
		return conn.createStatement().executeQuery(cmd);
	}

	public void save(String question, String questionSkeleton, String querySkeleton, String questionTemplate,
			String queryTemplate, String query) {
		try {
			insert("INSERT INTO [Template] (" + "[question], " + "[questionSkeleton], " + "[querySkeleton], "
					+ "[questionTemplate], " + "[queryTemplate], " + "[query]) " + "VALUES (" + "'" + question + "', "
					+ "'" + questionSkeleton + "', " + "'" + querySkeleton + "', " + "'" + questionTemplate + "', "
					+ "'" + queryTemplate + "', " + "'" + query + "');");
		} catch (Exception e) {
		}
	}

	public String getQueryTemplate(String questionTemplate) {
		String qt = "";
		try {
			java.sql.ResultSet resultSet = query(
					"SELECT * FROM [Template] WHERE [questionTemplate] == '" + questionTemplate + "';");
			if (resultSet != null) {
				try {
					while (resultSet.next()) {
						qt = resultSet.getString(5);
					}
				} catch (Exception ex) {
				}
			}
		} catch (Exception ex) {
		}
		return qt;
	}
}
