package com.profiler.modifier;

import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;

import com.profiler.modifier.db.cubrid.CubridPreparedStatementModifier;
import com.profiler.modifier.db.cubrid.CubridResultSetModifier;
import com.profiler.modifier.db.cubrid.CubridStatementModifier;
import com.profiler.modifier.db.cubrid.CubridUStatementModifier;
import com.profiler.modifier.db.dbcp.DBCPBasicDataSourceModifier;
import com.profiler.modifier.db.dbcp.DBCPPoolModifier;
import com.profiler.modifier.db.mssql.MSSQLConnectionModifier;
import com.profiler.modifier.db.mssql.MSSQLPreparedStatementModifier;
import com.profiler.modifier.db.mssql.MSSQLResultSetModifier;
import com.profiler.modifier.db.mssql.MSSQLStatementModifier;
import com.profiler.modifier.db.mysql.MySQLConnectionImplModifier;
import com.profiler.modifier.db.mysql.MySQLPreparedStatementModifier;
import com.profiler.modifier.db.mysql.MySQLResultSetModifier;
import com.profiler.modifier.db.mysql.MySQLStatementModifier;
import com.profiler.modifier.db.oracle.OraclePreparedStatementModifier;
import com.profiler.modifier.db.oracle.OracleResultSetModifier;
import com.profiler.modifier.db.oracle.OracleStatementModifier;
import com.profiler.modifier.tomcat.EntryPointStandardHostValveModifier;
import com.profiler.modifier.tomcat.TomcatConnectorModifier;
import com.profiler.modifier.tomcat.TomcatStandardServiceModifier;

public class DefaultModifierRegistry implements ModifierRegistry {
	// TODO 혹시 동시성을 고려 해야 되는지 검토.
	private Map<String, Modifier> registry = new HashMap<String, Modifier>();

	private final ClassPool classPool;

	public DefaultModifierRegistry(ClassPool classPool) {
		this.classPool = classPool;
	}

	@Override
	public Modifier findModifier(String className) {
		return registry.get(className);
	}

	public void addTomcatModifier() {
		Map<String, Modifier> registry = this.registry;
		Modifier entryPointStandardHostValveModifier = new EntryPointStandardHostValveModifier(classPool);
		registry.put("org/apache/catalina/core/StandardHostValve", entryPointStandardHostValveModifier);

		Modifier tomcatStandardServiceModifier = new TomcatStandardServiceModifier(classPool);
		registry.put("org/apache/catalina/core/StandardService", tomcatStandardServiceModifier);

		Modifier tomcatConnectorModifier = new TomcatConnectorModifier(classPool);
		registry.put("org/apache/catalina/connector/Connector", tomcatConnectorModifier);
	}

	public void addJdbcModifier() {
		// TODO 드라이버 존재 체크 로직을 앞단으로 이동 시킬수 없는지 검토
		addMySqlDriver();

		addMsSqlDriver();

		addOracleDriver();

		addCubridDriver();

		addDbcpDriver();
	}

	private void addMySqlDriver() {
		// TODO MySqlDriver는 버전별로 Connection이 interface인지 class인지가 다름. 문제 없는지
		// 확인필요.
		Modifier mysqlConnectionImplModifier = new MySQLConnectionImplModifier(classPool);
		registry.put("com/mysql/jdbc/ConnectionImpl", mysqlConnectionImplModifier);

		Modifier mysqlStatementModifier = new MySQLStatementModifier(classPool);
		registry.put("com/mysql/jdbc/StatementImpl", mysqlStatementModifier);

		Modifier mysqlPreparedStatementModifier = new MySQLPreparedStatementModifier(classPool);
		registry.put("com/mysql/jdbc/PreparedStatement", mysqlPreparedStatementModifier);

		Modifier mysqlResultSetModifier = new MySQLResultSetModifier(classPool);
		registry.put("com/mysql/jdbc/ResultSetImpl", mysqlResultSetModifier);
	}

	private void addMsSqlDriver() {
		Map<String, Modifier> registry = this.registry;
		Modifier mssqlConnectionModifier = new MSSQLConnectionModifier(classPool);
		registry.put("net/sourceforge/jtds/jdbc/ConnectionJDBC2", mssqlConnectionModifier);

		Modifier mssqlStatementModifier = new MSSQLStatementModifier(classPool);
		registry.put("net/sourceforge/jtds/jdbc/JtdsStatement", mssqlStatementModifier);

		Modifier mssqlPreparedStatementModifier = new MSSQLPreparedStatementModifier(classPool);
		registry.put("net/sourceforge/jtds/jdbc/JtdsPreparedStatement", mssqlPreparedStatementModifier);

		Modifier mssqlResultSetModifier = new MSSQLResultSetModifier(classPool);
		registry.put("net/sourceforge/jtds/jdbc/JtdsResultSet", mssqlResultSetModifier);

	}

	private void addOracleDriver() {
		Map<String, Modifier> registry = this.registry;
		// TODO oracle의 경우 connection에 대한 impl이 없음. 확인필요.
		Modifier oraclePreparedStatementModifier = new OraclePreparedStatementModifier(classPool);
		registry.put("oracle/jdbc/driver/OraclePreparedStatement", oraclePreparedStatementModifier);

		Modifier oracleStatement = new OracleStatementModifier(classPool);
		registry.put("oracle/jdbc/driver/OracleStatement", oracleStatement);

		Modifier oracleResultSetModifier = new OracleResultSetModifier(classPool);
		registry.put("oracle/jdbc/driver/OracleResultSetImpl", oracleResultSetModifier);
	}

	private void addCubridDriver() {
		Map<String, Modifier> registry = this.registry;
		// TODO cubrid의 경우도 connection에 대한 impl이 없음. 확인필요.
		Modifier cubridStatementModifier = new CubridStatementModifier(classPool);
		registry.put("cubrid/jdbc/driver/CUBRIDStatement", cubridStatementModifier);

		Modifier cubridPreparedStatementModifier = new CubridPreparedStatementModifier(classPool);
		registry.put("cubrid/jdbc/driver/CUBRIDPreparedStatement", cubridPreparedStatementModifier);

		Modifier cubridResultSetModifier = new CubridResultSetModifier(classPool);
		registry.put("cubrid/jdbc/driver/CUBRIDResultSet", cubridResultSetModifier);

		Modifier cubridUStatementModifier = new CubridUStatementModifier(classPool);
		registry.put("cubrid/jdbc/jci/UStatement", cubridUStatementModifier);
	}

	private void addDbcpDriver() {
		Map<String, Modifier> registry = this.registry;
		// TODO cubrid의 경우도 connection에 대한 impl이 없음. 확인필요.
		Modifier dbcpBasicDataSourceModifier = new DBCPBasicDataSourceModifier(classPool);
		registry.put("org/apache/commons/dbcp/BasicDataSource", dbcpBasicDataSourceModifier);

		Modifier dbcpPoolModifier = new DBCPPoolModifier(classPool);
		registry.put("org/apache/commons/dbcp/PoolingDataSource$PoolGuardConnectionWrapper", dbcpPoolModifier);
	}
}
