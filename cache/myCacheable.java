package cpen221.mp3.cache;

import java.util.ArrayList;
import java.util.List;

public class myCacheable<V> implements Cacheable {

    private String id;
    private V content;

    // Abstraction Function:
    // myCacheable represents an object that is eligible to be stored in the
    // Cache.
    //    - id represents a unique Identification value that each Cacheable
    //      object has to have. This Identification value is the PageTitle of
    //      some webPage searched for or it can be the searched query.
    //    - content represents any type of information that some myCacheable
    //      object has to hold.

    // Representation Invariant:
    //    - id != null
    //    - content != null

    public myCacheable(String id, V content) {
        this.id = id;
        this.content = content;
    }

    // ID will be the pageTitle
    public String id() {
        return this.id;
    }

    public V content() {
        return this.content;
    }





}
