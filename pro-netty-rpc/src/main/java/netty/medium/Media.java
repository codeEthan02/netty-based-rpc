package netty.medium;

import com.alibaba.fastjson.JSONObject;
import netty.handler.param.ServerRequest;
import netty.util.Response;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Media {
    public static Map<String, BeanMethod> beanMap;
    static {
        beanMap = new HashMap<String, BeanMethod>();
    }

    private static Media m = null;
    private Media() {

    }

    public static Media newInstance() {
        if (m == null) {
            m = new Media();
        }
        return m;
    }

    //反射处理业务
    public Response process(ServerRequest serverRequest) {

        Response result = null;

        try {
            String command = serverRequest.getCommand();
            BeanMethod beanMethod = beanMap.get(command);
            if (beanMethod == null) return null;

            Object bean = beanMethod.getBean();
            Method m = beanMethod.getMethod();
            Class<?> paramType = m.getParameterTypes()[0];
            Object content = serverRequest.getContent();

            Object args = JSONObject.parseObject(JSONObject.toJSONString(content), paramType);

            result = (Response) m.invoke(bean, args);
            result.setId(serverRequest.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
