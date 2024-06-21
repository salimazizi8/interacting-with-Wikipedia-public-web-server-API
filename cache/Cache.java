package cpen221.mp3.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.io.Serializable;

public class Cache<T extends Cacheable> implements Serializable{

    // anything Cacheable should be allowed to be stored in our Cache:
    // a map of some cacheable object to some time it was put into the cache.
    private ConcurrentHashMap<T, Long> cacheMap = new ConcurrentHashMap<>();
    private int timeOut;
    private int cacheCapacity;
    /* the default cache size is 32 objects */
    private static final int DSIZE = 32;
    /* the default timeout value is 3600s */
    private static final int DTIMEOUT = 3600;

    // Abstraction Function:
    // Cache represents a storage place for any object that meets the requirements
    // of being stored in a cache.
    //  - cacheMap represents a map that maps each object to the time it was
    //    inserted into this Cache. The time is in milliseconds, and
    //    demonstrates the the time difference between the current time and
    //    midnight of January 1 of 1970.
    //  - timeOut represents the number of seconds before an object is marked
    //    as stale inside the Cache (which will be evicted)
    //    The default timeOut value is 3600 seconds.
    //  - cacheCapacity represents the maximum number of elements that
    //    that are allowed to be in this Cache at one time. The default cacheCapacity
    //    is 32.


    // Rep Invariants:
    //    - cacheMap cannot contain null as its key field.
    //    - The cacheMap value field > 0 && value != null
    //    - timeOut > 0
    //    - cacheCapacity > 0

    // Thread Safety Arguments:
    // This class is thread safe because it is
    //  - Following Strategy 3 of ensuring Thread Safety, which is that
    //    we used thread-safe mutable data type: a ConcurrentMap called "cacheMap".
    //  - timeOut and cacheCapacity are final
    //  - Following the Strategy 2 for ensuring Thread Safety, timeOut is
    //    an immutable data type.



    /**
     * Create a cache with a fixed capacity and a timeout value.
     * Objects in the cache that have not been refreshed within the timeout period
     * are removed from the cache.
     *
     * @param capacity the number of objects the cache can hold
     * @param timeout  the duration an object should be in the cache before it times out
     */
    public Cache(int capacity, int timeout) {
        this.timeOut = timeout;
        this.cacheCapacity = capacity;
        myRunnable runner = new myRunnable(this.cacheMap, this.timeOut);
        // creating an instance of a Thread class:
        Thread tr1 = new Thread(runner);
        tr1.start();
    }

    /**
     * Create a cache with default capacity and timeout values.
     */
    public Cache() {
        this(DSIZE, DTIMEOUT);
    }

    /**
     * Add a value to the cache.
     * If the cache is full then remove the least recently accessed object to
     * make room for the new object.
     *
     * @param t a value to be placed into the Cache.
     * @return returns true if the value gets successfully put into the Cache,
     * false otherwise.
     */
    public boolean put(T t) {
        // TODO: implement this method

        if (t == null) {
            return false;
        }
        if (this.cacheMap.size() >= this.cacheCapacity) {
            long maxTimeSoFar = 0;
            T objectTobeRemoved = null;
            for (T objectElement : this.cacheMap.keySet()) {
                if (this.cacheMap.get(objectElement) > maxTimeSoFar) {
                    maxTimeSoFar = this.cacheMap.get(objectElement);
                    objectTobeRemoved = objectElement;
                }
            }
            // remove the least recently accessed object (the object with
            // longest time spent):
            if (objectTobeRemoved != null) {
                this.cacheMap.remove(objectTobeRemoved);
            }
            // put a value with a time as its key into the Cache:
            cacheMap.put(t, System.currentTimeMillis());
            return true;
        }
        this.cacheMap.put(t, System.currentTimeMillis());
        return true;
    }

    /** Retrieve the object with the given id
     * If no such object exists in the Cache, throws a checked exception.
     *
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the cache
     * @throws NoSuchElementException if the object with the given id
     * is not found in the cache
     */
    public T get(String id) throws NoSuchElementException {

        List<T> list = new ArrayList<>(this.cacheMap.keySet());
        for (T element : list) {
            if (element.id().equals(id)) {
                return element;
            }
        }
        /* Do not return null. Throw a suitable checked exception when an object
           is not in the cache. */
        throw new NoSuchElementException();  // a checked exception
    }


    /**
     * Update the last refresh time for the object with the provided id.
     * This method is used to mark an object as "not stale" so that its timeout
     * is delayed.
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful and false otherwise
     */
    public boolean touch(String id) {
        /* TODO: Implement this method */
        for (T objectElement : this.cacheMap.keySet()) {
            if (objectElement.id().equals(id)) {
                this.cacheMap.put(objectElement, System.currentTimeMillis());
                return true;
            }
        }
        return false;
    }

    /**
     * Update an object in the cache.
     * This method updates an object and acts like a "touch" to renew the
     * object in the cache.
     *
     * @param t the object to update
     * @return true if successful and false otherwise
     */
    public boolean update(T t) {
        /* TODO: implement this method */
        String touchedID = t.id();
        return this.touch(touchedID);
    }

}
