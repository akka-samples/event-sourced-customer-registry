package customer;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES;

import akka.javasdk.DependencyProvider;
import akka.javasdk.JsonSupport;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import customer.application.CustomerStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Setup
public class Bootstrap implements ServiceSetup {


  private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

  @Override
  public DependencyProvider createDependencyProvider() {
    return DependencyProvider.single(new CustomerStore());
  }


  @Override
  public void onStartup() {
    logger.info("Starting Akka Application");
    JsonSupport.getObjectMapper().configure(FAIL_ON_NULL_CREATOR_PROPERTIES, true); // <1>
  }
}
