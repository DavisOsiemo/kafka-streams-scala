package com.lightbend.kafka.scala.streams

import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.common.utils.Bytes
import ImplicitConversions._


class KGroupedStreamS[K, V](inner: KGroupedStream[K, V]) {

  def count(): KTableS[K, Long] = {
    val c: KTableS[K, java.lang.Long] = inner.count()
    c.mapValues[Long, java.lang.Long, Long](Long2long(_))
  }

  def count(materialized: Materialized[K, Long, KeyValueStore[Bytes, Array[Byte]]]): KTableS[K, Long] = 
    inner.count(materialized)

  def reduce(reducer: (V, V) => V): KTableS[K, V] = {
    val reducerJ: Reducer[V] = (v1: V, v2: V) => reducer(v1, v2)
    inner.reduce(reducerJ)
  }

  def reduce(reducer: (V, V) => V,
    materialized: Materialized[K, V, KeyValueStore[Bytes, Array[Byte]]]): KTableS[K, V] = {

    val reducerJ: Reducer[V] = (v1: V, v2: V) => reducer(v1, v2)
    inner.reduce(reducerJ, materialized)
  }

  def reduce(reducer: (V, V) => V,
    storeName: String): KTableS[K, V] = {

    val reducerJ: Reducer[V] = (v1: V, v2: V) => reducer(v1, v2)
    inner.reduce(reducerJ, Materialized.as[K, V, KeyValueStore[Bytes, Array[Byte]]](storeName))
  }

  def aggregate[VR, SK >: K, SV >: V](initializer: () => VR,
    aggregator: (SK, SV, VR) => VR): KTableS[K, VR] = {

    val initializerJ: Initializer[VR] = () => initializer()
    val aggregatorJ: Aggregator[K, V, VR] = (k: K, v: V, va: VR) => aggregator(k, v, va)
    inner.aggregate(initializerJ, aggregatorJ)
  }

  def aggregate[VR, SK >: K, SV >: V](initializer: () => VR,
    aggregator: (SK, SV, VR) => VR,
    materialized: Materialized[K, VR, KeyValueStore[Bytes, Array[Byte]]]): KTableS[K, VR] = {

    val initializerJ: Initializer[VR] = () => initializer()
    val aggregatorJ: Aggregator[K, V, VR] = (k: K, v: V, va: VR) => aggregator(k, v, va)
    inner.aggregate(initializerJ, aggregatorJ, materialized)
  }
}
