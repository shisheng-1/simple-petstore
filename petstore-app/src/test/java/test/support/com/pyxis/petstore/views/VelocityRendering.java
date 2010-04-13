package test.support.com.pyxis.petstore.views;

import com.pyxis.petstore.ExceptionImposter;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static org.apache.commons.lang.StringUtils.isEmpty;


public class VelocityRendering {

	private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String VELOCITY_CONFIG_FILE_URL_KEY = "velocity.config.url";
	private static final String TEMPLATES_BASE_URL_KEY = "templates.base.url";
	private static final String VIEWS_PROPERTIES_FILENAME = "/views.properties";
	private static final String VELOCITY_EXTENSION = ".vm";

	private static VelocityEngine velocityEngine;
    private static ResourceLoader resourceLoader = new DefaultResourceLoader();

    private final String template;
    private String encoding = DEFAULT_ENCODING;

    private VelocityRendering(String template) {
		this.template = template;
	}

	public static VelocityRendering render(String template) {
		if (velocityEngine == null)
			loadVelocityEngine();
		return new VelocityRendering(template);
	}

	private static void loadVelocityEngine() {
		try {
			VelocityConfigurer velocityConfigurer = new VelocityConfigurer();
            velocityConfigurer.setConfigLocation(getResource(velocityPropertyFileUrl()));
			velocityConfigurer.setResourceLoaderPath(templatesBaseUrl());
			velocityConfigurer.afterPropertiesSet();
			velocityEngine = velocityConfigurer.getVelocityEngine();
		} catch (Exception e) {
			throw ExceptionImposter.imposterize(e);
		}
	}

    private static Resource getResource(final String location) throws IOException {
        return resourceLoader.getResource(location);
    }

    private static String velocityPropertyFileUrl() throws IOException {
        return getViewProperty(VELOCITY_CONFIG_FILE_URL_KEY);
    }

    private static String templatesBaseUrl() throws IOException {
        return getViewProperty(TEMPLATES_BASE_URL_KEY);
    }

    private static String getViewProperty(final String key) throws IOException {
        Properties properties = new Properties();
        properties.load(VelocityRendering.class.getResourceAsStream(VIEWS_PROPERTIES_FILENAME));
        return properties.getProperty(key);
    }

    public void withEncoding(String encoding) {
		if(isEmpty(encoding))
			throw new IllegalArgumentException("Invalid encoding: " + encoding);
		this.encoding = encoding;
	}

    public String using(ModelBuilder modelBuilder) {
        return using(modelBuilder.asMap());
    }

	public String using(Map<String, Object> model) {
        setupTools(model);
        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateFileName(), this.encoding, model);
	}

    private void setupTools(Map<String, Object> model) {
        model.put("base", PathFor.BASE_URL);
        model.put("display", new org.apache.velocity.tools.generic.DisplayTool());
    }

    private String templateFileName() {
		return template + VELOCITY_EXTENSION;
	}
}
