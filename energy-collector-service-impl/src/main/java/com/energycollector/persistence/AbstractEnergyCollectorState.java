package com.energycollector.persistence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.time.Instant;
import java.util.Optional;

/**
 * Represents the state of an village energy collector.
 */

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = EnergyCollectorState.class)
public interface AbstractEnergyCollectorState extends Jsonable {

    @Value.Default
    default Long getCounterId() {
        return -1L;
    }

    @Value.Parameter
    String getVillageName();

    @Value.Parameter
    double getTotalAmount();

    @Value.Parameter
    double getConsumptionAmount();

    @Value.Parameter
    Optional<Instant> getConsumptionTime();

    default boolean isActive() {
        return getCounterId() != -1L;
    }

}
