/*--
 * Copyright (C) 2018 Atol Conseils et Développements.
 * http://www.atolcd.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atolcd.alfresco.repo.patch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.repo.domain.dialect.MySQLInnoDBDialect;
import org.alfresco.repo.domain.dialect.PostgreSQLDialect;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.LogUtil;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class SchemaUpgradeScriptPatch extends AbstractModuleComponent implements AuthenticationUtil.RunAsWork<Object> {
  private static final Log                 logger                                  = LogFactory.getLog(SchemaUpgradeScriptPatch.class);

  /**
   * The placeholder for the configured <code>Dialect</code> class name: <b>${db.script.dialect}</b>
   */
  private static final String              PLACEHOLDER_SCRIPT_DIALECT              = "\\$\\{db\\.script\\.dialect\\}";

  /** The global property containing the default batch size used by --FOREACH */
  private static final String              PROPERTY_DEFAULT_BATCH_SIZE             = "system.upgrade.default.batchsize";
  private static final String              MSG_EXECUTING_GENERATED_SCRIPT          = "schema.update.msg.executing_generated_script";
  private static final String              MSG_EXECUTING_COPIED_SCRIPT             = "schema.update.msg.executing_copied_script";
  private static final String              MSG_EXECUTING_STATEMENT                 = "schema.update.msg.executing_statement";
  private static final String              MSG_OPTIONAL_STATEMENT_FAILED           = "schema.update.msg.optional_statement_failed";
  private static final String              ERR_STATEMENT_FAILED                    = "schema.update.err.statement_failed";
  private static final String              ERR_SCRIPT_NOT_FOUND                    = "schema.update.err.script_not_found";
  private static final String              ERR_STATEMENT_VAR_ASSIGNMENT_BEFORE_SQL = "schema.update.err.statement_var_assignment_before_sql";
  private static final String              ERR_STATEMENT_VAR_ASSIGNMENT_FORMAT     = "schema.update.err.statement_var_assignment_format";
  private static final String              ERR_STATEMENT_TERMINATOR                = "schema.update.err.statement_terminator";
  public static final int                  DEFAULT_LOCK_RETRY_COUNT                = 24;
  public static final int                  DEFAULT_LOCK_RETRY_WAIT_SECONDS         = 5;
  public static final int                  DEFAULT_MAX_STRING_LENGTH               = 1024;

  private DataSource                       dataSource;

  private Dialect                          dialect;
  private Properties                       globalProperties;

  private final ThreadLocal<StringBuilder> executedStatementsThreadLocal           = new ThreadLocal<>();
  private final ResourcePatternResolver    rpr                                     = new PathMatchingResourcePatternResolver(
      this.getClass().getClassLoader());

  private String                           id;
  private String                           scriptUrl;

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setDialect(Dialect dialect) {
    this.dialect = dialect;
  }

  public void setGlobalProperties(Properties globalProperties) {
    this.globalProperties = globalProperties;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setScriptUrl(String script) {
    this.scriptUrl = script;
  }

  @Override
  protected void executeInternal() throws Throwable {
    AuthenticationUtil.runAs(this, AuthenticationUtil.SYSTEM_USER_NAME);
  }

  @Override
  public Object doWork() throws Exception {

    Connection connection = null;
    try {
      // make sure that we AUTO-COMMIT
      connection = dataSource.getConnection();
      connection.setAutoCommit(true);

      // check if the script was successfully executed
      boolean wasSuccessfullyApplied = didPatchSucceed(connection, id);
      if (wasSuccessfullyApplied) {
        // Either the patch was executed before or the system was
        // bootstrapped
        // with the patch bean present.
        return null;
      }
      // it wasn't run and it can be run now
      executeScriptUrl(connection, scriptUrl);

    } catch (Exception e) {
      logger.error(e);
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        logger.warn("Error closing DB connection: " + e.getMessage(), e);
      }
    }
    return null;
  }

  private void executeScriptUrl(Connection connection, String scriptUrl) throws Exception {
    Dialect dialect = this.dialect;
    String dialectStr = dialect.getClass().getSimpleName();
    InputStream scriptInputStream = getScriptInputStream(dialect.getClass(), scriptUrl);
    // check that it exists
    if (scriptInputStream == null) {
      throw AlfrescoRuntimeException.create(ERR_SCRIPT_NOT_FOUND, scriptUrl);
    }
    // write the script to a temp location for future and failure reference
    File tempFile = null;
    try {
      tempFile = TempFileProvider.createTempFile("AlfrescoSchema-" + dialectStr + "-Update-", ".sql");
      ContentWriter writer = new FileContentWriter(tempFile);
      writer.putContent(scriptInputStream);
    } finally {
      try {
        scriptInputStream.close();
      } catch (Throwable e) {
      } // usually a duplicate close
    }
    // now execute it
    String dialectScriptUrl = scriptUrl.replaceAll(PLACEHOLDER_SCRIPT_DIALECT, dialect.getClass().getName());
    // Replace the script placeholders
    executeScriptFile(connection, tempFile, dialectScriptUrl);
  }

  /**
   * Replaces the dialect placeholder in the script URL and attempts to find a file for it. If not found, the dialect hierarchy will be
   * walked until a compatible script is found. This makes it possible to have scripts that are generic to all dialects.
   * 
   * @return Returns an input stream onto the script, otherwise null
   */
  @SuppressWarnings("rawtypes")
  private InputStream getScriptInputStream(Class dialectClazz, String scriptUrl) throws Exception {
    // replace the dialect placeholder
    String dialectScriptUrl = scriptUrl.replaceAll(PLACEHOLDER_SCRIPT_DIALECT, dialectClazz.getName());
    // get a handle on the resource
    Resource resource = rpr.getResource(dialectScriptUrl);
    if (!resource.exists()) {
      // it wasn't found. Get the superclass of the dialect and try again
      Class superClazz = dialectClazz.getSuperclass();
      if (Dialect.class.isAssignableFrom(superClazz)) {
        // we still have a Dialect - try again
        return getScriptInputStream(superClazz, scriptUrl);
      } else {
        // we have exhausted all options
        return null;
      }
    } else {
      // we have a handle to it
      return resource.getInputStream();
    }
  }

  /**
   * @param cfg the Hibernate configuration
   * @param connection the DB connection to use
   * @param scriptFile the file containing the statements
   * @param scriptUrl the URL of the script to report. If this is null, the script is assumed to have been auto-generated.
   */
  private void executeScriptFile(Connection connection, File scriptFile, String scriptUrl) throws Exception {
    final Dialect dialect = this.dialect;

    StringBuilder executedStatements = executedStatementsThreadLocal.get();
    if (executedStatements == null) {
      // Dump the normalized, pre-upgrade Alfresco schema. We keep the
      // file for later reporting.
      /*
       * xmlPreSchemaOutputFile = dumpSchema(this.dialect, TempFileProvider .createTempFile( "AlfrescoSchema-" +
       * this.dialect.getClass().getSimpleName() + "-", "-Startup.xml").getPath(),
       * "Failed to dump normalized, pre-upgrade schema to file.");
       */

      // There is no lock at this stage. This process can fall out if the
      // lock can't be applied.
      // setBootstrapStarted(connection);

      executedStatements = new StringBuilder(8094);
      executedStatementsThreadLocal.set(executedStatements);
    }

    if (scriptUrl == null) {
      LogUtil.info(logger, MSG_EXECUTING_GENERATED_SCRIPT, scriptFile);
    } else {
      LogUtil.info(logger, MSG_EXECUTING_COPIED_SCRIPT, scriptFile, scriptUrl);
    }

    InputStream scriptInputStream = new FileInputStream(scriptFile);
    BufferedReader reader = new BufferedReader(new InputStreamReader(scriptInputStream, "UTF-8"));
    try {
      int line = 0;
      // loop through all statements
      StringBuilder sb = new StringBuilder(1024);
      String fetchVarName = null;
      String fetchColumnName = null;
      boolean doBatch = false;
      int batchUpperLimit = 0;
      int batchSize = 1;
      Map<String, Object> varAssignments = new HashMap<>(13);
      // Special variable assignments:
      if (dialect instanceof PostgreSQLDialect) {
        // Needs 1/0 for true/false
        varAssignments.put("true", "true");
        varAssignments.put("false", "false");
        varAssignments.put("TRUE", "TRUE");
        varAssignments.put("FALSE", "FALSE");
      } else {
        // Needs true/false as strings
        varAssignments.put("true", "1");
        varAssignments.put("false", "0");
        varAssignments.put("TRUE", "1");
        varAssignments.put("FALSE", "0");
      }
      boolean isFunction = false;

      while (true) {
        String sqlOriginal = reader.readLine();
        line++;

        if (sqlOriginal == null) {
          // nothing left in the file
          break;
        }

        // trim it
        String sql = sqlOriginal.trim();
        // Check for variable assignment
        if (sql.startsWith("--ASSIGN:")) {
          if (sb.length() > 0) {
            // This can only be set before a new SQL statement
            throw AlfrescoRuntimeException.create(ERR_STATEMENT_VAR_ASSIGNMENT_BEFORE_SQL, (line - 1), scriptUrl);
          }
          String assignStr = sql.substring(9, sql.length());
          String[] assigns = assignStr.split("=");
          if (assigns.length != 2 || assigns[0].length() == 0 || assigns[1].length() == 0) {
            throw AlfrescoRuntimeException.create(ERR_STATEMENT_VAR_ASSIGNMENT_FORMAT, (line - 1), scriptUrl);
          }
          fetchVarName = assigns[0];
          fetchColumnName = assigns[1];
          continue;
        }
        // Handle looping control
        else if (sql.startsWith("--FOREACH")) {
          // --FOREACH table.column batch.size.property
          String[] args = sql.split("[ \\t]+");
          int sepIndex;
          if (args.length == 3 && (sepIndex = args[1].indexOf('.')) != -1) {
            doBatch = true;
            // Select the upper bound of the table column
            String stmt = "SELECT MAX(" + args[1].substring(sepIndex + 1) + ") AS upper_limit FROM " + args[1].substring(0, sepIndex);
            Object fetchedVal = executeStatement(connection, stmt, "upper_limit", false, line, scriptFile);
            if (fetchedVal instanceof Number) {
              batchUpperLimit = ((Number) fetchedVal).intValue();
              // Read the batch size from the named property
              String batchSizeString = globalProperties.getProperty(args[2]);
              // Fall back to the default property
              if (batchSizeString == null) {
                batchSizeString = globalProperties.getProperty(PROPERTY_DEFAULT_BATCH_SIZE);
              }
              batchSize = batchSizeString == null ? 10000 : Integer.parseInt(batchSizeString);
            }
          }
          continue;
        }
        // Allow transaction delineation
        else if (sql.startsWith("--BEGIN TXN")) {
          connection.setAutoCommit(false);
          continue;
        } else if (sql.startsWith("--END TXN")) {
          connection.commit();
          connection.setAutoCommit(true);
          continue;
        }
        boolean execute = false;
        boolean optional = false;
        // Procedure creation management
        if (sql.startsWith("-- FUNCTION")) {
          isFunction = true;
          continue;
        } else if (sql.startsWith("-- END FUNCTION")) {
          isFunction = false;
          execute = true;
          optional = false;
        }
        // Check for comments
        if ((sql.length() == 0 || sql.startsWith("--") || sql.startsWith("//") || sql.startsWith("/*"))
            && !sql.startsWith("-- END FUNCTION")) {
          if (sb.length() > 0) {
            // we have an unterminated statement
            throw AlfrescoRuntimeException.create(ERR_STATEMENT_TERMINATOR, (line - 1), scriptUrl);
          }
          // there has not been anything to execute - it's just a
          // comment line
          continue;
        }
        // have we reached the end of a statement?
        if (!isFunction && sql.endsWith(";")) {
          sql = sql.substring(0, sql.length() - 1);
          execute = true;
          optional = false;
        } else if (sql.endsWith("(optional)") || sql.endsWith("(OPTIONAL)")) {
          // Get the end of statement
          int endIndex = sql.lastIndexOf(';');
          if (endIndex > -1) {
            sql = sql.substring(0, endIndex);
            execute = true;
            optional = true;
          } else {
            // Ends with "(optional)" but there is no semi-colon.
            // Just take it at face value and probably fail.
          }
        }
        // Add newline
        if (sb.length() > 0) {
          sb.append("\n");
        }
        // Add leading whitespace for formatting
        int whitespaceCount = sqlOriginal.indexOf(sql);
        for (int i = 0; i < whitespaceCount; i++) {
          sb.append(" ");
        }
        // append to the statement being built up
        if (!sql.startsWith("-- END FUNCTION"))
          sb.append(sql);
        // execute, if required
        if (execute) {
          // Now substitute and execute the statement the appropriate
          // number of times
          String unsubstituted = sb.toString();
          for (int lowerBound = 0; lowerBound <= batchUpperLimit; lowerBound += batchSize) {
            sql = unsubstituted;

            // Substitute in the next pair of range parameters
            if (doBatch) {
              varAssignments.put("LOWERBOUND", String.valueOf(lowerBound));
              varAssignments.put("UPPERBOUND", String.valueOf(lowerBound + batchSize - 1));
            }

            // Perform variable replacement using the ${var} format
            for (Map.Entry<String, Object> entry : varAssignments.entrySet()) {
              String var = entry.getKey();
              Object val = entry.getValue();
              sql = sql.replaceAll("\\$\\{" + var + "\\}", val.toString());
            }

            // Handle the 0/1 values that PostgreSQL doesn't
            // translate to TRUE
            if (this.dialect != null && this.dialect instanceof PostgreSQLDialect) {
              sql = sql.replaceAll("\\$\\{TRUE\\}", "TRUE");
            } else {
              sql = sql.replaceAll("\\$\\{TRUE\\}", "1");
            }

            if (this.dialect != null && this.dialect instanceof MySQLInnoDBDialect) {
              // note: enable bootstrap on MySQL 5.5 (eg. for
              // auto-generated SQL, such as JBPM)
              sql = sql.replaceAll("(?i)TYPE=InnoDB", "ENGINE=InnoDB");
            }

            Object fetchedVal = executeStatement(connection, sql, fetchColumnName, optional, line, scriptFile);
            if (fetchVarName != null && fetchColumnName != null) {
              varAssignments.put(fetchVarName, fetchedVal);
            }
          }
          sb.setLength(0);
          fetchVarName = null;
          fetchColumnName = null;
          doBatch = false;
          batchUpperLimit = 0;
          batchSize = 1;
        }
      }
    } finally {
      try {
        reader.close();
      } catch (Throwable e) {
      }
      try {
        scriptInputStream.close();
      } catch (Throwable e) {
      }
    }
  }

  /**
   * Execute the given SQL statement, absorbing exceptions that we expect during schema creation or upgrade.
   * 
   * @param fetchColumnName the name of the column value to return
   */
  private Object executeStatement(Connection connection, String sql, String fetchColumnName, boolean optional, int line, File file)
      throws Exception {
    StringBuilder executedStatements = executedStatementsThreadLocal.get();
    if (executedStatements == null) {
      throw new IllegalArgumentException("The executedStatementsThreadLocal must be populated");
    }

    Statement stmt = connection.createStatement();
    Object ret = null;
    try {
      if (logger.isDebugEnabled()) {
        LogUtil.debug(logger, MSG_EXECUTING_STATEMENT, sql);
      }
      boolean haveResults = stmt.execute(sql);
      // Record the statement
      executedStatements.append(sql).append(";\n\n");
      if (haveResults && fetchColumnName != null) {
        try (ResultSet rs = stmt.getResultSet()) {
          if (!rs.next()) {
            // Get the result value
            ret = rs.getObject(fetchColumnName);
          }
        }
      }
    } catch (SQLException e) {
      if (optional) {
        // it was marked as optional, so we just ignore it
        LogUtil.debug(logger, MSG_OPTIONAL_STATEMENT_FAILED, sql, e.getMessage(), file.getAbsolutePath(), line);
      } else {
        LogUtil.error(logger, ERR_STATEMENT_FAILED, sql, e.getMessage(), file.getAbsolutePath(), line);
        throw e;
      }
    } finally {
      try {
        stmt.close();
      } catch (Exception e) {
        logger.error(e);
      }
    }
    return ret;
  }

  /**
   * @return Returns the number of applied patches
   */
  private boolean didPatchSucceed(Connection connection, String patchId) throws Exception {
    String patchTableName = getAppliedPatchTableName(connection);
    if (patchTableName == null) {
      // Table doesn't exist, yet
      return false;
    }

    if (patchId == null) {
      return false;
    }

    Statement stmt = connection.createStatement();
    try {
      String query = "select succeeded from " + patchTableName + " where id = '" + patchId + "'";
      try (ResultSet rs = stmt.executeQuery(query)) {
        if (!rs.next()) {
          return false;
        }
        return rs.getBoolean(1);
      }
    } finally {
      try {
        stmt.close();
      } catch (Exception e) {
        logger.error("Error during statement closure", e);
      }
    }
  }

  /**
   * @return Returns the name of the applied patch table, or <tt>null</tt> if the table doesn't exist
   */
  private String getAppliedPatchTableName(Connection connection) throws Exception {
    Statement stmt = connection.createStatement();
    try {
      stmt.executeQuery("select * from alf_applied_patch");
      return "alf_applied_patch";
    } catch (Throwable e) {
      // we'll try another table name
    } finally {
      try {
        stmt.close();
      } catch (Throwable e) {
      }
    }
    // for pre-1.4 databases, the table was named differently
    stmt = connection.createStatement();
    try {
      stmt.executeQuery("select * from applied_patch");
      return "applied_patch";
    } catch (Throwable e) {
      // It is not there
      return null;
    } finally {
      try {
        stmt.close();
      } catch (Throwable e) {
      }
    }
  }
}
