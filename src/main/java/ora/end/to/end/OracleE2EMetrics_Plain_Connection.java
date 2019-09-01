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
 * select * from v$session where schemaname not in 'SYS';
   select MODULE, ACTION, USERNAME, SCHEMANAME,SERIAL#, CLIENT_INFO, PREV_SQL_ID FROM V$SESSION where schemaname not in 'SYS';


   select * from v$sql where service='orcl' AND parsing_schema_name in 'KARTA';
   select MODULE, ACTION, SQL_ID, FETCHES, ELAPSED_TIME from v$sql where service='orcl' AND parsing_schema_name in 'KARTA';
   select elapsed_time , cpu_time, executions , (elapsed_time / executions) as avg_ela_time_microsec from v$sqlarea where sql_id = 'a1xvma17xmckp';

	SELECT 	MODULE, ACTION, SQL_ID,	EXECUTIONS,	ELAPSED_TIME/1000000 TOTAL_ELAPSED_TIME_SEC,
					ELAPSED_TIME/1000000/EXECUTIONS ELAPSED_TIME_SEC_PER_EXEC,
					CPU_TIME/1000000 TOTAL_CPU_TIME_SEC,
					CPU_TIME/1000000/EXECUTIONS CPU_TIME_SEC
	FROM
		V$SQL
	WHERE
		SQL_ID in ('a1xvma17xmckp','b0r80srx88x6v','g1k9ws32uqxgp');
 * 
 */
public class OracleE2EMetrics_Plain_Connection {

    public static void main(String[] args) throws Exception {

        String url = "jdbc:oracle:thin:@localhost:1521/orcl";
        String user = "karta";
        String pass = "karta";

        DriverManager.registerDriver(new OracleDriver());
        OracleConnection cnx = (OracleConnection) DriverManager.getConnection(url, user, pass);
        System.out.println(cnx);

        String metrics[] = new String[OracleConnection.END_TO_END_STATE_INDEX_MAX];
        metrics[OracleConnection.END_TO_END_ACTION_INDEX] = "MyAction2";
        metrics[OracleConnection.END_TO_END_CLIENTID_INDEX] = "MyClientID2";
        metrics[OracleConnection.END_TO_END_MODULE_INDEX] = "MyModule2";
        metrics[OracleConnection.END_TO_END_ECID_INDEX] = "MyECID2";

        // Set these metrics

        cnx.setEndToEndMetrics(metrics, (short) 0);
        //cnx.getClientInfo().setProperty(OracleConnection.OCSID_ACTION_KEY,"THANUJ");

        // Check if the metrics are there

        Statement statement = cnx.createStatement();

        ResultSet rs = statement.executeQuery("select * from regions");
        while (rs.next()) {
            System.out.println(rs.getString(1) + " - " + rs.getString(2));
        }

        statement.close();
        cnx.close();

    }
}
