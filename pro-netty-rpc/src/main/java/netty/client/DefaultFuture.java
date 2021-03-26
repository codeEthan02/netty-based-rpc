package netty.client;

import netty.util.Response;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultFuture {

    public final static ConcurrentHashMap<Long, DefaultFuture> allDefaultFuture = new ConcurrentHashMap<Long, DefaultFuture>();

    final Lock lock = new ReentrantLock();
    public Condition condition = lock.newCondition();

    private Response response;

    public static ConcurrentHashMap<Long, DefaultFuture> getAllDefaultFuture() {
        return allDefaultFuture;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public DefaultFuture(ClientRequest request) {
        allDefaultFuture.put(request.getId(), this);
    }

    //主线程获取数据，首先要等待结果
    public Response get() {
        lock.lock();
        try {
            while (!done()) {
                condition.await();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return this.response;
    }

    public static void recieve(Response response) {
        DefaultFuture defaultFuture = allDefaultFuture.get(response.getId());
        if (defaultFuture != null) {
            Lock lock = defaultFuture.lock;
            lock.lock();
            try {
                defaultFuture.setResponse(response);
                defaultFuture.condition.signal();
                allDefaultFuture.remove(defaultFuture);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }

        }
    }

    private boolean done() {
        return this.response != null;
    }
}
