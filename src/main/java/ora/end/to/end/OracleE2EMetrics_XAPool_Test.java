package ora.end.to.end;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.sql.XAConnection;
import javax.sql.XADataSource;

import oracle.jdbc.driver.OracleConnection;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolXADataSource;

//select module, action, elapsed_time, cpu_time, executions, LAST_LOAD_TIME, sql_text from v$sql where module = 'MODULE_KEY'

/*
* https://docs.oracle.com/en/database/oracle/oracle-database/18/jjdbc/JDBC-DMS-Metrics.html#GUID-601B7FA6-A11A-4927-A0AD-77AB6F5CF896
* 
* There are two kinds of metrics, end-to-end metrics and Dynamic Monitoring Service (DMS) metrics.
* End-to-end metrics are used for tagging application activity from the entry into the application
* code through JDBC to the database and back. DMS metrics are used to measure the performance of 
* application components. Customer use of end-to-end metrics in JDBC is generally discouraged
* 
* To disable DMS - System.setProperty( "oracle.dms.console.DMSConsole", "oracle.dms.instrument_stub.DMSConsole"); 
* 
*/
public class OracleE2EMetrics_XAPool_Test {

	final static Map<String, XADataSource> map = new HashMap<>();

	static UniversalConnectionPoolManager ucpm;

	static {
		//You can enable logging by setting the oracle.jdbc.Trace=true system property
		System.setProperty("oracle.jdbc.Trace", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		consoleHandler.setFormatter(new SimpleFormatter());

		Logger app1 = Logger.getLogger("oracle.ucp"); // If you want to log everything just create logger with empty
														// string
		app1.setLevel(Level.FINEST);
		app1.addHandler(consoleHandler);

		Logger app2 = Logger.getLogger("com.arjuna");
		app2.setLevel(Level.FINEST);
		app2.addHandler(consoleHandler);
	}

	public static void printStatistics(PoolDataSource ds) throws Exception {
		System.out.println("----------------------------------------------------------");
		System.out.println(ds.getConnectionPoolName());
		System.out.println(ds.getStatistics());
		System.out.println("----------------------------------------------------------");
	}

	public static synchronized XADataSource getOracleXA(String user, String password) throws SQLException, Exception {
		// Get XADataSource
		// OracleXADataSource xaDS1 = new OracleXADataSource();
		if (ucpm == null) {
			ucpm = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
		}

		if (map.get(user) != null) {
			return map.get(user);
		}

		PoolXADataSource xaDS1 = PoolDataSourceFactory.getPoolXADataSource();

		// set XADataSource with information for connection to happen
		xaDS1.setURL("jdbc:oracle:thin:@localhost:1521/orcl");
		xaDS1.setConnectionFactoryClassName("oracle.jdbc.xa.client.OracleXADataSource");
		xaDS1.setUser(user);
		xaDS1.setPassword(password);
		xaDS1.setInitialPoolSize(2);
		xaDS1.setMinPoolSize(2);
		xaDS1.setMaxPoolSize(5);
		xaDS1.setConnectionPoolName(user + "-Arjuna");
		/*
		 * UCP supports all the pool statistics to be in the form of Dynamic Monitoring
		 * Service (DMS) metrics. You must include the dms.jar file in the class path of
		 * the application to collect and utilize these DMS metrics.
		 * 
		 * UCP supports DMS metrics collection in both the pool manager interface and
		 * the pool manager MBean. You can use the
		 * UnversalConnectionPoolManager.startMetricsCollection method to start
		 * collecting DMS metrics for the specified connection pool instance, and use
		 * the UnversalConnectionPoolManager.stopMetricsCollection method to stop DMS
		 * metrics collection. The metrics update interval can be specified using the
		 * UnversalConnectionPoolManager.setMetricUpdateInterval method. The pool
		 * manager MBean exports similar operations.
		 * 
		 * DMS metrics enable application and system developers to measure and export
		 * customized performance metrics for specific software components. All DMS
		 * metrics are available in the following DMS-enabled JAR files:
		 * 
		 * ojdbc6dms.jar ojdbc6dms_g.jar ojdbc7dms.jar ojdbc7dms_g.jar
		 */
		//https://docs.oracle.com/en/database/oracle/oracle-database/18/jjdbc/JDBC-DMS-Metrics.html#GUID-6C1D625D-4797-46BC-95C9-E3E235E799EB
		Properties properties = new Properties();
		//properties.put("DMSStatementMetrics", Boolean.TRUE.toString());
		properties.put("SQLText", Boolean.TRUE.toString());
		properties.put("oracle.jdbc.DMSStatementMetrics", Boolean.TRUE.toString());
		xaDS1.setConnectionProperties(properties);

		XAConnection xaCon = xaDS1.getXAConnection();

		if (Proxy.isProxyClass(xaCon.getClass())) {
			InvocationHandler ihandle = Proxy.getInvocationHandler(xaCon);
			System.out.println(xaCon + "->Proxy->" + ihandle + " [XAConnection : " + (xaCon instanceof XAConnection)
					+ ", Connection :" + (xaCon instanceof Connection) + "]");
		}

		Connection con = xaCon.getConnection();
		ResultSet rs = con.prepareStatement("select CURRENT_DATE from dual").executeQuery();
		while (rs.next()) {
			System.out.println("++++++++++++++++++++++ " + rs.getString(1));
		}
		rs.close();
		con.close();
		// Add to map so that we don't create multiple times
		map.put(user, xaDS1);

		//start metrics
		//ucpm.startMetricsCollection(xaDS1.getConnectionPoolName());
		//ucpm.setMetricUpdateInterval(1);
		return xaDS1;
	}

	public static void destroy() throws UniversalConnectionPoolException {
		for (String key : map.keySet()) {
			PoolXADataSource pool = (PoolXADataSource) map.get(key);
			ucpm.destroyConnectionPool(pool.getConnectionPoolName());
		}
	}

	private static String OracleKeyName = OracleConnection.OCSID_NAMESPACE + OracleConnection.CLIENT_INFO_KEY_SEPARATOR;
	//////////////// Applying Metrics to Connection////////////

	public static void main(String[] args) throws Exception {
		XADataSource xaDs = getOracleXA("karta", "karta");
		Connection con = xaDs.getXAConnection().getConnection();
		Properties properties = new Properties();
		properties.setProperty(OracleKeyName + OracleConnection.OCSID_ACTION_KEY, "MyAction3"); // Any value that is
																									// identify action
		properties.setProperty(OracleKeyName + OracleConnection.OCSID_CLIENTID_KEY, "MyClientID3");
		properties.setProperty(OracleKeyName + OracleConnection.OCSID_ECID_KEY, "MyECID3");
		properties.setProperty(OracleKeyName + OracleConnection.OCSID_MODULE_KEY, "MyModule3");
		properties.setProperty(OracleKeyName + OracleConnection.OCSID_DBOP_KEY, "MyDBOP3");
		properties.setProperty(OracleKeyName + OracleConnection.OCSID_SEQUENCE_NUMBER_KEY, "0");
		con.setClientInfo(properties);


		DatabaseMetaData meta = con.getMetaData();
		System.out.println(meta.getDatabaseMajorVersion());
		System.out.println(meta.getDriverMajorVersion());

		con.getClientInfo().list(System.out);

		Statement statement = con.createStatement();
		ResultSet rs = statement
				.executeQuery("select sysdate, sys_context('USERENV','ACTION'), sys_context('USERENV','MODULE'), "
						+ "sys_context('USERENV','CLIENT_IDENTIFIER') from dual");

		rs.next();
		System.out.println(rs.getString(1) + " – " + rs.getString(2) + ", " + rs.getString(3) + ", " + rs.getString(4));
		rs.close();

		statement.close();
		// Once execution is done cleanup the properties, else previous values are
		// retained on that connection
		// properties.setProperty( OracleKeyName + OracleConnection.OCSID_ACTION_KEY ,
		// ""); //empty string
		con.close();
		destroy();
	}

}
