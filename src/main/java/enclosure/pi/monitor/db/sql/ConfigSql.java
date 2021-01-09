package enclosure.pi.monitor.db.sql;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.common.Constants;
import enclosure.pi.monitor.common.SharedData;
import enclosure.pi.monitor.db.entity.Config;
import home.db.ColumnType;
import home.db.DBConnection;
import home.db.Database;
import home.db.DbClass;
import home.db.PkCriteria;

public class ConfigSql {

	private static final Logger logger = LogManager.getLogger(ConfigSql.class);

	public boolean createConfigTable() throws SQLException, ClassNotFoundException, IOException {
		logger.info("createConfigTable");

		DBConnection con = null;
		boolean exist = false;
		try {
			con = getConnection();

			exist = tableExist(con, Config.TBL_NAME);	

			logger.debug("Config table exist: " +  exist);
			if (!exist) {
				logger.info("Config table does not exist , creating");
				List<ColumnType> columns = new ArrayList<ColumnType>();					
				columns.add(new ColumnType(Config.ID, true).INT().setPKCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(Config.ENC_TEMP_LIMIT).INT());
				columns.add(new ColumnType(Config.EXTR_AUTO).Boolean());
				columns.add(new ColumnType(Config.EXTR_PPM_LIMIT).INT());
				columns.add(new ColumnType(Config.FIRE_ALARM).Boolean());
				columns.add(new ColumnType(Config.LIGHTS_ON).Boolean());
				columns.add(new ColumnType(Config.SMS_PHONE).VarChar(30));
				columns.add(new ColumnType(Config.ARDUINO_SERIAL).VarChar(30));

				con.createTable(Config.TBL_NAME, columns);	

			}

			if (!exist) {
				logger.info("Config table does not exist, adding default parameters");

				Config config = new Config();
				config.setEncTempLimit(20);
				config.setExtractorAuto(false);
				config.setExtrPPMLimit(0);
				config.setFireAlarmAuto(false);
				config.setLightsOn(false);
				config.setSmsPhoneNumber("");
				config.setArduinoSerialPort("/dev/ttyUSB0");

				add(config);

			}

		}finally {
			if (con != null) {
				con.close();
			}
		}		
		return exist;
	}

	public Config loadConfig() throws ClassNotFoundException, SQLException {

		DBConnection con = null;
		Config config = null;

		int cnt = 0;
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + Config.TBL_NAME  )

					.getSelectResultSet();

			if (rs != null) {
				while(rs.next()) {
					cnt ++;
					config = new Config(rs);
				}
			}
			
			if (cnt > 1) {
				throw new SQLException("Config exceed. Found more than 1 config record on the config table. the count should be 1.  cnt: " + cnt);
			}
		}finally {
			if (con != null)
				con.close();
		}


		return config;
	}
	
	public void updateConfig(Config config) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		try {
			con = getConnection();

			int upd = con.buildUpdateQuery(Config.TBL_NAME)
					.setParameter(Config.ARDUINO_SERIAL, config.getArduinoSerialPort())
					.setParameter(Config.ENC_TEMP_LIMIT, config.getEncTempLimit())
					.setParameter(Config.EXTR_AUTO, config.isExtractorAuto())
					.setParameter(Config.EXTR_PPM_LIMIT, config.getExtrPPMLimit())
					.setParameter(Config.FIRE_ALARM, config.isFireAlarmAuto())
					.setParameter(Config.LIGHTS_ON, config.isLightsOn())
					.setParameter(Config.SMS_PHONE, config.getSmsPhoneNumber())
					.addUpdWhereClause("Where "+Config.ID+" = :idValue", config.getId()).update();

			if (upd < 1) {
				throw new SQLException("Error updating User. " + upd);
			}

		}finally {
			con.close();
		}
	}

	private void add(Config config) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		try {
			con = getConnection();

			con.buildAddQuery(Config.TBL_NAME)
			.setParameter(Config.ENC_TEMP_LIMIT, config.getEncTempLimit())
			.setParameter(Config.EXTR_AUTO, config.isExtractorAuto())
			.setParameter(Config.EXTR_PPM_LIMIT, config.getExtrPPMLimit())
			.setParameter(Config.FIRE_ALARM, config.isFireAlarmAuto())
			.setParameter(Config.LIGHTS_ON, config.isLightsOn())
			.setParameter(Config.SMS_PHONE, config.getSmsPhoneNumber())
			.setParameter(Config.ARDUINO_SERIAL, config.getArduinoSerialPort())
			.add();



		}finally {
			con.close();
		}
	}
	


	private DBConnection getConnection() throws ClassNotFoundException, SQLException{

		boolean prod = SharedData.getInstance().isRunningInProd();
		
		Database db = new Database("jdbc:h2:" + (prod ? Constants.DB_URL : Constants.DB_URL_TESTING) ,Constants.DB_USER, Constants.DB_PASS.toCharArray(), DbClass.H2);
		return new DBConnection(db);

	}
	private boolean tableExist(DBConnection con, String table) throws SQLException {
		DatabaseMetaData md = con.getConnection().getMetaData();
		ResultSet rs = md.getTables(null, null, table.toLowerCase(), null);

		boolean exist = rs.next();

		if (!exist) {
			//check in upper case
			rs = md.getTables(null, null, table.toUpperCase(), null);

			exist = rs.next();
		}

		return exist;
	}

}
