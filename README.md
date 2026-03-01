# DIC — Dependency Injection Container

A lightweight dependency injection container built from scratch in Java, inspired by how Spring's IoC container works under the hood. Built as a learning exercise to deeply understand bean lifecycle management, directed acyclic graphs, topological sorting, and Java reflection.

---

## What it does

- Accepts a list of `BeanDefinition` objects describing your classes and their dependencies
- Builds a **directed acyclic graph** (DAG) of dependencies
- Runs **Kahn's topological sort** (O(N+E)) to determine correct instantiation order
- Detects **circular dependencies** and fails fast with a clear error
- Instantiates all beans via reflection in the correct order
- Stores singletons in a typed cache
- Exposes a `getInstance(Class<T>)` method for fetching managed beans

---

## Core concepts

### Bean Lifecycle

```
Register → Build Graph → Topological Sort → Instantiate → Cache → Serve
```

1. **Registration** — bean definitions are passed in describing each class and its dependencies
2. **Graph construction** — an adjacency map is built representing the dependency relationships
3. **Topological sort** — Kahn's algorithm resolves instantiation order; cycle detection happens here before any object is created
4. **Instantiation** — beans are constructed via reflection in guaranteed dependency-first order
5. **Caching** — each instance is stored as a singleton in `Map<Class<?>, Object>`

### Dependency Graph

Dependencies are modeled as a **directed acyclic graph** where an edge from A → B means "A depends on B, so B must be instantiated first."

The graph is stored as two maps:
- `inDegrees` — how many unresolved dependencies each bean has
- `adjacencyList` — reverse map: given a resolved bean, which beans were waiting on it

### Topological Sort — O(N+E)

Uses Kahn's algorithm:
1. Pre-populate a queue with all beans that have zero dependencies
2. Poll a bean, add it to the resolved order, decrement the inDegree of everything that depended on it
3. Any bean whose inDegree hits zero joins the queue
4. If the resolved list is smaller than total beans after the loop — a cycle exists

This separates **analysis** (graph resolution) from **instantiation** completely. No partial object creation, no rollback needed.

### Constructor Injection

- Single constructor → used automatically
- Multiple constructors → must annotate one with `@Inject`
- Constructor must be `public`
- Constructor parameters are resolved from the cache by type

---

## Usage

### 1. Define your classes

```java
public class UserRepository {
    // no dependencies
}

public class UserService {
    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### 2. Create bean definitions

```java
        BeanDefinition<UserService> userServiceBeanDefinition = BeanDefinitionFactory
            .builder(UserService.class)
            .beanName("UserService")
            .build();
```
```java
        userServiceBeanDefinition.setDependencies(
                new ArrayList<>(Arrays.asList(
                    authServiceBeanDefinition,
                    userRepositoryBeanDefinition
                )));
```

### 3. Initialize the container

```java
DIC container = new DIC(definitions);
```

### 4. Fetch beans

```java
UserService userService = container.getInstance(UserService.class);
```

---

## Project structure

```
src/
├── annotations/
│   └── Inject.java          # Constructor injection marker annotation
├── components/
│   └── BeanDefinitionFactory.java             # Factory for Creating BeanDefinitions
│   └── DIC.java             # Core container: graph, sort, instantiation
└── dto/
    └── BeanDefinition.java  # Metadata: name, class, dependencies
```

---

## Known limitations / future work

- [ ] **Interface-to-implementation resolution** — `getInstance(UserRepository.class)` when the cache key is `UserRepositoryImpl.class`
- [ ] **Bean scopes** — currently everything is singleton; prototype scope (fresh instance per request) not yet supported
- [ ] **Classpath scanning** — beans must be registered manually; annotation + reflection based package scanning not yet implemented
- [ ] **Field and setter injection** — only constructor injection is supported
- [ ] **`@Primary` / `@Qualifier`** — no disambiguation when multiple implementations exist for one interface

---

## What I learned

- How a dependency graph maps naturally to `Map<Class, List<Class>>`
- Why topological sort must complete before instantiation begins (cycle detection, no partial state)
- The difference between a forward adjacency list (bean → its deps) and a reverse one (bean → its dependents) and why Kahn's needs the reverse
- How Java reflection works: `getDeclaredConstructors()`, `getParameterTypes()`, `newInstance()`
- Why `@Retention(RetentionPolicy.RUNTIME)` is required for annotations used with reflection
- The separation between a *bean definition* (metadata) and a *bean instance* (the actual object)
