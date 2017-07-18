# topaz

A Fullstack MVC Framework

- Convention over Configuration
- Embedded ORM Layer
- FreeMarker Support
- RESTful Support

## 1 minutes to run

- Configuration in web.xml
```
	<filter>
		<display-name>CoreFilter</display-name>
		<filter-name>CoreFilter</filter-name>
		<filter-class>CoreFilter</filter-class>		
		<init-param>
			<param-name>controllerPackage</param-name>
			<param-value>[Classpath of Controllers. e.g. com.yourpackage.controller]</param-value>
		</init-param>
	</filter>
	<filter-mapping>
		<filter-name>CoreFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<servlet>
		<servlet-name>freemarker</servlet-name>
		<servlet-class>freemarker.ext.servlet.FreemarkerServlet</servlet-class>
		<init-param>
			<param-name>TemplatePath</param-name>
			<param-value>/</param-value>
		</init-param>
		<init-param>
			<param-name>NoCache</param-name>
			<param-value>true</param-value>
		</init-param>
		<init-param>
			<param-name>ContentType</param-name>
			<param-value>text/html; charset=UTF-8</param-value>
		</init-param>
		<init-param>
			<param-name>template_update_delay</param-name>
			<param-value>0</param-value>
		</init-param>
		<init-param>
			<param-name>default_encoding</param-name>
			<param-value>UTF-8</param-value>
		</init-param>
		<init-param>
			<param-name>number_format</param-name>
			<param-value>0.##########</param-value>
		</init-param>
		<load-on-startup>1</load-on-startup>
	</servlet>
	<servlet-mapping>
		<servlet-name>freemarker</servlet-name>
		<url-pattern>*.ftl</url-pattern>
	</servlet-mapping>
```

- Write your fist controller
```
package com.yourpackage.controller

@_Controller(uri = "/users")
public class UserController {

	@_Endpoint
	public Json getUser() {
		Map<String, String> user = new HashMap<>();
		user.put("id", 123);
		user.put("name", "Isaac");
		return Json.create(200, "user", user);
	}
}
```



- Write your first model
> Model should exntends com.github.foxty.topaz.dao.Model class and every model has an implicit filed id which used as the primary key.
> We defined a model named with 'User', there should be a table also named with 'user'. 
> By default, topaz will crate a inmemroy db, you can override this configration by change configurations.
```
@_Model
public class User extends Model {
	
	@_Column
	private String email;
	
	@_Column
	private String nick;
	
	@_Column
	private transient String passwd;
	
	//Getter and Setters
	......
}


```