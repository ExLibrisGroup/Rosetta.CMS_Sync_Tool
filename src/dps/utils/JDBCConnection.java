package dps.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author shaib
 *
 */
public class JDBCConnection {

	private String connectionUrl;
	private String[] usernames;
	private String[] passwords;
	private String[] schemaCodes;
	private Connection[] connections;
	private Properties properties;

	public JDBCConnection(String propertiesPath, String schemaCode ,String pw ,boolean flag)
	throws Exception {

		this(propertiesPath);
		System.setProperty("java.security.egd", "file:///dev/urandom");
		this.connectionUrl = properties.getProperty("dbconnection.url");
		this.connections = new Connection[1];
		this.schemaCodes = new String[1];
		this.usernames = new String[1];
		this.passwords = new String[1];
		this.schemaCodes[0] = schemaCode;
		this.usernames[0] = properties.getProperty("dbconnection." + schemaCode + ".username");
		this.passwords[0] = pw;
		this.connections[0] = DriverManager.getConnection(connectionUrl, usernames[0], passwords[0]);
		this.connections[0].setAutoCommit(false);
	}

	public JDBCConnection(String propertiesPath, String[] schemaCodesArr)
	throws Exception {

		this(propertiesPath);
		schemaCodes = schemaCodesArr;
		connectionUrl = properties.getProperty("dbconnection.url");
		connections = new Connection[schemaCodesArr.length];
		usernames = new String[schemaCodesArr.length];
		passwords = new String[schemaCodesArr.length];

		for (int i = 0; i < schemaCodes.length; i++) {

			usernames[i] = properties.getProperty("dbconnection." + schemaCodes[i] + ".username");
			passwords[i] = getPassword(usernames[i]);
			connections[i] = DriverManager.getConnection(connectionUrl, usernames[i], passwords[i]);
			connections[i].setAutoCommit(false);
		}
	}

	public JDBCConnection(String propertiesPath, String user, String pw)
	throws IOException, ClassNotFoundException, SQLException {

		this(propertiesPath);
		this.connections = new Connection[1];
		this.usernames = new String[1];
		this.passwords = new String[1];
		this.connectionUrl = properties.getProperty("dbconnection.url");
		this.usernames[0] = user;
		this.passwords[0] =pw;
		this.connections[0] = DriverManager.getConnection(connectionUrl, usernames[0], passwords[0]);
		this.connections[0].setAutoCommit(false);
	}

	private JDBCConnection(String propertiesPath)
	throws IOException, ClassNotFoundException {
		properties = new Properties();
		FileInputStream fis = new FileInputStream(new File(propertiesPath));
		properties.load(fis);
		Class.forName("oracle.jdbc.driver.OracleDriver");
	}

	public Connection getConnection(String schemaCode) {
		for (int i = 0; i < schemaCodes.length; i++) {
			if (schemaCode.equals(schemaCodes[i])) {
				return connections[i];
			}
		}
		throw new RuntimeException("Did not find connection for schemaCode: " + schemaCode);
	}

	public Connection getConnectionByIndex(int index) {
		return connections[index];
	}

	public Connection getSingleConnection() {
		return connections[0];
	}

	public void commit() throws SQLException {
		for (Connection conn : connections) {
			conn.commit();
		}
	}

	public void rollBack() throws SQLException {
		for (Connection conn : connections) {
			conn.rollback();
		}
	}

	public void close() throws SQLException {
		for (Connection conn : connections) {
			if (!conn.isClosed()) {
				conn.close();
			}
		}
	}

	public String getPassword(String user) throws Exception {
		Process proc = null;
		Integer exitValue = null;
		String inputStream = null;
		String errorStream = null;

		List<String> args = new ArrayList<String>();

		args.add("get_ora_passwd");
		args.add(user);
		proc = new ProcessBuilder(args).start();

		StringBuffer output = readInputStream(proc, false);
		proc.waitFor();
		exitValue = proc.exitValue();
		if (exitValue == 0) {
			inputStream = output.toString();
		} else {
			output = readInputStream(proc, true);
			errorStream = output.toString();
			throw new RuntimeException(errorStream);
		}
		if(proc != null){
			proc.destroy();
			proc = null;
		}
		return inputStream.toString();
	}

	private static StringBuffer readInputStream(Process proc, boolean error) throws IOException {
		InputStream is;
		if (error) {
			is = proc.getErrorStream();
		} else {
			is = proc.getInputStream();
		}

		int len = 0;
		byte buf[] = new byte[10000];
		StringBuffer output = new StringBuffer();

		while ((len = is.read(buf)) != -1) {
			String str = new String(buf, 0, len);
			output.append(str);
		}
		if (is != null) {
			is.close();
		}
		return output;
	}

}
