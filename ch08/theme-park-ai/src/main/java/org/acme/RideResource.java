package org.acme;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.quarkiverse.langchain4j.chroma.ChromaEmbeddingStore;
import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.acme.ai.ThemeParkChatBot;
import org.acme.data.Ride;
import org.acme.data.RideRecord;
import org.acme.data.RideRepository;
import org.acme.rag.DocumentFromText;
import org.acme.times.WaitingTime;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;


@Path("/ride")
public class RideResource {

    @Inject
    RideRepository rideRepository;

    @Inject
    WaitingTime waitingTime;

    @Startup
    @Transactional
    public void populateData() {
        insertRides();
    }

    private void insertRides() {
        Ride r1 = new Ride();
        r1.name = "Oncharted. My Penitence";
        r1.rating = 5.0;

        rideRepository.persist(r1);

        waitingTime.setRandomWaitingTime(r1.name);


        Ride r2 = new Ride();
        r2.name = "Dragon Fun";
        r2.rating = 4.9;

        rideRepository.persist(r2);

        waitingTime.setRandomWaitingTime(r2.name);
    }

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    ChromaEmbeddingStore chromaEmbeddingStore;

    @Inject
    DocumentFromText documentFromText;

    @Startup
    public void ingest() {
        List<Document> documents = documentFromText
                                    .createDocuments(Paths.get("./rides"));
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(chromaEmbeddingStore)
                .embeddingModel(embeddingModel)
                .documentSplitter(
                        recursive(300, 30))
                .build();

        ingestor.ingest(documents);
    }

    @GET
    @Path("/best")
    public RideRecord getBestRide() {
        return rideRepository.getTheBestRideByRatings();
    }

    @GET
    @Path("/waitingtimes")
    public Map<String, Long> getWaitingTimes() {
        return waitingTime.getWaitingTimes();
    }

    @Inject
    ThemeParkChatBot themeParkChatBot;

    @GET
    @Path("/chat/best")
    public String askForTheBest() {
        return this.themeParkChatBot.chat("What is the best ride at the moment?");
    }

    @GET
    @Path("/chat/waiting")
    public String askForWaitingTime() {
        return this.themeParkChatBot.chat("What is the waiting time for Dragon Fun ride?");
    }

    @GET
    @Path("/chat/nomem")
    public String askForBoth() {
        return this.themeParkChatBot.chat("What is the waiting time for that?");
    }

}
