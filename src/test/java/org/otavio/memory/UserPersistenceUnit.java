package org.otavio.memory;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserPersistenceUnit implements PersistenceUnit<Long, User> {

    private static final Function<Long, User> DATABASE_OPERATION = s -> {
        try {
            TimeUnit.SECONDS.sleep(1L);//database consult
        } catch (Exception exp) {
            exp.printStackTrace();//just to test, ignore it
        }

        return new User("user: " + s);
    };

    private static final Function<String , List<User>> DATABASE_QUERY = s -> {
        try {
            TimeUnit.SECONDS.sleep(1L);//database consult
        } catch (Exception exp) {
            exp.printStackTrace();//just to test, ignore it
        }
        Random random = new Random();
        return Stream.generate(random::nextInt)
                .limit(10)
                .map(i -> new User(" user:" + i)).collect(Collectors.toList());
    };


    private final Map<Long, User> cacheById;

    private final Map<String, List<User>> cacheByQuery;

    public UserPersistenceUnit() {
        this.cacheById = TTLCache.of(10L, TimeUnit.MILLISECONDS, DATABASE_OPERATION);
        this.cacheByQuery = TTLCache.of(10L, TimeUnit.MILLISECONDS, DATABASE_QUERY);
    }

    @Override
    public User findById(Long key) {
        return cacheById.get(key);
    }

    @Override
    public User deleteById(Long key) {
        //remove from database
        cacheById.remove(key);
        cacheByQuery.clear();
        return null;
    }

    @Override
    public List<User> query(String query) {
        return null;
    }
}
