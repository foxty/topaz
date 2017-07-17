package com.github.foxty.topaz.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.foxty.topaz.dao.meta.ModelMeta;

/**
 * Model registration and cache model mappings(Column mapping and Relation
 * mapping)
 * <p>
 * Created by itian on 6/22/2017.
 */
public class Models {

	private static Log log = LogFactory.getLog(Models.class);
	private static Models INSTANCE = new Models();

	public static Models getInstance() {
		return INSTANCE;
	}

	private Map<String, ModelMeta> modelMetaMap = new ConcurrentHashMap<>();

	private Models() {
	}

	public void register(Class<?>... modelClazzs) {
		for (Class<?> modelClazz : modelClazzs) {
			String key = modelClazz.getName();
			if (!modelMetaMap.containsKey(key)) {
				ModelMeta existMapping = modelMetaMap.putIfAbsent(modelClazz.getName(), new ModelMeta(modelClazz));
				if (null != existMapping) {
					log.warn("Duplicate registration for model " + modelClazz);
				}
			}
		}
	}

	public ModelMeta getModelMeta(Class<?> modelClazz) {
		if (!modelMetaMap.containsKey(modelClazz.getName())) {
			register(modelClazz);
		}
		return modelMetaMap.get(modelClazz.getName());
	}

	public String genDDL(Class<? extends Model> clazz) {
		StringBuffer sb = new StringBuffer();
		return sb.toString();
	}
}
