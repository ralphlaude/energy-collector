package com.energycollector.persistence;

import com.energycollector.Consumption;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.PVector;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.startServer;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CounterRepositoryTest {

    private static ServiceTest.Setup setup;
    private static ServiceTest.TestServer testServer;
    private AtomicInteger offset;

    @BeforeClass
    public static void beforeAll() {
        setup = defaultSetup().withCassandra(true)
                .configureBuilder(b ->
                        // by default, cassandra-query-journal delays propagation of events by 10sec. In test we're using
                        // a 1 node cluster so this delay is not necessary.
                        b.configure("cassandra-query-journal.eventual-consistency-delay", "0").overrides(bind(ReadSide.class).to(ReadSideTestDriver.class))
                );
        testServer = startServer(setup);
    }

    @AfterClass
    public static void afterAll() {
        testServer.stop();
    }

    @Before
    public void restartOffset() {
        offset = new AtomicInteger(1);
    }

    private ReadSideTestDriver testDriver = testServer.injector().instanceOf(ReadSideTestDriver.class);
    private CounterRepository userRepository = testServer.injector().instanceOf(CounterRepository.class);

    @Test
    public void shouldGetAllConsumptionInTheRightPeriod() throws InterruptedException, ExecutionException, TimeoutException {
        long counterId = 1L;
        String villageName = "village";
        double initialAmount = 10.0;
        double deltaAmount = 2.0;
        double totalAmount = 12.0;
        CallbackRegistered counterRegistered = CallbackRegistered.builder().counterId(counterId)
                .villageName(villageName).initialAmount(initialAmount).build();

        Instant consumptionTime = Instant.now();
        Instant consumptionTimeOutsideTheRange = consumptionTime.minus(2, ChronoUnit.HOURS);
        ConsumptionUpdated consumptionUpdatedOne = ConsumptionUpdated.builder().counterId(counterId)
                .villageName(villageName).consumptionAmount(deltaAmount).totalAmount(totalAmount)
                .consumptionTime(consumptionTime).build();
        ConsumptionUpdated consumptionUpdatedTwo = ConsumptionUpdated.builder().counterId(counterId)
                .villageName(villageName).consumptionAmount(deltaAmount).totalAmount(totalAmount + deltaAmount)
                .consumptionTime(consumptionTime.minus(30, ChronoUnit.MINUTES)).build();
        ConsumptionUpdated consumptionUpdatedOutsideTheRange = ConsumptionUpdated.builder().counterId(counterId)
                .villageName(villageName).consumptionAmount(totalAmount - initialAmount).totalAmount(totalAmount)
                .consumptionTime(consumptionTimeOutsideTheRange).build();

        feed(counterRegistered);
        feed(consumptionUpdatedOne);
        feed(consumptionUpdatedTwo);
        feed(consumptionUpdatedOutsideTheRange);


        PVector<Consumption> consumptions = userRepository.consumptionByPeriod(1)
                .toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertThat(consumptions.isEmpty(), is(false));
        assertThat(consumptions.size(), is(1));
        assertThat(consumptions.get(0).getVillageName(), is(villageName));
        assertThat(consumptions.get(0).getConsumption(), is(4.0D));
    }


    private void feed(AbstractEnergyCollectorEvent userEvent) throws InterruptedException, ExecutionException, TimeoutException {
        testDriver.feed(userEvent, Offset.sequence(offset.getAndIncrement()))
                .toCompletableFuture().get(5, TimeUnit.SECONDS);
    }
}
