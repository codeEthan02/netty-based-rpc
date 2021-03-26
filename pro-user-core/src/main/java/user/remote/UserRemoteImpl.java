package user.remote;

import netty.annotation.Remote;
import netty.util.ResponseUtil;
import user.model.User;
import user.service.UserService;

import javax.annotation.Resource;
import java.util.List;

@Remote
public class UserRemoteImpl implements UserRemote{

    @Resource
    private UserService userService;

    public Object saveUser(User user) {
        userService.save(user);
        return ResponseUtil.createSuccessResult(user);
    }

    public Object saveUsers(List<User> users) {
        userService.saveList(users);
        return ResponseUtil.createSuccessResult(users);
    }
}
