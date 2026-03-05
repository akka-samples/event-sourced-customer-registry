package customer.application;

import customer.domain.Customer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple customer store implementation used for idempotent updates documentation.
 */
public class CustomerStore {

  private Map<String, Customer> store = new ConcurrentHashMap<>();

  public Optional<Customer> getById(String customerId) {
    return Optional.ofNullable(store.get(customerId));
  }

  public void save(String customerId, Customer customer) {
    store.put(customerId, customer);
  }


  public Collection<Customer> getAll() {
    return store.values();
  }
}
