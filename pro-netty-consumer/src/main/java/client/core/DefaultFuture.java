package client.core;

import client.param.ClientRequest;
import client.param.Response;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultFuture {

    public final static ConcurrentHashMap<Long, DefaultFuture> allDefaultFuture = new ConcurrentHashMap<Long, DefaultFuture>();

    final Lock lock = new ReentrantLock();
    public Condition condition = lock.newCondition();

    private Response response;
    private long timeout = 2 * 60 * 1000;
    private long startTime = System.currentTimeMillis();

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getStartTime() {
        return startTime;
    }

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

    public Response get(long time) {
        lock.lock();
        try {
            while (!done()) {
                condition.await(time, TimeUnit.MILLISECONDS);
                if (System.currentTimeMillis() - startTime > time) {
                    System.out.println("请求超时！");
                    break;
                }
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

    static class FutureThread extends Thread {
        @Override
        public void run() {
            Set<Long> ids = allDefaultFuture.keySet();
            for (Long id : ids) {
                DefaultFuture df = allDefaultFuture.get(id);
                if (null == df) allDefaultFuture.remove(df);
                else {
                    //假如链路超时
                    if (df.getTimeout() < System.currentTimeMillis() - df.getStartTime()) {
                        Response resp = new Response();
                        resp.setId(id);
                        resp.setCode("333333");
                        resp.setMsg("链路请求超时");
                        recieve(resp);
                    }
                }
            }
        }
    }

    static {
        FutureThread futureThread = new FutureThread();
        futureThread.setDaemon(true);
        futureThread.start();
    }
}
