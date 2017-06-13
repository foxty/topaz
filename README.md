# topaz

A TinyMVC Framework

- Convention over Configuration
- Embedded MySQL DAO
- FreeMarker Support
- JSON support

## Configuration in web.xml

	<listener>
		<listener-class>org.apache.commons.fileupload.servlet.FileCleanerCleanup</listener-class>
	</listener>

	<filter>
		<display-name>CoreFilter</display-name>
		<filter-name>CoreFilter</filter-name>
		<filter-class>CoreFilter</filter-class>
		<init-param>
			<param-name>configFile</param-name>
			<param-value>/var/kaigongbao/data/config/config.properties</param-value>
		</init-param>
		<init-param>
			<param-name>controllerBase</param-name>
			<param-value>/<Classpath of Controllers></param-value>
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

## Controller Usage

## DAO Usage
