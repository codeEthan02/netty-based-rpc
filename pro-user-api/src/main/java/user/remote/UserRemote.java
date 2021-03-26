package user.remote;

import user.model.User;

import java.util.List;

public interface UserRemote {

    Object saveUser(User user);

    Object saveUsers(List<User> users);
}
