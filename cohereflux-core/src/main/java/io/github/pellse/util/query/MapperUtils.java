/*
 * Copyright 2023 Sebastien Pelletier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pellse.util.query;

import java.util.Collection;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.StreamSupport.stream;

public interface MapperUtils {

//    static <T, ID, R, RC extends Collection<R>, EX extends Throwable> Mapper<T, ID, R, EX> oneToOne(
//            CheckedFunction1<List<T>, RC, EX> queryFunction,
//            Function<R, ID> idResolver) {
//
//        return oneToOne(queryFunction, idResolver, id -> null, ArrayList::new, null);
//    }
//
////    static <T, ID, R, RC extends Collection<R>, EX extends Throwable> Mapper<T, ID, R, EX> oneToOne(
////            CheckedFunction1<List<ID>, RC, EX> queryFunction,
////            Function<R, ID> idResolver,
////            MapFactory<ID, R> mapFactory) {
////
////        return oneToOne(queryFunction, idResolver, id -> null, ArrayList::new, mapFactory);
////    }
//
//    static <T, TC extends Collection<T>, ID, R, RC extends Collection<R>, EX extends Throwable> Mapper<T, ID, R, EX> oneToOne(
//            CheckedFunction1<TC, RC, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Supplier<TC> topLevelCollectionFactory) {
//
//        return oneToOne(queryFunction, idResolver, id -> null, topLevelCollectionFactory);
//    }
//
//    static <T, ID, R, RC extends Collection<R>, EX extends Throwable> Mapper<T, ID, R, EX> oneToOne(
//            CheckedFunction1<List<T>, RC, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Function<ID, R> defaultResultProvider) {
//
//        return oneToOne(queryFunction, idResolver, defaultResultProvider, ArrayList::new, null);
//    }
//
//    static <T, ID, R, RC extends Collection<R>, EX extends Throwable> Mapper<T, ID, R, EX> oneToOne(
//            CheckedFunction1<List<T>, RC, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Function<ID, R> defaultResultProvider,
//            MapFactory<ID, R> mapFactory) {
//
//        return oneToOne(queryFunction, idResolver, defaultResultProvider, ArrayList::new, mapFactory);
//    }
//
//    static <T, TC extends Collection<T>, ID, R, RC extends Collection<R>, EX extends Throwable> Mapper<T, ID, R, EX> oneToOne(
//            CheckedFunction1<TC, RC, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Function<ID, R> defaultResultProvider,
//            Supplier<TC> topLevelCollectionFactory) {
//
//        return oneToOne(queryFunction, idResolver, defaultResultProvider, topLevelCollectionFactory, null);
//    }
//
//    @SuppressWarnings("unchecked")
//    static <T, TC extends Collection<T>, ID, R, RC extends Collection<R>, EX extends Throwable> Mapper<T, ID, R, EX> oneToOne(
//            CheckedFunction1<TC, RC, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Function<ID, R> defaultResultProvider,
//            Supplier<TC> topLevelCollectionFactory,
//            MapFactory<ID, R> mapFactory) {
//
//        return convertIdTypeMapperDelegate(entities ->
//                queryOneToOne((TC) entities, queryFunction, idResolver, defaultResultProvider, mapFactory), topLevelCollectionFactory);
//    }
//
//    static <T, ID, R, EX extends Throwable> Mapper<T, ID, List<R>, EX> oneToManyAsList(
//            CheckedFunction1<List<T>, List<R>, EX> queryFunction,
//            Function<R, ID> idResolver) {
//
//        return oneToManyAsList(queryFunction, idResolver, ArrayList::new, null);
//    }
//
//    static <T, ID, R, EX extends Throwable> Mapper<T, ID, List<R>, EX> oneToManyAsList(
//            CheckedFunction1<List<T>, List<R>, EX> queryFunction,
//            Function<R, ID> idResolver,
//            MapFactory<ID, List<R>> mapFactory) {
//
//        return oneToManyAsList(queryFunction, idResolver, ArrayList::new, mapFactory);
//    }
//
//    static <T, TC extends Collection<T>, ID, R, EX extends Throwable> Mapper<T, ID, List<R>, EX> oneToManyAsList(
//            CheckedFunction1<TC, List<R>, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Supplier<TC> topLevelCollectionFactory) {
//
//        return oneToMany(queryFunction, idResolver, ArrayList::new, topLevelCollectionFactory);
//    }
//
//    static <T, TC extends Collection<T>, ID, R, EX extends Throwable> Mapper<T, ID, List<R>, EX> oneToManyAsList(
//            CheckedFunction1<TC, List<R>, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Supplier<TC> topLevelCollectionFactory,
//            MapFactory<ID, List<R>> mapFactory) {
//
//        return oneToMany(queryFunction, idResolver, ArrayList::new, topLevelCollectionFactory, mapFactory);
//    }
//
//    static <T, ID, R, EX extends Throwable> Mapper<T, ID, Set<R>, EX> oneToManyAsSet(
//            CheckedFunction1<Set<T>, Set<R>, EX> queryFunction,
//            Function<R, ID> idResolver) {
//
//        return oneToManyAsSet(queryFunction, idResolver, HashSet::new, null);
//    }
//
//    static <T, ID, R, EX extends Throwable> Mapper<T, ID, Set<R>, EX> oneToManyAsSet(
//            CheckedFunction1<Set<T>, Set<R>, EX> queryFunction,
//            Function<R, ID> idResolver,
//            MapFactory<ID, Set<R>> mapFactory) {
//
//        return oneToManyAsSet(queryFunction, idResolver, HashSet::new, mapFactory);
//    }
//
//    static <T, TC extends Collection<T>, ID, R, EX extends Throwable> Mapper<T, ID, Set<R>, EX> oneToManyAsSet(
//            CheckedFunction1<TC, Set<R>, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Supplier<TC> topLevelCollectionFactory) {
//
//        return oneToMany(queryFunction, idResolver, HashSet::new, topLevelCollectionFactory);
//    }
//
//    static <T, TC extends Collection<T>, ID, R, EX extends Throwable> Mapper<T, ID, Set<R>, EX> oneToManyAsSet(
//            CheckedFunction1<TC, Set<R>, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Supplier<TC> topLevelCollectionFactory,
//            MapFactory<ID, Set<R>> mapFactory) {
//
//        return oneToMany(queryFunction, idResolver, HashSet::new, topLevelCollectionFactory, mapFactory);
//    }
//
//    static <T, ID, R, RC extends Collection<R>, EX extends Throwable> Mapper<T, ID, RC, EX>
//    oneToMany(
//            CheckedFunction1<List<T>, RC, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Supplier<RC> collectionFactory) {
//
//        return oneToMany(queryFunction, idResolver, collectionFactory, ArrayList::new);
//    }
//
//    static <T, TC extends Collection<T>, ID, R, RC extends Collection<R>, EX extends Throwable> Mapper<T, ID, RC, EX>
//    oneToMany(
//            CheckedFunction1<TC, RC, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Supplier<RC> collectionFactory,
//            Supplier<TC> topLevelCollectionFactory) {
//
//        return oneToMany(queryFunction, idResolver, collectionFactory, topLevelCollectionFactory, null);
//    }
//
//    @SuppressWarnings("unchecked")
//    static <T, TC extends Collection<T>, ID, R, RC extends Collection<R>, EX extends Throwable> Mapper<T, ID, RC, EX> oneToMany(
//            CheckedFunction1<TC, RC, EX> queryFunction,
//            Function<R, ID> idResolver,
//            Supplier<RC> collectionFactory,
//            Supplier<TC> topLevelCollectionFactory,
//            MapFactory<ID, RC> mapFactory) {
//
//        return convertIdTypeMapperDelegate(entities ->
//                queryOneToMany((TC) entities, queryFunction, idResolver, collectionFactory, mapFactory), topLevelCollectionFactory);
//    }

    private static <T, TC extends Collection<T>, ID, R, EX extends Throwable> Mapper<T, ID, R, EX> convertIdTypeMapperDelegate(
            Mapper<T, ID, R, EX> mapper, Supplier<TC> topLevelCollectionFactory) {

        return entityIds -> mapper.apply(refineEntityIDType(entityIds, topLevelCollectionFactory));
    }

    private static <T, TC extends Collection<T>> TC refineEntityIDType(Iterable<T> entities, Supplier<TC> topLevelCollectionFactory) {

        return stream(entities.spliterator(), false)
                .collect(toCollection(topLevelCollectionFactory));
    }
}
