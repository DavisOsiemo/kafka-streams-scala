package com.lightbend.kafka.scala.streams

import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.state.WindowStore
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.common.serialization.Serde

import ImplicitConversions._

class TimeWindowedKStreamS[K, V](val inner: TimeWindowedKStream[K, V]) {

  def aggregate[VR](initializer: () => VR,
    aggregator: (K, V, VR) => VR): KTableS[Windowed[K], VR] = {

    val initializerJ: Initializer[VR] = () => initializer()
    val aggregatorJ: Aggregator[K, V, VR] = (k, v, va) => aggregator(k, v, va)
    inner.aggregate(initializerJ, aggregatorJ)
  }

  def aggregate[VR](initializer: () => VR,
    aggregator: (K, V, VR) => VR,
    materialized: Materialized[K, VR, WindowStore[Bytes, Array[Byte]]]): KTableS[Windowed[K], VR] = {

    val initializerJ: Initializer[VR] = () => initializer()
    val aggregatorJ: Aggregator[K, V, VR] = (k, v, va) => aggregator(k, v, va)
    inner.aggregate(initializerJ, aggregatorJ, materialized)
  }

  def count(): KTableS[Windowed[K], Long] = {
    val c: KTableS[Windowed[K], java.lang.Long] = inner.count()
    c.mapValues[Long](Long2long(_))
  }

  def count(store: String, keySerde: Option[Serde[K]] = None): KTableS[Windowed[K], Long] = { 
    val materialized = keySerde.map(k =>
      Materialized.as[K, java.lang.Long, WindowStore[Bytes, Array[Byte]]](store).withKeySerde(k)
    ).getOrElse(
      Materialized.as[K, java.lang.Long, WindowStore[Bytes, Array[Byte]]](store)
    )

    val c: KTableS[Windowed[K], java.lang.Long] = inner.count(materialized)
    c.mapValues[Long](Long2long(_))
  }

  def reduce(reducer: (V, V) => V): KTableS[Windowed[K], V] = {
    val reducerJ: Reducer[V] = (v1: V, v2: V) => reducer(v1, v2)
    inner.reduce(reducerJ)
  }

  def reduce(reducer: (V, V) => V,
    materialized: Materialized[K, V, WindowStore[Bytes, Array[Byte]]]): KTableS[Windowed[K], V] = {

    val reducerJ: Reducer[V] = (v1: V, v2: V) => reducer(v1, v2)
    inner.reduce(reducerJ, materialized)
  }
}
