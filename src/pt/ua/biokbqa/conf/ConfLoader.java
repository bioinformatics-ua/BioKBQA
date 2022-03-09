package pt.ua.biokbqa.conf;

public class ConfLoader {

	public static String property;
	public static String tagmekey;
	public static String jdbcUrl;
	public static String jdbcUser;
	public static String jdbcPassword;
	public static String jdbcDbname;

	public void loadConfig(String filename) {
		try {
			java.util.Properties props = new java.util.Properties();
			java.io.FileInputStream fis = new java.io.FileInputStream(
					new java.io.File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
							.getParent() + (System.getProperty("os.name").toUpperCase().contains("WIN") ? "\\" : "//")
							+ filename);
			props.loadFromXML(fis);
			ConfLoader.property = props.getProperty("namespace.name");
			ConfLoader.tagmekey = props.getProperty("spotter.tagmekey");
			ConfLoader.jdbcUrl = props.getProperty("jdbc.url");
			ConfLoader.jdbcUser = props.getProperty("jdbc.user");
			ConfLoader.jdbcPassword = props.getProperty("jdbc.password");
			ConfLoader.jdbcDbname = props.getProperty("jdbc.dbname");
		} catch (Exception ex) {
		}
	}
}
