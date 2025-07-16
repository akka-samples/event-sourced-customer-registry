package customer.application;

import static akka.Done.done;
import static java.util.concurrent.CompletableFuture.completedFuture;

import akka.Done;
import customer.domain.Customer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple customer store implementation used for idempotent updates documentation.
 */
public class CustomerStore {

  private Map<String, Customer> store = new ConcurrentHashMap<>();

  public CompletionStage<Optional<Customer>> getById(String customerId) {
    return completedFuture(Optional.ofNullable(store.get(customerId)));
  }

  public CompletionStage<Done> save(String customerId, Customer customer) {
    return completedFuture(store.put(customerId, customer)).thenApply(__ -> done());
  }


  public CompletionStage<Collection<Customer>> getAll() {
    return completedFuture(store.values());
  }
}
