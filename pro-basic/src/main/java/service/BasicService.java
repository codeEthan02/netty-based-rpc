package service;

import client.annotation.RemoteInvoke;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import user.model.User;
import user.remote.UserRemote;

@Service
public class BasicService {

    @RemoteInvoke
    private UserRemote userRemote;

    public void testSaveUser() {
        User u = new User();
        u.setId(1);
        u.setName("张三");
        Object response = userRemote.saveUser(u);
        System.out.println(JSONObject.toJSONString(response));
    }
}
