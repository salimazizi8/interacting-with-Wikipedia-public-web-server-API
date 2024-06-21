package cpen221.mp3.wikimediator;

import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.myCacheable;
import fastily.jwiki.core.Wiki;

import java.util.*;
import java.util.stream.Collectors;

public class WikiMediator {

    // a Cache that will be used throughout the life of the WikiMediator:
    Cache<myCacheable> cache = new Cache(CACHE_CAPACITY, CACHE_TIME_OUT);
    Wiki myWiki = new Wiki("en.wikipedia.org");
    // Used for Zeitgeist and Trending methods:
    Map<String, Integer> frequencyMap = new HashMap<>();
    Map<String, Long> timeMap = new HashMap<>();
    // Used for peakLoad30s method:
    List<Long> allTimeStamps = new ArrayList<>();
    private static final int CACHE_TIME_OUT = 43200;
    private static final int CACHE_CAPACITY = 256;
    private final long wikiMediatorInitialTime;
    private Object Executors;


    // Abstraction Function (what do all those variables do to make/contribute
    // to this abstract WikiMediator):
    //  WikiMediator represents a service for interacting with Wikipedia to obtain
    //  pages and to gain other information about some query.
    //     - cache represents a storage to help WikiMediator avoid accessing
    //       network resources excessively. cache stores any objects that
    //       have a unique Identification value.
    //     - myWiki represents the main entry point to the jwiki API, because
    //       WikiMediator uses jwiki API to interact with the wikipedia server.
    //     - frequencyMap keeps track of the number of times a query or
    //       a pageTitle was searched for using this WikiMediator service.
    //     - timeMap keeps track of the exact and the most updated time a
    //       query or pageTitle was searched for using this WikiMediator service.
    //     - allTimeStamps keeps track of the time each of the 6 basic page
    //       requests from this WikiMediator (simpleSearch, getPage,
    //       zeitgist, trending, peakLoad30) were requested.
    //     - CACHE_TIME_OUT represents the number of seconds before an item
    //       gets marked as stale and gets evicted from the cache. WikiMediator's
    //       cache evicts elements that have been in cache for more than
    //       12 hours (43200 seconds).
    //     - CACHE_CAPACITY represents the maximum number of pages that
    //       are stored inside the cache by the WikiMediator service. WikiMediator
    //       only allows 256 pages to be stored in its cache.
    //     - wikiMediatorInitialTime represents the exact time this WikiMediator
    //       service was requested/called for.


    // Rep Invariants:
    //   - The elements put into the cache should be of type Cacheable.
    //   - The domain defined inside the wiki's constructor has to be a
    //     well-defined and non-null string.
    //   - The key for frequencyMap != null
    //   - The value for frequencyMap > 0
    //   - The key for timeMap != null
    //   - The value for timeMap > 0
    //   - The elements of allTimeStamps are greater than zero
    //   - wikiMediatorInitialTime should correspond to the exact time of the
    //     CPU which this WikiMediator service was initialized/requested.


    // Negative integers not allowed.
    // the value for frequencyMap should not get disconnected from the actual
    // frequency increase.

    public WikiMediator() {
        this.wikiMediatorInitialTime = System.currentTimeMillis();
    }




    /*
        You must implement the methods with the exact signatures
        as provided in the statement for this mini-project.

        You must add method signatures even for the methods that you
        do not plan to implement. You should provide skeleton implementation
        for those methods, and the skeleton implementation could return
        values like null.
     */

    /**
     * Given a query, return up to limit page titles that
     * match the query string (per Wikipedia's search service).
     *
     * @param query a non-null string to search for in Wikipedia
     * @param limit sets the the maximum number of page titles to be returned
     * @return up to limit page titles that match the query. Returns an empty
     * list if the query is an empty string or if the limit is zero.
     */
    public List<String> simpleSearch(String query, int limit) {
        // for PeakLoad30s:
        this.allTimeStamps.add(System.currentTimeMillis());
        List<String> searchResult = new ArrayList<>();
        // Doing the following for "zeitgeist" and "getPage" method's sake:
        if (this.frequencyMap.containsKey(query)) {
            int numOfOccur = this.frequencyMap.get(query);
            this.frequencyMap.put(query, ++numOfOccur);
            this.timeMap.put(query, System.currentTimeMillis());
        } else {
            this.frequencyMap.put(query, 1);
            this.timeMap.put(query, System.currentTimeMillis());
        }

        // actual simpleSearch logic:
        if (query.equals("") | query.equals(" ") | limit == 0) {
            return new ArrayList<>();
        }
        searchResult = myWiki.allPages(query, false, false,
                limit, null);

        return List.copyOf(searchResult);
    }

    /**
     * Given a pageTitle, return the text associated with the
     * Wikipedia page that matches pageTitle.
     *
     * @requires pageTitle is a non-null String. The keyword
     * to search for in Wikipedia.
     * @effects returns the text associated with pageTitle. Returns an empty
     * string if the pageTitle is an empty string or if it is a space.
     */
    public String getPage(String pageTitle) {
        // for PeakLoad30s:
        this.allTimeStamps.add(System.currentTimeMillis());

        // Doing the following for "zeitgeist" and "getPage" method's sake:
        if (this.frequencyMap.containsKey(pageTitle)) {
            int numOfOccur = this.frequencyMap.get(pageTitle);
            this.frequencyMap.put(pageTitle, ++numOfOccur);
            this.timeMap.put(pageTitle, System.currentTimeMillis());
        } else {
            this.frequencyMap.put(pageTitle, 1);
            this.timeMap.put(pageTitle, System.currentTimeMillis());
        }

        // Zeroth step:
        if (pageTitle.equals("") | pageTitle.equals(" ")) {
            return "";
        }

        // First of all, check the Cache if we have the
        // text associated with the pageTitle:
        try {
            // Instance of any Generic type has to be non-generic:
            myCacheable<String> cachedElement = cache.get(pageTitle);
            return cachedElement.content();
        } catch (NoSuchElementException e) {
            // Now, since we haven't returned yet(and got an EXCEPTION, meaning
            // could not find the object inside the Cache), means
            // the argument "pageTitle" was not found in the Cache,
            // so we should add it into the Cache
            // so that the next time someone wants to have it, it should
            // be retrieved from the Cache:
            String resultFromWiki = myWiki.getPageText(pageTitle);
            // generate a unique ID for every cacheable object we put into the Cache:

            // The pageTitle is used as the ID for an object.
            this.cache.put(new myCacheable(pageTitle, resultFromWiki));

            return resultFromWiki;
        }
    }


    /** Return a list of page titles that can be reached by
     * following up to hops links starting with the page specified by pageTitle.
     *
     * @param pageTitle title of the starting page that will get us to other pages
     * @param hops number of links to be followed
     * @return list of page titles (links) that were reached by following hops
     * number of links. Returns a list containing only the pageTitle if there
     * are no links associated with pageTitle.
     **/
   public List<String> getConnectedPages(String pageTitle, int hops) {
       // for PeakLoad30s:
       this.allTimeStamps.add(System.currentTimeMillis());

       if (hops == 0 || myWiki.whatLinksHere(pageTitle).isEmpty()) {
           // fetching only the startingPage if hops = 0;
           List<String> listOfTitles = new ArrayList<>();
           listOfTitles.add(pageTitle);
           return listOfTitles;
       } else {
           return getConnectedPagesHelper(pageTitle, hops);
       }
   }


   /** Helper method for getConnectedPages method.
    *
    **/
   private List<String> getConnectedPagesHelper(String pageTitle, int hops) {
       // base case:
       if (hops == 0) {
           return new ArrayList<>();
       }

       // recursive case:
       List<String> returningResult = new ArrayList<>();
       List<String> beta = myWiki.whatLinksHere(pageTitle);
       returningResult.addAll(beta);
       for (String element: beta) {
           returningResult.addAll(getConnectedPagesHelper(element, hops - 1));
       }
       return returningResult;
   }



   /**
    Return the most common Strings used in simpleSearch
    and getPage requests, with items being sorted in non-increasing
    count order. When many requests have been made, return only limit items.
    *
    * @param limit represents the maximum number of elements to be returned
    * @return a list containing the most common Strings that are passed into the
    * simpleSearch and getPage methods. When many requests have been
    * made, returns the limit number of items with the highest number
    * of appearances in the searches. Returns an empty list if limit is zero.
    *
    **/

   public List<String> zeitgeist(int limit) {
       // for PeakLoad30s:
       this.allTimeStamps.add(System.currentTimeMillis());

       if (limit == 0) {
           return new ArrayList<>();
       }

       List<String> frequencyList = new ArrayList<>();
       Map<String, Integer> dummyFrequencyMap = new HashMap<>(this.frequencyMap);

       while (!dummyFrequencyMap.isEmpty()) {
           int initialMax = 0;
           String foundCommon = "";
           for (String element : dummyFrequencyMap.keySet()) {
               if (dummyFrequencyMap.get(element) > initialMax) {
                   initialMax = dummyFrequencyMap.get(element);
                   foundCommon = element;
               }
           }

           if (frequencyList.size() < limit) {
               frequencyList.add(foundCommon);
               dummyFrequencyMap.remove(foundCommon);
           } else {
               break;
           }
       }
       return frequencyList;
   }

   /** Return the most common Strings used in simpleSearch and getPage requests
    * in the last 30 seconds.
    *
    * @param limit is the number of items to be returned
    * if many requests have been made
    * @return a list containing the most frequent strings used in
    * simpleSearch and getPage requests. Returns an empty list if limit is zero.
    * */
   public List<String> trending(int limit) {
       // for PeakLoad30s:
       this.allTimeStamps.add(System.currentTimeMillis());

       if (limit == 0) {
           return new ArrayList<>();
       }
       List<String> resultFromZeitgeist = this.zeitgeist(limit);
       return resultFromZeitgeist.stream().filter( s ->
               (System.currentTimeMillis() - this.timeMap.get(s) <= 30000)).collect(Collectors.toList());
   }


   /** Finding the maximum number of requests seen in any 30-second window.
    *  The request count is to include all requests made using this
    *  class (WikiMediator class).
    *
    * @requires nothing
    * @return the maximum number of requests across all requests
    * (including this request) in WikiMediator.
    **/
   public int peakLoad30s() {
       // for PeakLoad30s:
       this.allTimeStamps.add(System.currentTimeMillis());

       List<Integer> countList = new ArrayList<>();
       // Starting Time: 0 seconds
       long window1= this.wikiMediatorInitialTime;
       int count = 0;
       long currentTime = System.currentTimeMillis();
       while (window1 + 30000 <= currentTime) {
           for (Long timeElement : this.allTimeStamps) {
               if (timeElement <= window1 + 30000 && timeElement >= window1) {
                   ++count;
               }
           }
           // now that you found number of requests in the very first time
           // interval (between 0s - 30 s), you should add that number into the
           // countList:
           countList.add(count);
           window1 = window1 + 1000; // increment by 1 second (== 1000 milliseconds)
       }

       int maxSoFar = 0;
       for (Integer countElement: countList) {
           if (countElement > maxSoFar) {
               maxSoFar = countElement;
           }
       }
       return maxSoFar;
   }

   public List<String> getPath(String startPage, String stopPage) {
       myRunnable2 runnableObject = new myRunnable2(startPage, stopPage);
       return runnableObject.kk();
   }

   public List<String> getPath_logic(String startPage, String stopPage, List<String> listOf) {
       List<String> path = new ArrayList<>();
       Stack<String> stack = new Stack<>();
       Set<String> discoveredSet = new HashSet<>();
       List<String> allVertices = myWiki.whatLinksHere(stopPage);
       stack.push(stopPage);
       path.add(stopPage);
       while (!stack.isEmpty()) {
           String v = stack.pop();
           if (startPage.equals(v)) {
               path.add(startPage);
           }
           allVertices = myWiki.whatLinksHere(v);
           if (!discoveredSet.contains(v)) {
               discoveredSet.add(v);
               for (String element: allVertices) {
                   stack.push(element);
               }
           }

       }
       Collections.sort(path, Collections.reverseOrder());
       return path;
   }

   /*
   public List<String> getPath_logic(String startPage, String stopPage, List<String> listOf) {
       if (startPage.equals(stopPage) | myWiki.whatLinksHere(startPage).isEmpty()) {
           listOf.add(startPage);
           return listOf;
       } else {
           int hop = 1;
           List<String> fd = getConnectedPages(stopPage, hop);
           while (!getConnectedPages(stopPage, hop).contains(startPage)) {
               ++hop;
           }
           System.out.println(hop);
           List<String> beta = myWiki.whatLinksHere(stopPage);
           getPathHelper(startPage, stopPage, listOf, hop);
           return listOf;
       }
   }

   private boolean getPathHelper(String currentPage, String finalPage, List<String> list, int radius) {
       boolean remove = true;
       List<String> neighbours =myWiki.whatLinksHere(currentPage);

       list.add(currentPage);
       if (neighbours.contains(finalPage)) {
           list.add(finalPage);
           return true;
       }
       if (radius == 0) {
           list.remove(currentPage);
           return false;
       }
       for (String element:neighbours ) {
           if (!list.contains(element)) {
               if (getPathHelper(element, finalPage, list, radius - 1)) {
                   remove = false;
               }
           }
       }
       if(remove){list.remove(currentPage);}
       return true;
   }

    */
}








/*
class CPENList<T, S extends Collection> {
    private T object;
    private S parameter;
    CPENList(T obj, S param) {
        object = obj;
        parameter = param;
    }

    S getParameter() {
        return parameter;
    }

    void addToParameter(S otherParam) {
        parameter.addAll(otherParam);
    }
}

class MainClass {
    public static void main() {
        CPENList<String, Double> myList = new CPENList<String, Double>("MP3", 4d);
        System.out.println(myList.getParameter());
    }
}

 */