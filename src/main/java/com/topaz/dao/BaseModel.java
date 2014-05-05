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
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.topaz.common.TopazUtil;
import com.topaz.dao.Prop.Relation;

@SuppressWarnings("serial")
public class BaseModel implements Serializable {

	private static Log log = LogFactory.getLog(BaseModel.class);
	static Map<Class<?>, Map<String, PropMapping>> MODEL_PROPS = new HashMap<Class<?>, Map<String, PropMapping>>();

	// Primary Key for model
	@Prop protected Integer id;

	protected static void prepareModel(Class<?> clazz) {
		boolean isReady = MODEL_PROPS.containsKey(clazz);
		if (!isReady) {
			MODEL_PROPS.put(clazz, extractPropMethods(clazz));
		}
	}

	/**
	 * Extract properties read, write methods and column name
	 * 
	 * @return
	 */
	private static Map<String, PropMapping> extractPropMethods(Class<?> clazz) {
		Map<String, PropMapping> result = new HashMap<String, PropMapping>();
		Field[] parentFields = BaseModel.class.getDeclaredFields();
		Field[] subFields = clazz.getDeclaredFields();
		Field[] all = new Field[parentFields.length + subFields.length];
		System.arraycopy(parentFields, 0, all, 0, parentFields.length);
		System.arraycopy(subFields, 0, all, parentFields.length,
				subFields.length);
		for (Field f : all) {
			Prop prop = f.getAnnotation(Prop.class);
			if (prop != null) {
				String propName = f.getName();
				String readMethodName = (f.getType() == boolean.class
						|| f.getType() == Boolean.class ? "is" : "get")
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
					throw new DaoException(e);
				}
				result.put(propName, new PropMapping(f.getType(), prop,
						propName, readMethod, writeMethod));
			}
		}
		return result;
	}

	// ==================================== instance methods

	public Map<String, PropMapping> propsMapping() {
		return MODEL_PROPS.get(this.getClass());
	}

	public BaseModel() {
		this(null);
	}

	public BaseModel(Map<String, Object> props) {

		Class<?> curClazz = this.getClass();
		prepareModel(curClazz);

		Map<String, PropMapping> mapping = MODEL_PROPS.get(curClazz);
		if (props != null && !props.isEmpty()) {
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				String p = entry.getKey();
				Object v = entry.getValue();

				PropMapping pm = mapping.get(p);
				try {
					pm.getWriteMethod().invoke(this, v);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					throw new DaoException(e);
				}
			}
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	final public void set(String prop, Object newValue) {
		Map<String, PropMapping> mapping = MODEL_PROPS.get(this.getClass());
		PropMapping pm = mapping.get(prop);
		if (null != pm) {
			try {
				pm.getWriteMethod().invoke(this, newValue);
			} catch (Exception e) {
				throw new DaoException(e);
			}
		} else {
			throw new DaoException("No property for " + prop);
		}
	}

	/**
	 * Save model and throw exception if failed.
	 * 
	 * @throws DaoException
	 */
	public void save() {
		if (!saved()) {
			throw new DaoException("Save model failed for " + this);
		}
	}

	/**
	 * Save model and return true/false
	 * 
	 * @return boolean
	 * @throws DaoException
	 */
	public boolean saved() {
		boolean result = false;
		if (getId() != null && getId() != 0) {
			return updated();
		}
		Class<?> clazz = this.getClass();
		Map<String, PropMapping> mapping = MODEL_PROPS.get(clazz);

		final StringBuffer insertSql = new StringBuffer("INSERT INTO ");
		final StringBuffer valueSql = new StringBuffer(" VALUES(");
		final List<Object> params = new ArrayList<Object>(mapping.size());

		String tblName = TopazUtil.camel2flat(clazz.getSimpleName());
		insertSql.append(tblName).append(" (");
		for (Map.Entry<String, PropMapping> entry : mapping.entrySet()) {
			PropMapping pm = entry.getValue();
			if (pm.isTable())
				continue;
			Object propValue;
			try {
				propValue = pm.getReadMethod().invoke(this);
			} catch (Exception e) {
				throw new DaoException(e);
			}
			if (propValue != null) {
				insertSql.append(pm.getTargetName()).append(",");
				valueSql.append("?,");
				params.add(propValue);
			}
		}
		insertSql.replace(insertSql.length() - 1, insertSql.length(), ")");
		valueSql.replace(valueSql.length() - 1, valueSql.length(), ")");
		insertSql.append(valueSql);

		result = (Boolean) DaoManager.getInstance().accessDB(
				new IConnVisitor() {

					public Object visit(Connection conn) {
						Boolean result = false;
						PreparedStatement statement = null;
						ResultSet resultSet = null;
						try {
							statement = conn.prepareStatement(
									insertSql.toString(),
									Statement.RETURN_GENERATED_KEYS);
							for (int i = 0; i < params.size(); i++) {
								statement.setObject(i + 1, params.get(i));
							}
							result = statement.executeUpdate() == 1;
							resultSet = statement.getGeneratedKeys();
							if (resultSet.next()) {
								id = resultSet.getInt(1);
							}
							return result;
						} catch (SQLException e) {
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

	public boolean deleted() {
		ModelDeleteBuilder db = new ModelDeleteBuilder(this.getClass());
		db.where("id", id);
		return db.update() > 0;
	}

	// Read methods

	@SuppressWarnings({ "rawtypes", "unchecked" })
	final static public ModelSelectBuilder find(Class clazz, String... with) {
		prepareModel(clazz);
		return new ModelSelectBuilder(clazz, with);
	}

	final static public ModelSelectBuilder findBySql(
			Class<? extends BaseModel> clazz, String sql, List<Object> sqlParams) {
		prepareModel(clazz);
		return new ModelSelectBuilder(clazz, sql, sqlParams);
	}

	final static public List<Map<String, Object>> findBySql(final String sql,
			final Object... sqlParams) {

		DaoManager mgr = DaoManager.getInstance();
		List<Map<String, Object>> result = mgr.accessDB(new IConnVisitor() {

			public Object visit(Connection conn) throws SQLException {
				QueryRunner runner = new QueryRunner();
				MapListHandler h = new MapListHandler();
				return runner.query(conn, sql, h, sqlParams);
			}
		});
		return result;
	}

	final static public <T> T findById(Class<T> clazz, int id, String... withs) {
		prepareModel(clazz);
		ModelSelectBuilder ms = find(clazz, withs).where("id", id);
		return ms.fetchFirst();
	}

	/**
	 * Update model and return the status
	 * 
	 * @return boolean
	 * @throws DaoException
	 */
	final public boolean updated() {
		if (getId() == null || getId().longValue() == 0L) {
			throw new DaoException(
					"No id specified, this entity is not accociate with DB!");
		}
		ModelUpdateBuilder ub = new ModelUpdateBuilder(this.getClass());

		Map<String, PropMapping> mapping = MODEL_PROPS.get(this.getClass());
		PropMapping idMapping = mapping.get("id");

		for (Entry<String, PropMapping> entry : mapping.entrySet()) {
			PropMapping pm = entry.getValue();
			if (pm == idMapping || pm.isTable())
				continue;
			Object newValue;
			try {
				newValue = pm.getReadMethod().invoke(this);
			} catch (Exception e) {
				throw new DaoException(e);
			}
			ub.set(pm.getPropertyName(), newValue);
		}

		ub.where("id", getId());
		return ub.update() > 0;
	}

	/**
	 * Update methods and throw exception if failed
	 * 
	 * @throws DaoException
	 */
	final public void update() {
		if (!updated()) {
			throw new DaoException("Update model failed for " + this);
		}
	}

	final public boolean increase(String prop) {
		ModelUpdateBuilder sb = new ModelUpdateBuilder(this.getClass());
		sb.inc(prop, 1).where("id", getId());
		return sb.update() > 0;
	}

	final public boolean decrease(String prop) {
		ModelUpdateBuilder sb = new ModelUpdateBuilder(this.getClass());
		sb.dec(prop, 1).where("id", getId());
		return sb.update() > 0;
	}

	/**
	 * Deletion methods
	 * 
	 * @param clazz
	 * @return SQLBuilder
	 */
	final static public ModelDeleteBuilder delete(
			Class<? extends BaseModel> clazz) {
		prepareModel(clazz);
		ModelDeleteBuilder sb = new ModelDeleteBuilder(clazz);
		return sb;
	}
}

class PropMapping {
	private Class<?> type;
	private Prop prop;
	private String propertyName;
	private Method readMethod;
	private Method writeMethod;

	public PropMapping(Class<?> type, Prop prop, String pName, Method rMethod,
			Method wMethod) {
		this.type = type;
		this.prop = prop;
		propertyName = pName;
		readMethod = rMethod;
		writeMethod = wMethod;
	}

	public Class<?> getType() {
		return type;
	}

	boolean isColumn() {
		return prop.type() == Prop.Type.Column;
	}

	boolean isTable() {
		return prop.type() == Prop.Type.Table;
	}

	public Relation getRelation() {
		return prop.relation();
	}

	public String getByKey() {
		if (StringUtils.isBlank(prop.byKey())) {
			return TopazUtil.camel2flat(type.getSimpleName()) + "_id";
		} else {
			return prop.byKey();
		}
	}

	/**
	 * Return target column name or table name
	 * 
	 * @return
	 */
	public String getTargetName() {
		if (StringUtils.isBlank(prop.targetName())) {
			return TopazUtil.camel2flat(isColumn() ? propertyName : type
					.getSimpleName());
		} else {
			return prop.targetName();
		}

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
		return "[ColumnMapping: targetName=" + getTargetName()
				+ ", readMethod=" + readMethod + ", writeMethod=" + writeMethod
				+ "]";
	}
}
