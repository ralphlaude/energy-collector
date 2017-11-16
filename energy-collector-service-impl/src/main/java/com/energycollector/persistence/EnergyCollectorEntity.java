package com.energycollector.persistence;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This class represents the energy consumption for a village. It handles corresponding command
 * by issuing corresponding events and changing its state. These events are save and sent to the readside handler.
 *
 * @see AbstractEnergyCollectorCommand
 * @see AbstractEnergyCollectorEvent
 * @see AbstractEnergyCollectorState
 */
public final class EnergyCollectorEntity extends PersistentEntity<AbstractEnergyCollectorCommand, AbstractEnergyCollectorEvent, EnergyCollectorState> {

    private final Logger log = LoggerFactory.getLogger(EnergyCollectorEntity.class);

    @Override
    public final Behavior initialBehavior(Optional<EnergyCollectorState> snapshotState) {
        BehaviorBuilder behaviorBuilder = newBehaviorBuilder(snapshotState.orElse(initialState()));

        behaviorBuilder.setCommandHandler(CallbackRegister.class, (cmd, ctx) -> {
            CallbackRegistered counterRegistered = CallbackRegistered.builder()
                    .counterId(cmd.getCounterId())
                    .villageName(cmd.getVillageName())
                    .initialAmount(cmd.getInitialAmount())
                    .build();
            log.info("Counter registered");
            return ctx.thenPersist(counterRegistered, event -> ctx.reply(Done.getInstance()));
        });

        behaviorBuilder.setCommandHandler(ConsumptionUpdate.class, (cmd, ctx) -> {
            ConsumptionUpdated counterRegistered = ConsumptionUpdated.builder()
                    .counterId(cmd.getCounterId())
                    .villageName(state().getVillageName())
                    .consumptionAmount(cmd.getTotalAmount() - state().getTotalAmount())
                    .totalAmount(cmd.getTotalAmount())
                    .consumptionTime(cmd.getConsumptionTime())
                    .build();
            log.info("Counter consumption updated");
            return ctx.thenPersist(counterRegistered, event -> ctx.reply(Done.getInstance()));
        });

        behaviorBuilder.setReadOnlyCommandHandler(GetCounterInfo.class, (cmd, ctx) -> {
            if(!state().isActive()) {
                log.info("Counter is not registered yet");
                ctx.reply(CounterInfo.builder().build());
            } else {
                log.info("Counter with id is registered - " + state().getCounterId());
                ctx.reply(CounterInfo.builder().counterId(state().getCounterId())
                        .villageName(state().getVillageName()).build()
                );
            }
        });

        behaviorBuilder.setEventHandler(CallbackRegistered.class, event -> {
            log.info("Counter creation is done");
            return EnergyCollectorState.builder().counterId(event.getCounterId())
                    .villageName(event.getVillageName())
                    .consumptionAmount(event.getInitialAmount())
                    .totalAmount(event.getInitialAmount())
                    .build();
        });

        behaviorBuilder.setEventHandler(ConsumptionUpdated.class, event -> {
            log.info("Counter consumption update is done");
            return EnergyCollectorState.builder().counterId(event.getCounterId())
                    .villageName(event.getVillageName())
                    .consumptionAmount(event.getConsumptionAmount())
                    .totalAmount(event.getTotalAmount())
                    .consumptionTime(event.getConsumptionTime())
                    .build();
        });

        return behaviorBuilder.build();
    }


    private EnergyCollectorState initialState() {
        return EnergyCollectorState.builder().villageName("").consumptionAmount(-1.0D)
                .totalAmount(-1.0D).build();
    }

}
