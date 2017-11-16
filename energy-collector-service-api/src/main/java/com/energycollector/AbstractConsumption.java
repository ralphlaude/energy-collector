package com.energycollector;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = Consumption.class)
public interface AbstractConsumption {

    @Value.Parameter
    String getVillageName();

    @Value.Parameter
    double getConsumption();
}
