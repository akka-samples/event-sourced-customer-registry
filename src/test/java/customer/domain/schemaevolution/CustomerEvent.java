package customer.domain.schemaevolution;

import customer.domain.Address;
import akka.javasdk.annotations.Migration;
import akka.javasdk.annotations.TypeName;

import java.util.Optional;

public sealed interface CustomerEvent {

  @TypeName("internal-customer-created")
  record CustomerCreatedOld(String email, String name, String street, String city) implements CustomerEvent {
  }

  @TypeName("internal-name-changed")
  @Migration(NameChangedMigration.class) // <1>
  record NameChanged(String newName, Optional<String> oldName, String reason) implements CustomerEvent {
  }

  @TypeName("internal-address-changed")
  @Migration(AddressChangedMigration.class)
  record AddressChanged(Address newAddress) implements CustomerEvent {
  }
}
