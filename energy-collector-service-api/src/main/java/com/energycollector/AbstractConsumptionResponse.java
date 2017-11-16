package com.energycollector;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;
import org.pcollections.PVector;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = ConsumptionResponse.class)
public interface AbstractConsumptionResponse {

    @Value.Parameter
    PVector<Consumption> getVillageConsumptions();
}
