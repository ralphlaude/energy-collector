package com.energycollector;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = UpdateConsumptionRequest.class)
public interface AbstractUpdateConsumptionRequest {

    @Value.Parameter
    double getTotalAmount();
}
