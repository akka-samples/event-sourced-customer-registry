package customer.domain.schemaevolution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import akka.javasdk.JsonMigration;

import java.util.List;

public class AddressChangedMigration extends JsonMigration {

  @Override
  public int currentVersion() {
    return 1;
  }


  @Override
  public List<String> supportedClassNames() {
    return List.of("customer.domain.CustomerEvent$OldAddressChanged"); // <1>
  }

  @Override
  public JsonNode transform(int fromVersion, JsonNode json) {
    if (fromVersion < 1) {
      ObjectNode objectNode = ((ObjectNode) json);
      JsonNode oldField = json.get("address"); // <1>
      objectNode.set("newAddress", oldField); // <2>
      objectNode.remove("address"); // <3>
    }
    return json;
  }
}
