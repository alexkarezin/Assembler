# Assembler
[![Maven Central](https://img.shields.io/maven-central/v/io.github.pellse/reactive-assembler-core.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.pellse/reactive-assembler-core/0.6.0) [![Javadocs](http://javadoc.io/badge/io.github.pellse/reactive-assembler-core.svg)](http://javadoc.io/doc/io.github.pellse/reactive-assembler-core)

[Reactive](https://www.reactivemanifesto.org), functional, type-safe and stateless Java API for efficient implementation of the [API Composition Pattern](https://microservices.io/patterns/data/api-composition.html) (similar to the Service Aggregator pattern) for querying and merging data from multiple data sources/services, with a specific focus on solving the N + 1 query problem.

The Assembler Library internally leverages [Project Reactor](https://projectreactor.io) to implement end to end reactive streams pipelines (e.g. from a REST endpoint to a RSocket based microservice to the database) and keep all reactive streams properties as defined by the [Reactive Manifesto](https://www.reactivemanifesto.org) (Responsive, Resilient, Elastic, Message Driven with back-pressure, non-blocking, etc.)

## Use Cases

The Assembler library can be used in situations where an application needs to access data or functionality that is spread across multiple services. Some common use cases for this pattern include:

1. Data integration: An application may need to access data from multiple sources, such as databases, third-party services, or internal systems. The Assembler library can be used to create a single API that integrates data from all of these sources, providing a single point of access for the application.
2. Microservices architecture: In a microservices architecture, different functionality is provided by different services. The Assembler library can be used to create a single API that combines the functionality of multiple microservices, making it easier for the application to access the services it needs. 
3. Business logic: An application may need to access multiple services to perform a specific business logic, the Assembler library can be used to create a single API that encapsulate all that business logic and make it easier for the application to access it. 
4. Increasing Reusability: When multiple systems are being used in an organization, it is convenient to create a composition of these systems' APIs to be used by multiple internal or external applications. 
5. Legacy systems: An application may need to access data or functionality provided by a legacy system that does not have a modern API. The Assembler library can be used to create a new API that accesses the legacy system's functionality through existing interfaces or middleware. 
6. Combining functionality from different APIs: The Assembler library can be used to create a new API that combines functionality from different existing APIs, making it easier for clients to access the functionality they need. 
7. Creating a single point of entry: The Assembler library can be used to create a single point of entry for different systems, making it easier for clients to access the functionality they need.

## Usage Example
Below is an example of generating transaction information from a list of customers of an online store. Assuming the following fictional data model and api to access different services:
```java
public record Customer(Long customerId, String name) {}
public record BillingInfo(Long id, Long customerId, String creditCardNumber) {}
public record OrderItem(String id, Long customerId, String orderDescription, Double price) {}
public record Transaction(Customer customer, BillingInfo billingInfo, List<OrderItem> orderItems) {}

Flux<Customer> getCustomers(); // call to a  REST or RSocket microservice
Flux<BillingInfo> getBillingInfo(List<Long> customerIds); // Connects to relational database (R2DBC)
Flux<OrderItem> getAllOrders(List<Long> customerIds); // Connects to MongoDB (Reactive Streams Driver)
```
If `getCustomers()` returns 50 customers, instead of having to make one additional call per *customerId* to retrieve each customer's associated `BillingInfo` (which would result in 50 additional network calls, thus the N + 1 queries issue) we can only make 1 additional call to retrieve all at once all `BillingInfo` for all `Customer` returned by `getCustomers()`, idem for `OrderItem`. Since we are working with 3 different and independent data sources, joining data from `Customer`, `BillingInfo` and `OrderItem` into `Transaction` (using *customerId* as a correlation id between all those entities) has to be done at the application level, which is what this library was implemented for.

When using [reactive-assembler-core](https://central.sonatype.com/artifact/io.github.pellse/reactive-assembler-core/0.6.0), here is how we would aggregate multiple reactive data sources and implement the [API Composition Pattern](https://microservices.io/patterns/data/api-composition.html):

```java
import reactor.core.publisher.Flux;
import io.github.pellse.reactive.assembler.Assembler;
import static io.github.pellse.reactive.assembler.AssemblerBuilder.assemblerOf;
import static io.github.pellse.reactive.assembler.RuleMapper.oneToMany;
import static io.github.pellse.reactive.assembler.RuleMapper.oneToOne;
import static io.github.pellse.reactive.assembler.Rule.rule;
    
Assembler<Customer, Flux<Transaction>> assembler = assemblerOf(Transaction.class)
    .withCorrelationIdExtractor(Customer::customerId)
    .withAssemblerRules(
        rule(BillingInfo::customerId, oneToOne(this::getBillingInfo)),
        rule(OrderItem::customerId, oneToMany(OrderItem::id, this::getAllOrders)),
        Transaction::new)
    .build();

Flux<Transaction> transactionFlux = assembler.assemble(getCustomers());
```
In the code snippet above, we first retrieve all customers, then we concurrently retrieve all billing info (in a single query) and all orders (in a single query) associated with all previously retrieved customers (as defined by the assembler rules). We finally aggregate each customer/billing info/list of order items (related by the same customer id) into a `Transaction` object. We end up with a reactive stream (`Flux`) of `Transaction` objects.

## Infinite Stream of Data
In the scenario where we deal with an infinite or very large stream of data e.g. 100 000+ customers, since the Assembler Library needs to completely drain the upstream from `getCustomers()` to gather all the correlation ids (*customerId*), the example above will result in resource exhaustion. The solution is to split the stream into multiple smaller streams and batch the processing of those individual streams. Most reactive libraries already support that concept, below is an example using [Project Reactor](https://projectreactor.io):
```java
Flux<Transaction> transactionFlux = getCustomers()
    .windowTimeout(100, ofSeconds(5))
    .flatMapSequential(assembler::assemble);
```
## Asynchronous Caching
In addition to providing helper functions to define mapping semantics (e.g. `oneToOne()`, `oneToMany()`), the Assembler also provides a caching/memoization mechanism of the down streams through the `cached()` wrapper method.

```java
import reactor.core.publisher.Flux;
import io.github.pellse.reactive.assembler.Assembler;
import static io.github.pellse.reactive.assembler.AssemblerBuilder.assemblerOf;
import static io.github.pellse.reactive.assembler.RuleMapper.oneToMany;
import static io.github.pellse.reactive.assembler.RuleMapper.oneToOne;
import static io.github.pellse.reactive.assembler.Rule.rule;
import static io.github.pellse.reactive.assembler.caching.CacheFactory.cached;
    
var assembler = assemblerOf(Transaction.class)
    .withCorrelationIdExtractor(Customer::customerId)
    .withAssemblerRules(
        rule(BillingInfo::customerId, oneToOne(cached(this::getBillingInfo))),
        rule(OrderItem::customerId, oneToMany(OrderItem::id, cached(this::getAllOrders))),
        Transaction::new)
    .build();
    
var transactionFlux = getCustomers()
    .window(3)
    .flatMapSequential(assembler::assemble);
```

### Auto Cache



### Pluggable Asynchronous Caching Strategy

Overloaded versions of the `cached()` method are also defined to allow plugging your own cache implementation. We can pass an additional parameter of type `CacheFactory` to customize the caching mechanism:
```java
public interface CacheFactory<ID, R, RRC> {
    Cache<ID, R> create(
        Function<Iterable<? extends ID>, Mono<Map<ID, List<R>>>> fetchFunction,
        Context<ID, R, RRC> context);
}

public interface Cache<ID, R> {
    Mono<Map<ID, List<R>>> getAll(Iterable<ID> ids, boolean computeIfAbsent);
    Mono<?> putAll(Map<ID, List<R>> map);
    Mono<?> removeAll(Map<ID, List<R>> map);
    Mono<?> updateAll(Map<ID, List<R>> mapToAdd, Map<ID, List<R>> mapToRemove);
}
```
If no `CacheFactory` parameter is passed to `cached()`, the default implementation will internally return a `Cache` based on `ConcurrentHashMap`.

Below is an example of a few different ways we can explicitly customize the caching mechanism:
```java
import static io.github.pellse.reactive.assembler.AssemblerBuilder.assemblerOf;
import static io.github.pellse.reactive.assembler.RuleMapper.oneToMany;
import static io.github.pellse.reactive.assembler.RuleMapper.oneToOne;
import static io.github.pellse.reactive.assembler.Rule.rule;
import static io.github.pellse.reactive.assembler.Cache.cache;
import static io.github.pellse.reactive.assembler.CacheFactory.cached;
import reactor.core.publisher.Flux;
  
var assembler = assemblerOf(Transaction.class)
    .withIdExtractor(Customer::customerId)
    .withAssemblerRules(
        rule(BillingInfo::customerId, oneToOne(cached(this::getBillingInfo, new HashMap<>()))),
        rule(OrderItem::customerId, oneToMany(cached(this::getAllOrders, cache(HashMap::new)))),
        Transaction::new)
    .build();
```
Overloaded versions of `cached()` and `cache()` are provided to wrap any implementation of `java.util.Map` since it doesn't natively implement the `CacheFactory<ID, R, RRC>` interface mentioned above.

### Third Party Asynchronous Cache Provider Integration

Here is a list of add-on modules that can be used to integrate third party asynchronous caching libraries (more will be added in the future):

| Assembler add-on module | Third party cache library |
| --- | --- |
| [![Maven Central](https://img.shields.io/maven-central/v/io.github.pellse/reactive-assembler-cache-caffeine.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.pellse/reactive-assembler-cache-caffeine/0.6.0) [reactive-assembler-cache-caffeine](https://central.sonatype.com/artifact/io.github.pellse/reactive-assembler-cache-caffeine/0.6.0) | [Caffeine](https://github.com/ben-manes/caffeine) |


Below is an example of using a `CacheFactory` implementation for the [Caffeine](https://github.com/ben-manes/caffeine) library through the `caffeineCache()` helper method from the caffeine add-on module: 
```java
import com.github.benmanes.caffeine.cache.Cache;

import static io.github.pellse.reactive.assembler.AssemblerBuilder.assemblerOf;
import static io.github.pellse.reactive.assembler.RuleMapper.oneToMany;
import static io.github.pellse.reactive.assembler.RuleMapper.oneToOne;
import static io.github.pellse.reactive.assembler.Rule.rule;
import static io.github.pellse.reactive.assembler.CacheFactory.cached;
import static io.github.pellse.reactive.assembler.cache.caffeine.CaffeineCacheFactory.caffeineCache;

import static java.time.Duration.ofMinutes;
import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;

var cacheBuilder = newBuilder()
                .recordStats()
                .expireAfterWrite(ofMinutes(10))
                .maximumSize(1000);

var assembler = assemblerOf(Transaction.class)
    .withIdExtractor(Customer::customerId)
    .withAssemblerRules(
        rule(BillingInfo::customerId, oneToOne(cached(this::getBillingInfo, caffeineCache()))),
        rule(OrderItem::customerId, oneToMany(cached(this::getAllOrders, caffeineCache(cacheBuilder))))),
        Transaction::new)
    .build();
```

## Integration with non-reactive sources
A utility function `toPublisher()` is also provided to wrap non-reactive sources, useful when e.g. calling 3rd party synchronous APIs:
```java
import static io.github.pellse.reactive.assembler.QueryUtils.toPublisher;

List<BillingInfo> getBillingInfo(List<Long> customerIds); // non-reactive source
List<OrderItem> getAllOrders(List<Long> customerIds); // non-reactive source

Assembler<Customer, Flux<Transaction>> assembler = assemblerOf(Transaction.class)
    .withIdExtractor(Customer::customerId)
    .withAssemblerRules(
        rule(BillingInfo::customerId, oneToOne(toPublisher(this::getBillingInfo))),
        rule(OrderItem::customerId, oneToMany(toPublisher(this::getAllOrders))),
        Transaction::new)
    .build();
```

## Kotlin Support
[![Maven Central](https://img.shields.io/maven-central/v/io.github.pellse/reactive-assembler-kotlin-extension.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.pellse/reactive-assembler-kotlin-extension/0.6.0) [reactive-assembler-kotlin-extension](https://central.sonatype.com/artifact/io.github.pellse/reactive-assembler-kotlin-extension/0.6.0)
```kotlin
import io.github.pellse.reactive.assembler.kotlin.assembler
import io.github.pellse.reactive.assembler.kotlin.cached
import io.github.pellse.reactive.assembler.Cache.cache
import io.github.pellse.reactive.assembler.RuleMapper.oneToMany
import io.github.pellse.reactive.assembler.RuleMapper.oneToOne
import io.github.pellse.reactive.assembler.Rule.rule
import io.github.pellse.reactive.assembler.cache.caffeine.CaffeineCacheFactory.caffeineCache

// Example 1:
val assembler = assembler<Transaction>()
    .withIdExtractor(Customer::customerId)
    .withAssemblerRules(
        rule(BillingInfo::customerId, oneToOne(::getBillingInfo.cached())),
        rule(OrderItem::customerId, oneToMany(::getAllOrders.cached(::hashMapOf))),
        ::Transaction
    ).build()
            
// Example 2:
val assembler = assembler<Transaction>()
    .withIdExtractor(Customer::customerId)
    .withAssemblerRules(
        rule(BillingInfo::customerId, oneToOne(::getBillingInfo.cached(cache()))),
        rule(OrderItem::customerId, oneToMany(::getAllOrders.cached(caffeineCache()))),
        ::Transaction
    ).build()
```

## Other Supported Technologies

[Java 8 Stream (synchronous and parallel)](https://github.com/pellse/assembler/tree/master/assembler-core) for cases where full reactive/non-blocking support is not needed:

[![Maven Central](https://img.shields.io/maven-central/v/io.github.pellse/assembler-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.pellse%22%20AND%20a:%22assembler-core%22)
[![Javadocs](http://javadoc.io/badge/io.github.pellse/assembler-core.svg)](http://javadoc.io/doc/io.github.pellse/assembler-core) 

The implementations below are still available, but it is strongly recommended to switch to [reactive-assembler-core](https://github.com/pellse/assembler/tree/master/reactive-assembler-core) as the new reactive support can easily integrate with any external reactive libraries:

1. [![Maven Central](https://img.shields.io/maven-central/v/io.github.pellse/assembler-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.pellse%22%20AND%20a:%22assembler-core%22)
[![Javadocs](http://javadoc.io/badge/io.github.pellse/assembler-core.svg)](http://javadoc.io/doc/io.github.pellse/assembler-core) [CompletableFuture](https://github.com/pellse/assembler/tree/master/assembler-core)
2. [![Maven Central](https://img.shields.io/maven-central/v/io.github.pellse/assembler-flux.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.pellse%22%20AND%20a:%22assembler-flux%22)
[![Javadocs](http://javadoc.io/badge/io.github.pellse/assembler-flux.svg)](http://javadoc.io/doc/io.github.pellse/assembler-flux) [Flux](https://github.com/pellse/assembler/tree/master/assembler-flux)
3. [![Maven Central](https://img.shields.io/maven-central/v/io.github.pellse/assembler-rxjava.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.pellse%22%20AND%20a:%22assembler-rxjava%22)
[![Javadocs](http://javadoc.io/badge/io.github.pellse/assembler-rxjava.svg)](http://javadoc.io/doc/io.github.pellse/assembler-rxjava) [RxJava](https://github.com/pellse/assembler/tree/master/assembler-rxjava)
4. [![Maven Central](https://img.shields.io/maven-central/v/io.github.pellse/assembler-akka-stream.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.pellse%22%20AND%20a:%22assembler-akka-stream%22)
[![Javadocs](http://javadoc.io/badge/io.github.pellse/assembler-akka-stream.svg)](http://javadoc.io/doc/io.github.pellse/assembler-akka-stream) [Akka Stream](https://github.com/pellse/assembler/tree/master/assembler-akka-stream)
5. [![Maven Central](https://img.shields.io/maven-central/v/io.github.pellse/assembler-reactive-stream-operators.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.pellse%22%20AND%20a:%22assembler-reactive-stream-operators%22)
[![Javadocs](http://javadoc.io/badge/io.github.pellse/assembler-reactive-stream-operators.svg)](http://javadoc.io/doc/io.github.pellse/assembler-reactive-stream-operators) [Eclipse MicroProfile Reactive Stream Operators](https://github.com/pellse/assembler/tree/master/assembler-reactive-stream-operators)

You only need to include in your project's build file (maven, gradle) the lib that corresponds to the type of reactive (or non reactive) support needed (Java 8 stream, CompletableFuture, Flux, RxJava, Akka Stream, Eclipse MicroProfile Reactive Stream Operators).

All modules above have dependencies on the following modules:
1. [![Maven Central](https://img.shields.io/maven-central/v/io.github.pellse/assembler-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.pellse%22%20AND%20a:%22assembler-core%22)
[![Javadocs](http://javadoc.io/badge/io.github.pellse/assembler-core.svg)](http://javadoc.io/doc/io.github.pellse/assembler-core) [assembler-core](https://github.com/pellse/assembler/tree/master/assembler-core)
2. [![Maven Central](https://img.shields.io/maven-central/v/io.github.pellse/assembler-util.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.pellse%22%20AND%20a:%22assembler-util%22)
[![Javadocs](http://javadoc.io/badge/io.github.pellse/assembler-util.svg)](http://javadoc.io/doc/io.github.pellse/assembler-util) [assembler-util](https://github.com/pellse/assembler/tree/master/assembler-util)

## What's Next?
See the [list of issues](https://github.com/pellse/assembler/issues) for planned improvements in a near future.
