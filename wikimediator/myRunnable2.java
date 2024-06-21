package cpen221.mp3.wikimediator;

import fastily.jwiki.core.Wiki;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class myRunnable2 implements Runnable {
    private String startPage;
    private String endPage;
    private List<String> listOf = new ArrayList<>();
    Wiki myWiki = new Wiki("en.wikipedia.org");
    WikiMediator wikiMed = new WikiMediator();

    public myRunnable2(String startPage, String endPage) {
        this.startPage = startPage;
        this.endPage = endPage;
    }

    public void run() {
        Duration timeout = Duration.ofSeconds(300);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<String>> handler = executor.submit(new Callable() {
            @Override
            public List<String> call() throws Exception {
                return wikiMed.getPath_logic(startPage, endPage, listOf);
            }
        });

        try {
           handler.get(timeout.toSeconds(), TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            handler.cancel(true);
        }
        executor.shutdownNow();
    }

    public List<String> kk() {
        run();
        return listOf;
    }
}


