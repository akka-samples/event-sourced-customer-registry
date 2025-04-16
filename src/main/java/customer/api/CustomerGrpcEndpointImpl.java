package customer.api;

import akka.NotUsed;
import akka.grpc.GrpcServiceException;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.GrpcEndpoint;
import akka.javasdk.client.ComponentClient;
import akka.stream.javadsl.Source;
import customer.api.proto.*;
import customer.application.CustomerByEmailView;
import customer.application.CustomerByNameView;
import customer.application.CustomerEntity;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
@GrpcEndpoint // <1>
public class CustomerGrpcEndpointImpl implements CustomerGrpcEndpoint {

  private static final Logger log = LoggerFactory.getLogger(CustomerGrpcEndpointImpl.class);

  private final ComponentClient componentClient;

  public CustomerGrpcEndpointImpl(ComponentClient componentClient) { // <2>
    this.componentClient = componentClient;
  }

  @Override
  public CreateCustomerResponse createCustomer(CreateCustomerRequest in) {
    log.info("gRPC request to create customer: {}", in);
    if (in.getCustomerId().isBlank())
      throw new IllegalArgumentException("Customer id must not be empty");

    componentClient.forEventSourcedEntity(in.getCustomerId())
      .method(CustomerEntity::create)
      .invoke(apiToDomain(in.getCustomer()));

    return CreateCustomerResponse.getDefaultInstance();
  }


  @Override
  public Customer getCustomer(GetCustomerRequest in) {
    if (in.getCustomerId().isBlank())
      throw new GrpcServiceException(
          Status.INVALID_ARGUMENT.augmentDescription("Customer id must not be empty"));

    try {
      var customer = componentClient.forEventSourcedEntity(in.getCustomerId()) // <3>
          .method(CustomerEntity::getCustomer)
          .invoke();

      return domainToApi(customer); // <4>
    } catch (Exception ex) {
      if (ex.getMessage().contains("No customer found for id")) throw new GrpcServiceException(Status.NOT_FOUND);
      else throw new RuntimeException(ex);
    }
  }


  @Override
  public ChangeNameResponse changeName(ChangeNameRequest in) {
    log.info("gRPC request to change customer [{}] name: {}", in.getCustomerId(), in.getNewName());
    componentClient.forEventSourcedEntity(in.getCustomerId())
      .method(CustomerEntity::changeName)
      .invoke(in.getNewName());

    return ChangeNameResponse.getDefaultInstance();
  }

  @Acl(deny = @Acl.Matcher(principal = Acl.Principal.ALL))
  @Override
  public ChangeAddressResponse changeAddress(ChangeAddressRequest in) {
    log.info("gRPC request to change customer [{}] address: {}", in.getCustomerId(), in.getNewAddress());
    componentClient.forEventSourcedEntity(in.getCustomerId())
        .method(CustomerEntity::changeAddress)
        .invoke(apiToDomain(in.getNewAddress()));

    return ChangeAddressResponse.getDefaultInstance();
  }

  // The two methods below are not necessarily realistic since we have the full result in one response,
  // but provides examples of streaming a response
  @Override
  public CustomerList customerByName(CustomerByNameRequest in) {
    var viewCustomerList = componentClient.forView()
        .method(CustomerByNameView::getCustomers)
        .invoke(in.getName());

    var apiCustomers = viewCustomerList.customers().stream().map(this::domainToApi).toList();

    return CustomerList.newBuilder().addAllCustomers(apiCustomers).build();
  }

  @Override
  public CustomerList customerByEmail(CustomerByEmailRequest in) {
    var viewCustomerList = componentClient.forView()
        .method(CustomerByEmailView::getCustomers)
        .invoke(in.getEmail());

    var apiCustomers = viewCustomerList.customers().stream().map(this::domainToApi).toList();
    return CustomerList.newBuilder().addAllCustomers(apiCustomers).build();
  }

  @Override
  public Source<CustomerSummary, NotUsed> customerByEmailStream(CustomerByEmailRequest in) {
    // Shows of streaming consumption of a view, transforming
    // each element and passing along to a streamed response
    var customerSummarySource = componentClient.forView()
        .stream(CustomerByEmailView::getCustomersStream)
        .source(in.getEmail());

    return customerSummarySource.map(c ->
      CustomerSummary.newBuilder()
          .setName(c.name())
          .setEmail(c.email())
          .build());
  }

  // Conversions between the public gRPC API protobuf messages and the internal
  // Java domain classes.
  private customer.domain.Customer apiToDomain(Customer protoCustomer) {
    return new customer.domain.Customer(
        protoCustomer.getEmail(),
        protoCustomer.getName(),
        apiToDomain(protoCustomer.getAddress())
    );
  }

  private customer.domain.Address apiToDomain(Address protoAddress) {
    if (protoAddress == null) return null;
    else {
      return new customer.domain.Address(
          protoAddress.getStreet(),
          protoAddress.getCity()
      );
    }
  }


  private Customer domainToApi(customer.domain.Customer domainCustomer) {
    return Customer.newBuilder()
        .setName(domainCustomer.name())
        .setEmail(domainCustomer.email())
        .setAddress(domainToApi(domainCustomer.address()))
        .build();
  }

  private Address domainToApi(customer.domain.Address domainAddress) {
    if (domainAddress == null) return null;
    else {
      return Address.newBuilder()
          .setCity(domainAddress.city())
          .setStreet(domainAddress.street())
          .build();
    }
  }

  private Customer domainToApi(customer.domain.CustomerRow domainRow) {
    return Customer.newBuilder()
        .setName(domainRow.name())
        .setEmail(domainRow.email())
        .setAddress(domainToApi(domainRow.address()))
        .build();
  }
}
