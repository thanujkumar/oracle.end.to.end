package ora.end.to.end;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleDriver;

//select module, action, elapsed_time, cpu_time, executions, LAST_LOAD_TIME, sql_text from v$sql where module = 'OE2E'

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
public class OracleE2EMetrics_Plain_Connection {

	public static void main(String[] args) throws Exception {

		String url = "jdbc:oracle:thin:@localhost:1521/orcl";
		String user = "TESTDB1";
		String pass = "testdb";

		DriverManager.registerDriver(new OracleDriver());
		OracleConnection cnx = (OracleConnection) DriverManager.getConnection(url, user, pass);
		System.out.println(cnx);

		String metrics[] = new String[OracleConnection.END_TO_END_STATE_INDEX_MAX];
		metrics[OracleConnection.END_TO_END_ACTION_INDEX] = "Index2";
		metrics[OracleConnection.END_TO_END_MODULE_INDEX] = "OE2E";
		metrics[OracleConnection.END_TO_END_CLIENTID_INDEX] = "THANUJ";

		// Set these metrics
		cnx.setEndToEndMetrics(metrics, (short) 0);

		// Check if the metrics are there

		Statement statement = cnx.createStatement();
		ResultSet rs = statement
				.executeQuery("select sysdate, sys_context('USERENV','ACTION'), sys_context('USERENV','MODULE'), "
						+ "sys_context('USERENV','CLIENT_IDENTIFIER') from dual");

		rs.next();
		System.out.println(rs.getString(1) + " – " + rs.getString(2) + ", " + rs.getString(3) + ", " + rs.getString(4));
		rs.close();

		statement.close();
		cnx.close();

	}
}
