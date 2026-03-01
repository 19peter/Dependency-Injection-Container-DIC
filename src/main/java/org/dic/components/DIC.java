package org.dic.components;

import org.dic.annotations.Inject;
import org.dic.dto.BeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


//How to construct a Dep Graph ?
//1-Linked List ? : We create nodes for each Bean and Edges that point to their dependencies
//We resolve the bean that has no dependencies first
//If two nodes point to a resolved node, we resolve the one with the least dependencies first
//Cant be used because it's linear

//2- B-Tree ? Similar to LinkedList, we do depth first search and resolve ? But in a B-Tree
//a node has only one parent

//Directed Acyclic Graph (DAG) - Using Topological Sort to resolve nodes
//with in-degree of 0 first


public class DIC {
    List<BeanDefinition<?>> beanDefinitions;
    Queue<BeanDefinition<?>> queue;
    Map<Class<?>, Object> cache;
    Set<String> resolved;
    Map<String, Integer> inDegrees;
    Map<String, List<String>> adjacencyList;
    Map<String, BeanDefinition<?>> beanDefinitionMap;
    LinkedList<BeanDefinition<?>> resolvedOrder;

    public DIC(List<BeanDefinition<?>> beanDefinitionsList) {
        queue = new ArrayDeque<>();
        cache = new HashMap<>();

        inDegrees = new HashMap<>();
        adjacencyList = new HashMap<>();
        beanDefinitionMap = new HashMap<>();
        resolved = new HashSet<>();
        resolvedOrder = new LinkedList<>();

        this.beanDefinitions = beanDefinitionsList;
        for (BeanDefinition<?> bd : beanDefinitions) {
            beanDefinitionMap.put(bd.getName(), bd);
        }

        topologicalSortV3();
        construct();

    }



    private void resolutionV1() {
        //Loop until Bean Definitions List is empty
        while (!beanDefinitions.isEmpty()) {
            //Loop over each element, find the one with no dep -> add to queue and remove
            //from other dependencies
            //Use Iterator to safely modify the list
            Iterator<BeanDefinition<?>> iterator = beanDefinitions.iterator();
            boolean isRemoved = false;

            while (iterator.hasNext()) {
                BeanDefinition<?> bd = iterator.next();
                if (bd.getDependencies().isEmpty()) {
                    queue.add(bd);
                    iterator.remove();
                    isRemoved = true;
                    //Loop over BeanDefinitions and remove this dependency from their lists
                    for (BeanDefinition<?> bd2 : beanDefinitions) {
                        bd2.getDependencies().removeIf(dep -> dep.equals(bd.getName()));
                    }
                    break;
                }

            }
            if (!isRemoved) throw new StackOverflowError("Cyclic Dependency Detected");

        }
    }

    //O(N^2 + E)
    private void resolutionV2() { //O(N^2 + E)
        while (resolved.size() != beanDefinitions.size()) { //O(N)
            boolean isRemoved = false;

            for (BeanDefinition bd : beanDefinitions) { //O(N)
                if (resolved.contains(bd.getName())) continue;

                if (bd.getDependencies().isEmpty()) {
                    queue.add(bd);
                    resolved.add((bd.getName()));
                    isRemoved = true;

                } else {
                    AtomicBoolean allResolved = new AtomicBoolean(true);

                    bd.getDependencies().forEach(d -> { // O(E)
                        if (!resolved.contains(d)) {
                            allResolved.set(false);
                        }
                    });

                    if (allResolved.get()) {
                        queue.add(bd);
                        resolved.add((bd.getName()));
                        isRemoved = true;
                    }
                }

            }
            if (!isRemoved) throw new RuntimeException("Cyclic Dependency Detected");

        }
    }


    // O(2(N+E)) -> O(N+E)
    private void topologicalSortV3() {
        //Calculate in-degrees for each Bean
        //Add 0-degree bean to queue, decrement from in-degrees, use adjacency list as lookup
        for (BeanDefinition<?> bd : beanDefinitions) { //O(N)
            int dependencySize = bd.getDependencies().size();
            if (dependencySize == 0) {
                queue.add(bd);
            }
            inDegrees.put(bd.getName(), bd.getDependencies().size());

            bd.getDependencies().forEach(dep -> { //O(E)
                //Dependency -> Dependent
                adjacencyList.computeIfAbsent(dep.getName(), k -> new ArrayList<>()).add(bd.getName());
            });
        }

        while (!queue.isEmpty()) { //O(N)
            BeanDefinition<?> bd = queue.poll();
            resolvedOrder.add(bd);
            List<String> dependents = adjacencyList.getOrDefault(bd.getName(), new ArrayList<>());
            for (String dependent : dependents) { //O(E)
                inDegrees.computeIfPresent(dependent, (k,v) -> v - 1);
                if (inDegrees.get(dependent) == 0) queue.add(beanDefinitionMap.get(dependent));
            }
        }

        if (resolvedOrder.size() != beanDefinitions.size()) throw new RuntimeException("Cyclic Dependency Detected");
    }

    private void construct() {
        resolvedOrder.forEach(bd -> {
            List<Constructor<?>> constructors = Arrays.stream(bd.getBeanClass().getDeclaredConstructors()).toList();
            Constructor<?> ctor = null;

            if (constructors.isEmpty()) throw new RuntimeException("Class has no constructor");
            else {
                if (constructors.size() == 1) ctor = constructors.getFirst();
                else {
                    ctor = constructors.stream()
                            .filter(c -> c.isAnnotationPresent(Inject.class))
                            .findFirst()
                            .orElseThrow(() ->
                                    new RuntimeException("No constructor annotated with @Inject found"));
                }
            }

            if (!Modifier.isPublic(ctor.getModifiers())) throw new RuntimeException("Constructors Must be public");

            List<Class<?>> dependencies = List.of(ctor.getParameterTypes());

            if (dependencies.isEmpty()) createInstanceAndSave(ctor, new ArrayList<>(), bd.getBeanClass());
            else {
                List<Object> cachedDependenciesInstances = extractDependencies(dependencies);
                createInstanceAndSave(ctor, cachedDependenciesInstances, bd.getBeanClass());
            }
        });
    }

    private List<Object> extractDependencies(List<Class<?>> dependencies) {
        List<Object> cachedDependenciesInstances = new ArrayList<>();
        dependencies.forEach(d -> {
            Object cachedInstance = cache.get(d);
            cachedDependenciesInstances.add(cachedInstance);
        });
        return cachedDependenciesInstances;
    }

    private void createInstanceAndSave(Constructor<?> ctor, List<Object> cachedDependenciesInstances, Class<?> className) {
        try {
            Object instance = ctor.newInstance(cachedDependenciesInstances.toArray());
            cache.put(className, instance);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public <T> T getInstance(Class<T> clazz) {
        Object instance = cache.get(clazz);

        if (instance == null) {
            throw new RuntimeException("No bean found for " + clazz.getName());
        }

        return clazz.cast(instance); // runtime-safe cast
    }
}


