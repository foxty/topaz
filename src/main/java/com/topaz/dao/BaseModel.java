package com.topaz.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("serial")
public class BaseModel implements Serializable {

	private static Log log = LogFactory.getLog(BaseModel.class);

	private static Map<Class<?>, String> TABLE_NAMES = new HashMap<Class<?>, String>();
	private static Map<Class<?>, Map<String, PropertyMapping>> PROPS_MAPPINGS = new HashMap<Class<?>, Map<String, PropertyMapping>>();

	protected static DaoManager daoMgr = DaoManager.getInstance();
	// Primary Key for object
	@Column
	protected Long id;

	protected static void checkDaoPrepared(Class<?> clazz) {
		boolean isReady = TABLE_NAMES.containsKey(clazz)
				&& PROPS_MAPPINGS.containsKey(clazz);
		if (!isReady) {
			initDao(clazz);
		}
	}

	protected static void initDao(Class<?> clazz) {
		TABLE_NAMES.put(clazz, Utils.camel2flat(clazz.getSimpleName()));
		PROPS_MAPPINGS.put(clazz, extractPropMethods(clazz));
	}

	/**
	 * Extract properties read, write methods and its column name in DB table
	 * 
	 * @return
	 */
	private static Map<String, PropertyMapping> extractPropMethods(
			Class<?> clazz) {
		Map<String, PropertyMapping> result = new HashMap<String, PropertyMapping>();
		Field[] parentFields = BaseModel.class.getDeclaredFields();
		Field[] subFields = clazz.getDeclaredFields();
		Field[] all = new Field[parentFields.length + subFields.length];
		System.arraycopy(parentFields, 0, all, 0, parentFields.length);
		System.arraycopy(subFields, 0, all, parentFields.length,
				subFields.length);
		for (Field f : all) {
			Column c = f.getAnnotation(Column.class);
			if (c != null) {
				String propName = f.getName();
				String columnName = ("".equals(c.name()) ? Utils
						.camel2flat(propName) : c.name());
				String readMethodName = (f.getType() == boolean.class || f.getType() == Boolean.class ? "is"
						: "get")
						+ StringUtils.capitalize(propName);
				String writeMethodName = "set"
						+ StringUtils.capitalize(propName);
				Method readMethod = null;
				Method writeMethod = null;
				try {
					readMethod = clazz
							.getMethod(readMethodName, new Class[] {});
					writeMethod = clazz.getMethod(writeMethodName, f.getType());
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					throw new DaoException(e);
				}
				result.put(propName, new PropertyMapping(columnName, propName,
						readMethod, writeMethod));
			}
		}
		return result;
	}

	public Map<String, PropertyMapping> propsMapping() {
		return PROPS_MAPPINGS.get(this.getClass());
	}

	public BaseModel() {
		this(null);
	}

	public BaseModel(Map<String, Object> props) {

		Class<?> curClazz = this.getClass();
		checkDaoPrepared(curClazz);

		Map<String, PropertyMapping> mapping = PROPS_MAPPINGS.get(curClazz);
		if (props != null && !props.isEmpty()) {
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				String p = entry.getKey();
				Object v = entry.getValue();

				PropertyMapping pm = mapping.get(p);
				try {
					pm.getWriteMethod().invoke(this, v);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					throw new DaoException(e);
				}
			}
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	// Creation methods
	public boolean save() {
		boolean result = false;
		if (getId() != null && getId() != 0L) {
			return update();
		}
		Class<?> clazz = this.getClass();
		Map<String, PropertyMapping> mapping = PROPS_MAPPINGS.get(clazz);

		final StringBuffer insertSql = new StringBuffer("INSERT INTO ");
		final StringBuffer valueSql = new StringBuffer(" VALUES(");
		final List<Object> params = new ArrayList<Object>(mapping.size());

		String tblName = Utils.camel2flat(clazz.getSimpleName());
		insertSql.append(tblName).append(" (");
		for (Map.Entry<String, PropertyMapping> entry : mapping.entrySet()) {
			PropertyMapping pm = entry.getValue();
			Object propValue;
			try {
				propValue = pm.getReadMethod().invoke(this);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new DaoException(e);
			}
			if (propValue != null) {
				insertSql.append(pm.getColumnName()).append(",");
				valueSql.append("?,");
				params.add(propValue);
			}
		}
		insertSql.replace(insertSql.length() - 1, insertSql.length(), ")");
		valueSql.replace(valueSql.length() - 1, valueSql.length(), ")");
		insertSql.append(valueSql);

		result = (Boolean) DaoManager.getInstance().accessDB(new IAccessDB() {

			public Object useDB(Connection conn) {
				Boolean result = false;
				PreparedStatement statement = null;
				ResultSet resultSet = null;
				try {
					statement = conn.prepareStatement(insertSql.toString(),
							Statement.RETURN_GENERATED_KEYS);
					for (int i = 0; i < params.size(); i++) {
						statement.setObject(i + 1, params.get(i));
					}
					result = statement.executeUpdate() == 1;
					resultSet = statement.getGeneratedKeys();
					if (resultSet.next()) {
						id = resultSet.getLong(1);
					}
					return result;
				} catch (SQLException e) {
					log.error(e.getMessage(), e);
					throw new DaoException(e);
				} finally {
					try {
						DbUtils.close(resultSet);
					} catch (SQLException e) {
						log.error(e.getMessage(), e);
					}
					try {
						DbUtils.close(statement);
					} catch (SQLException e) {
						log.error(e.getMessage(), e);
					}
				}
			}

		});
		return result;
	}

	// Read methods

	final static public SQLBuilder find(Class<?> clazz) {
		checkDaoPrepared(clazz);
		Map<String, PropertyMapping> mappings = PROPS_MAPPINGS.get(clazz);
		return new SQLBuilder(clazz, mappings, SQLBuilderType.SELECT);
	}

	final static public <T> T findById(Class<T> clazz, Long id) {
		checkDaoPrepared(clazz);
		SQLBuilder qb = find(clazz);
		return qb.where("id", id).fetchFirst();
	}

	// Update methods
	final public boolean update() {
		if (getId() == null || getId().longValue() == 0L) {
			throw new DaoException(
					"No id specified, this entity is not accociate with DB record!");
		}
		Map<String, PropertyMapping> mapping = PROPS_MAPPINGS.get(this
				.getClass());

		PropertyMapping idMapping = mapping.get("id");
		List<String> columns = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();

		for (Entry<String, PropertyMapping> entry : mapping.entrySet()) {
			PropertyMapping pm = entry.getValue();
			if (pm == idMapping)
				continue;
			Object newValue;
			try {
				newValue = pm.getReadMethod().invoke(this);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new DaoException(e);
			}
			columns.add(pm.getColumnName());
			values.add(newValue);
		}
		SQLBuilder sb = new SQLBuilder(this.getClass(), mapping,
				SQLBuilderType.UPDATE);
		sb.set(columns, values).where("id", getId());
		return sb.update() > 0;
	}

	final public boolean increase(String prop) {
		Class<?> clazz = this.getClass();
		SQLBuilder sb = new SQLBuilder(clazz, PROPS_MAPPINGS.get(clazz),
				SQLBuilderType.UPDATE);
		sb.inc(prop, 1).where("id", getId());
		return sb.update() > 0;
	}

	// Deletion methods
	final static public SQLBuilder delete(Class<?> clazz) {
		checkDaoPrepared(clazz);
		Map<String, PropertyMapping> mappings = PROPS_MAPPINGS.get(clazz);
		SQLBuilder sb = new SQLBuilder(clazz, mappings, SQLBuilderType.DELETE);
		return sb;
	}
}

class PropertyMapping {
	private String columnName;
	private String propertyName;
	private Method readMethod;
	private Method writeMethod;

	public PropertyMapping(String cName, String pName, Method rMethod,
			Method wMethod) {
		columnName = cName;
		propertyName = pName;
		readMethod = rMethod;
		writeMethod = wMethod;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Method getReadMethod() {
		return readMethod;
	}

	public Method getWriteMethod() {
		return writeMethod;
	}

	public String toString() {
		return "[PropertyMapping: columnName=" + columnName + ", readMethod="
				+ readMethod + ", writeMethod=" + writeMethod + "]";
	}
}