package com.energycollector.service;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import com.energycollector.AbstractConsumptionResponse;
import com.energycollector.AbstractCounterInfoResponse;
import com.energycollector.Consumption;
import com.energycollector.EnergyCollectorService;
import com.energycollector.RegisterVillageCounterRequest;
import com.energycollector.UpdateConsumptionRequest;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.PVector;

import java.util.concurrent.TimeUnit;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.startServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class EnergyCollectorServiceTest {

    private static ServiceTest.TestServer server;
    private static ActorSystem actorSystem;
    private static ActorMaterializer actorMaterializer;

    private long counterId;
    private String villageName;
    private double initialAmount;
    private double totalAmount;

    @BeforeClass
    public static void setUpClass() {
        actorSystem = ActorSystem.apply("EnergyCollectorServiceTest");
        actorMaterializer = ActorMaterializer.apply(ActorMaterializerSettings.create(actorSystem), actorSystem);
        server = startServer(defaultSetup().withCassandra(true));
    }

    @AfterClass
    public static void tearDownClass() {
        actorMaterializer.shutdown();
        actorSystem.terminate();

        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void shouldGetCounterInfoAfterCallBack() throws Exception {
        counterId = 1L;
        villageName = "village";
        initialAmount = 10.0D;
        RegisterVillageCounterRequest registerVillageCounterRequest = RegisterVillageCounterRequest.builder().counterId(counterId).villageName(villageName).initialAmount(initialAmount).build();

        EnergyCollectorService energyCollectorService = server.client(EnergyCollectorService.class);
        energyCollectorService.callbackCounter().handleResponseHeader((responseHeader, result) -> {
            assertEquals(responseHeader.status(), 201);
            assertEquals(responseHeader.getHeader("Location").get(), "http://localhost:9000/energycollector/counter?id=" + counterId);
            return result;
        }).invoke(registerVillageCounterRequest).toCompletableFuture().get(3, SECONDS);

        AbstractCounterInfoResponse counterInfoResponse = energyCollectorService.counterInfo(counterId).invoke().toCompletableFuture().get(3, SECONDS);
        assertThat(counterInfoResponse.getId().get(), is(counterId));
        assertThat(counterInfoResponse.getVillageName().get(), is(villageName));
        assertThat(counterInfoResponse.getApiStatus().isPresent(), is(false));
    }

    @Test
    public void shouldGetAllConsumptionAfterUpdateConsumption() throws Exception {
        counterId = 1L;
        villageName = "village";
        initialAmount = 10.0D;
        totalAmount = 12.0D;

        EnergyCollectorService energyCollectorService = server.client(EnergyCollectorService.class);
        RegisterVillageCounterRequest registerVillageCounterRequest = RegisterVillageCounterRequest.builder().counterId(counterId).villageName(villageName).initialAmount(initialAmount).build();
        energyCollectorService.callbackCounter().invoke(registerVillageCounterRequest).toCompletableFuture().get(3, SECONDS);


        UpdateConsumptionRequest updateConsumptionRequest = UpdateConsumptionRequest.builder().totalAmount(totalAmount).build();
        energyCollectorService.updateCounterConsumption(counterId).handleResponseHeader((responseHeader, result) -> {
            assertEquals(responseHeader.status(), 201);
            assertEquals(responseHeader.getHeader("Location").get(), "http://localhost:9000/energycollector/counter?id=" + counterId);
            return result;
        }).invoke(updateConsumptionRequest).toCompletableFuture().get(3, SECONDS);

        TimeUnit.SECONDS.sleep(3);

        AbstractConsumptionResponse consumptionResponse = energyCollectorService.allVillageConsumption(24).invoke().toCompletableFuture().get(3, SECONDS);
        PVector<Consumption> villageConsumptions = consumptionResponse.getVillageConsumptions();
        assertThat(villageConsumptions.isEmpty(), is(false));
        assertThat(villageConsumptions.get(0).getConsumption(), is(2.0D));
        assertThat(villageConsumptions.get(0).getVillageName(), is(villageName));
    }
}
