package com.energycollector.persistence;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

final class EnergyCollectorEventTag {

    static final AggregateEventTag<AbstractEnergyCollectorEvent> EVENT_AGGREGATE_EVENT_TAG = AggregateEventTag.of(AbstractEnergyCollectorEvent.class);
}
