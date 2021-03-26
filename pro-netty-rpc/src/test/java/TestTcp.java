import netty.client.ClientRequest;
import netty.util.Response;
import netty.client.TcpClient;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestTcp {

//    @Test
//    public void testGetResponse() {
//        ClientRequest request = new ClientRequest();
//        request.setContent("测试Tcp长连接请求");
//        Response resp = TcpClient.send(request);
//        System.out.println(resp.getResult());
//    }
//
//    @Test
//    public void testSaveUser() {
//        ClientRequest request = new ClientRequest();
//        User u = new User();
//        u.setId(1);
//        u.setName("张三");
//        request.setCommand("user.controller.UserController.saveUser");
//        request.setContent(u);
//        Response resp = TcpClient.send(request);
//        System.out.println(resp.getResult());
//    }
//
//    @Test
//    public void testSaveUsers() {
//        ClientRequest request = new ClientRequest();
//        List<User> users = new ArrayList<User>();
//        User u = new User();
//        u.setId(1);
//        u.setName("张三");
//        users.add(u);
//        request.setCommand("user.controller.UserController.saveUsers");
//        request.setContent(users);
//        Response resp = TcpClient.send(request);
//        System.out.println(resp.getResult());
//    }
}
