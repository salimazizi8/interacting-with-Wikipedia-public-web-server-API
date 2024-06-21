package cpen221.mp3.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class myRunnable<T extends Cacheable> implements Runnable {

    private ConcurrentMap<T, Long> cacheMap;
    private int timeout;

    // Abstraction Function:
    //  myRunnable represents a child of the Java Runnable interface
    //  that allows us to pass its instance as an argument to a Thread.
    //    - cacheMap represents a map that maps each object to the time it was
    //      inserted into the Cache. The time is in milliseconds, and
    //      demonstrates the the time difference between the current time and
    //      midnight of January 1 of 1970.
    //    - timeOut represents the number of seconds before an object is marked
    //      as stale

    // Representation Invariant:
    //    - cacheMap cannot contain null as its key field.
    //      The value for cacheMap > 0 && value != null
    //    - timeOut > 0



    public myRunnable(ConcurrentMap<T, Long> map_input, int timeout) {
        this.cacheMap = map_input;
        this.timeout = timeout;
    }

    public void run() {
        while (true) {
            for (T objectElement : this.cacheMap.keySet()) {
                if ((System.currentTimeMillis() / 1000)
                        - this.cacheMap.get(objectElement) > this.timeout) {
                    this.cacheMap.remove(objectElement);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
