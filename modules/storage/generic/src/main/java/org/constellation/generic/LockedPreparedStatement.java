/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.generic;

import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.geotoolkit.internal.sql.StatementEntry;

/**
 * A object allowing to acces the sql String of a statement.
 * Dependeing on the driver the stmt.toString() method does not always return the sql String representation.
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class LockedPreparedStatement extends StatementEntry {

    /**
     * The SQL request contained in the prepared statement.
     */
    private final String sql;

    /**
     * Build a new LockedPreparedStatement.
     *
     * @param stmt A java.sql.preparedStatement.
     * @param sql The SQL code contain in the specified statement.
     */
    public LockedPreparedStatement(final PreparedStatement stmt, final String sql) {
        super(stmt);
        this.sql = sql;
    }

    /**
     * Call The setString method on the preparedStatement
     * 
     * @param index the index of the parameter in the prepared statement (select * from table WHERE colum1=? (index=1) AND colum2=? (index=2))
     * @param value the value to insert in the request
     * @throws SQLException
     */
    public void setString(final int index, final String value) throws SQLException {
        statement.setString(index, value);
    }

    /**
     * Call The setInt method on the preparedStatement
     *
     * @param index the index of the parameter in the prepared statement (select * from table WHERE colum1=? (index=1) AND colum2=? (index=2))
     * @param value the value to insert in the request
     * @throws SQLException
     */
    public void setInt(final int index, final int value) throws SQLException {
        statement.setInt(index, value);
    }

    /**
     * Call The setBoolean method on the preparedStatement
     *
     * @param index the index of the parameter in the prepared statement (select * from table WHERE colum1=? (index=1) AND colum2=? (index=2))
     * @param value the value to insert in the request
     * @throws SQLException
     */
    public void setBoolean(final int index, final boolean value) throws SQLException {
        statement.setBoolean(index, value);
    }

    /**
     * Call The setDate method on the preparedStatement
     *
     * @param index the index of the parameter in the prepared statement (select * from table WHERE colum1=? (index=1) AND colum2=? (index=2))
     * @param value the value to insert in the request
     * @throws SQLException
     */
    public void setDate(final int index, final Date value) throws SQLException {
        statement.setDate(index, value);
    }

    /**
     * Call The setNull method on the preparedStatement
     *
     * @param index the index of the parameter in the prepared statement (select * from table WHERE colum1=? (index=1) AND colum2=? (index=2))
     * @param value the value to insert in the request
     * @throws SQLException
     */
    public void setNull(final int index, final int value) throws SQLException {
        statement.setNull(index, value);
    }

    /**
     * Call The execute method on the preparedStatement :
     *
     * Executes the SQL statement in this PreparedStatement object,
     * which may be any kind of SQL statement.
     * Some prepared statements return multiple results;
     * the execute method handles these complex statements as well as the simpler
     * form of statements handled by the methods executeQuery and executeUpdate.
     *
     * @throws SQLException
     */
    public void execute() throws SQLException {
        statement.execute();
    }

    /**
     * Call The executeUpdate method on the preparedStatement :
     *
     * Executes the SQL statement in this PreparedStatement object,
     * which must be an SQL Data Manipulation Language (DML) statement,
     * such as INSERT, UPDATE or DELETE;
     * or an SQL statement that returns nothing, such as a DDL statement.
     * @throws SQLException
     */
    public void executeUpdate() throws SQLException {
        statement.executeUpdate();
    }

    /**
     * Call The executeQuery method on the preparedStatement :
     *
     * Executes the SQL query in this PreparedStatement object and returns the ResultSet object generated by the query.
     *
     * @return a ResultSet object that contains the data produced by the query; never null
     * @throws SQLException
     */
    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }

    /**
     * call The getParameterMetaData method on the preparedStatement :
     *
     * Retrieves the number, types and properties of this PreparedStatement object's parameters.
     *
     * @return a ParameterMetaData object that contains information about the number, types and properties for each parameter marker of this PreparedStatement object
     * @throws SQLException
     */
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return statement.getParameterMetaData();
    }

    /**
     * Return the sql code associated with the preparedStatement.
     *
     * @return the sql code associated with the preparedStatement.
     */
    public String getSql() {
        return sql;
    }
}
