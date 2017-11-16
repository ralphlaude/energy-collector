package com.energycollector.persistence;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.energycollector.persistence.AbstractEnergyCollectorCommand;
import com.energycollector.persistence.AbstractEnergyCollectorEvent;
import com.energycollector.persistence.AbstractEnergyCollectorState;
import com.energycollector.persistence.EnergyCollectorEntity;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class EnergyCollectorEntityTest {

    private static ActorSystem system;

    private long counterId;
    private String villageName;
    private double initialAmount;
    private double totalAmount;
    private AbstractEnergyCollectorCommand abstractEnergyCollectorCommand;
    private AbstractEnergyCollectorState energyCollectorState;
    private PersistentEntityTestDriver<AbstractEnergyCollectorCommand, AbstractEnergyCollectorEvent, AbstractEnergyCollectorState> driver;

    @BeforeClass
    public static void startActorSystem() {
        system = ActorSystem.create("EnergyCollectorEntityTest");
    }

    @AfterClass
    public static void shutdownActorSystem() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Before
    public void createTestDriver() {
        counterId = 1L;
        villageName = "village";
        initialAmount = 10.0;
        driver = new PersistentEntityTestDriver(system, new EnergyCollectorEntity(), String.valueOf(counterId));
    }

    @After
    public void noIssues() {
        if (!driver.getAllIssues().isEmpty()) {
            driver.getAllIssues().forEach(System.out::println);
            fail("There were issues " + driver.getAllIssues().get(0));
        }
    }

    @Test
    public void shouldSetCounterInitialValue() throws Exception {
        givenCallBackCommand();
        whenSendingCallBackCommand();
        thenCounterIsInitiated();
    }

    @Test
    public void shouldUpdateCounterConsumption() throws Exception {
        givenConsumptionUpdateCommand();
        whenSendingConsumptionUpdateCommand();
        thenCounterConsumptionIsUpdated();
    }

    private void givenCallBackCommand() {
        abstractEnergyCollectorCommand = CallbackRegister.builder().counterId(counterId).villageName(villageName).initialAmount(initialAmount).build();
    }

    private void givenConsumptionUpdateCommand() {
        totalAmount = 12;
        abstractEnergyCollectorCommand = ConsumptionUpdate.builder().counterId(counterId).totalAmount(totalAmount).consumptionTime(Instant.now()).build();
    }

    private void whenSendingCallBackCommand() {
        PersistentEntityTestDriver.Outcome<AbstractEnergyCollectorEvent, AbstractEnergyCollectorState> outcome = driver.run(abstractEnergyCollectorCommand);
        energyCollectorState = outcome.state();
    }

    private void whenSendingConsumptionUpdateCommand() {
        driver.run(CallbackRegister.builder().counterId(counterId).villageName(villageName).initialAmount(initialAmount).build());
        PersistentEntityTestDriver.Outcome<AbstractEnergyCollectorEvent, AbstractEnergyCollectorState> outcome = driver.run(abstractEnergyCollectorCommand);
        energyCollectorState = outcome.state();
    }

    private void thenCounterIsInitiated() {
        assertThat(energyCollectorState.isActive(), is(true));
        assertThat(energyCollectorState.getCounterId(), is(counterId));
        assertThat(energyCollectorState.getVillageName(), is(villageName));
        assertThat(energyCollectorState.getTotalAmount(), is(initialAmount));
        assertThat(energyCollectorState.getConsumptionAmount(), is(initialAmount));
        assertThat(energyCollectorState.getConsumptionTime().isPresent(), is(false));
    }

    private void thenCounterConsumptionIsUpdated() {
        assertThat(energyCollectorState.isActive(), is(true));
        assertThat(energyCollectorState.getCounterId(), is(counterId));
        assertThat(energyCollectorState.getVillageName(), is(villageName));
        assertThat(energyCollectorState.getTotalAmount(), is(totalAmount));
        assertThat(energyCollectorState.getConsumptionAmount(), is(totalAmount - initialAmount));
        assertThat(energyCollectorState.getConsumptionTime().isPresent(), is(true));
    }
}
