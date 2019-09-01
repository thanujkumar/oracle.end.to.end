package ora.end.to.end;


import oracle.jdbc.driver.OracleConnection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

;


public class OracleE2ETest {

    public static final int MODULE_MAX_LENGTH = 48;
    public static final int ACTION_MAX_LENGTH = 32;
    public static final int CLIENT_ID_MAX_LENGTH = 64;
    public static final int ECID_MAX_LENGTH = 64;
    public static final int DBOP_MAX_LENGTH = 29;

    public static void main(String[] args) throws Exception {
        String url = "jdbc:oracle:thin:@localhost:1521/orcl";
        String user = "karta";
        String pass = "karta";

//        System.out.println("please query V$SESSION and hit return to continue when done \n");
//        byte buffer[] = new byte[80];
//        System.in.read(buffer, 0, 80);

        //DriverManager.registerDriver(new OracleDriver());
        Properties prop = new Properties();
        prop.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_VSESSION_PROGRAM, "THANUJ_E2E");
        prop.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_VSESSION_TERMINAL, "IDE");
       // prop.setProperty("oracle.jdbc.DMSStatementMetrics", Boolean.TRUE.toString());
        prop.setProperty(OracleConnection.CONNECTION_PROPERTY_USER_NAME, "karta");
        prop.setProperty(OracleConnection.CONNECTION_PROPERTY_PASSWORD, "karta");

        OracleConnection cnx = (OracleConnection) DriverManager.getConnection(url, prop);

        String OracleKeyName = OracleConnection.OCSID_NAMESPACE + OracleConnection.CLIENT_INFO_KEY_SEPARATOR; //"OCSID.";



//        cnx.setClientInfo(OracleKeyName + OracleConnection.OCSID_ACTION_KEY, StringUtils.right("MyAction", ACTION_MAX_LENGTH));
//        cnx.setClientInfo(OracleKeyName + OracleConnection.OCSID_CLIENTID_KEY, StringUtils.left("MyClientKey", CLIENT_ID_MAX_LENGTH));
//        cnx.setClientInfo(OracleKeyName + OracleConnection.OCSID_ECID_KEY, StringUtils.left("MyECIDKey", ECID_MAX_LENGTH));
//        cnx.setClientInfo(OracleKeyName + OracleConnection.OCSID_MODULE_KEY, StringUtils.right("MyModule", MODULE_MAX_LENGTH));
//        cnx.setClientInfo(OracleKeyName + OracleConnection.OCSID_DBOP_KEY, StringUtils.right("MyDBOP", DBOP_MAX_LENGTH));
//        cnx.setClientInfo(OracleKeyName + OracleConnection.OCSID_SEQUENCE_NUMBER_KEY, Short.toString((short) 0));



        String metrics[] = new String[OracleConnection.END_TO_END_STATE_INDEX_MAX];
        metrics[OracleConnection.END_TO_END_ACTION_INDEX] = "MyAction";
        metrics[OracleConnection.END_TO_END_CLIENTID_INDEX] = "MyClientID";
        metrics[OracleConnection.END_TO_END_MODULE_INDEX] = "MyModule";
        metrics[OracleConnection.END_TO_END_ECID_INDEX] = "MyECID";



        // Set these metrics
        cnx.setEndToEndMetrics(metrics, (short)0);

//        System.out.println("please query V$SESSION and hit return to continue when done, execute statement \n");
//        System.in.read();

//        // Check if the metrics are there
//        PreparedStatement statement = cnx.prepareStatement("select * from regions");
//        //Statement statement = cnx.createStatement();
//        //ResultSet rs = statement.executeQuery("select * from regions");
//        ResultSet rs = statement.executeQuery();
//        while (rs.next()) {
//           // System.out.println(rs.getString(1) + " - " + rs.getString(2));
//        }

        Statement statement = cnx.createStatement();
        ResultSet rs = statement.executeQuery("SELECT userenv('sid'), to_char(sysdate, 'Month.dd.yyyy hh24:mi') FROM dual");
        while (rs.next()) {
            System.out.println("This is session "+rs.getString(1)+" on " + rs.getString(2));
        }


//        System.out.println(Arrays.toString(cnx.getEndToEndMetrics()));
//
//        System.out.println("please query V$SESSION and hit return to continue when done, stmt executed, about to close \n");
//        System.in.read();

        rs.close();
        statement.close();
        cnx.close();

    }

}