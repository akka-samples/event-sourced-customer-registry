package customer.domain;


import akka.javasdk.annotations.Migration;
import akka.javasdk.annotations.TypeName;

public sealed interface CustomerEvent {

  @TypeName("internal-customer-created") // <1>
  @Migration(CustomerCreatedMigration.class)
  record CustomerCreated(String email, String name, Address address) implements CustomerEvent {
  }

  @TypeName("internal-name-changed")
  record NameChanged(String newName) implements CustomerEvent {
  }

  @TypeName("internal-address-changed")
  record AddressChanged(Address address) implements CustomerEvent {
  }
}
