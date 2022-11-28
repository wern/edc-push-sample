package org.eclipse.dataspaceconnector.extensions.dataplaneselector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.eclipse.dataspaceconnector.dataplane.selector.DataPlaneSelectorService;
import org.eclipse.dataspaceconnector.dataplane.selector.instance.DataPlaneInstanceImpl;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Requires;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Setting;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.system.configuration.Config;

@Requires({DataPlaneSelectorService.class})
public class DataPlaneSelectorConfigurationServiceExtension implements ServiceExtension {

  @Setting public static final String CONFIG_PREFIX = "edc.dataplane.selector";
  @Setting public static final String URL_SUFFIX = "url";
  @Setting public static final String DESTINATION_TYPES_SUFFIX = "destinationtypes";
  @Setting public static final String SOURCE_TYPES_SUFFIX = "sourcetypes";
  @Setting public static final String PROPERTIES_SUFFIX = "properties";
  @Setting public static final String PUBLIC_API_URL_PROPERTY = "publicApiUrl";

  private static final String NAME = "Data Plane Selector Configuration Extension";
  private static final String COMMA = ",";
  private static final String LOG_MISSING_CONFIGURATION =
      NAME + ": Missing configuration for " + CONFIG_PREFIX + ".%s.%s";
  private static final String LOG_SKIP_BC_MISSING_CONFIGURATION =
      NAME + ": Configuration issues. Skip registering of Data Plane Instance '%s'";
  private static final String LOG_REGISTERED =
      NAME
          + ": Registered Data Plane Instance. (id=%s, url=%s, sourceTypes=%s, destinationTypes=%s, properties=<omitted>)";

  private Monitor monitor;
  private DataPlaneSelectorService dataPlaneSelectorService;

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public void initialize(final ServiceExtensionContext serviceExtensionContext) {
    this.dataPlaneSelectorService =
        serviceExtensionContext.getService(DataPlaneSelectorService.class);
    this.monitor = serviceExtensionContext.getMonitor();
    final Config config = serviceExtensionContext.getConfig(CONFIG_PREFIX);

    monitor.info("### Initializing " + this.getClass().getSimpleName());
    config.partition().forEach(this::configureDataPlaneInstance);
  }

  private void configureDataPlaneInstance(final Config config) {
    final String id = config.currentNode();

    final String url = config.getString(URL_SUFFIX, "");
    final List<String> sourceTypes =
        Arrays.stream(config.getString(SOURCE_TYPES_SUFFIX, "").split(COMMA))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .distinct()
            .collect(Collectors.toList());
    final List<String> destinationTypes =
        Arrays.stream(config.getString(DESTINATION_TYPES_SUFFIX, "").split(COMMA))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .distinct()
            .collect(Collectors.toList());
    final String propertiesJson = config.getString(PROPERTIES_SUFFIX, "{}");

    if (url.isEmpty()) {
      monitor.warning(String.format(LOG_MISSING_CONFIGURATION, id, URL_SUFFIX));
    }

    if (sourceTypes.isEmpty()) {
      monitor.warning(String.format(LOG_MISSING_CONFIGURATION, id, SOURCE_TYPES_SUFFIX));
    }

    if (destinationTypes.isEmpty()) {
      monitor.warning(String.format(LOG_MISSING_CONFIGURATION, id, DESTINATION_TYPES_SUFFIX));
    }

    final Map<String, String> properties;
    try {
      ObjectMapper mapper = new ObjectMapper();
      properties = mapper.readValue(propertiesJson, new TypeReference<Map<String, String>>() {});
    } catch (JsonProcessingException e) {
      throw new EdcException(e);
    }

    final boolean missingPublicApiProperty = !properties.containsKey(PUBLIC_API_URL_PROPERTY);
    if (missingPublicApiProperty) {
      monitor.warning(
          String.format(LOG_MISSING_CONFIGURATION, id, PROPERTIES_SUFFIX)
              + "."
              + PUBLIC_API_URL_PROPERTY);
    }

    final boolean invalidConfiguration =
        url.isEmpty() || sourceTypes.isEmpty() || destinationTypes.isEmpty();
    if (invalidConfiguration || missingPublicApiProperty) {
      monitor.warning(String.format(LOG_SKIP_BC_MISSING_CONFIGURATION, id));
      return;
    }

    final DataPlaneInstanceImpl.Builder builder =
        DataPlaneInstanceImpl.Builder.newInstance().id(id).url(url);

    sourceTypes.forEach(builder::allowedSourceType);
    destinationTypes.forEach(builder::allowedDestType);
    properties.forEach(builder::property);

    dataPlaneSelectorService.addInstance(builder.build());

    monitor.debug(
        String.format(
            LOG_REGISTERED,
            id,
            url,
            String.join(", ", sourceTypes),
            String.join(", ", destinationTypes)));
  }
}