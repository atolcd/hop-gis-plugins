package org.atolcd.hop.core.row.value;

import java.sql.ResultSetMetaData;

import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.variables.IVariables;

import org.locationtech.jts.geom.Geometry;
/**
 * Created by Sudhanshu-Tango on 1/13/2017.
 */
public interface GeometryInterface {
  Geometry getGeometry(Object object) throws HopValueException;

IValueMeta getValueFromSqlType(IVariables variables, DatabaseMeta databaseMeta, String name, ResultSetMetaData rm, int index,
		boolean ignoreLength, boolean lazyConversion) throws HopDatabaseException;
}
