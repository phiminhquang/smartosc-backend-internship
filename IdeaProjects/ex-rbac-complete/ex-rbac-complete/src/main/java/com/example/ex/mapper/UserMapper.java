package com.example.ex.mapper;

import com.example.ex.dto.request.UserCreationRequest;
import com.example.ex.dto.response.UserResponse;
import com.example.ex.model.Roles;
import com.example.ex.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.LinkedHashSet;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserCreationRequest request);
//expression khi mà mik ko muốn nó map trực tiếp mà bảo nó hãy chạy cái hàm ở trong đã lấy nó mà ma
// của mik 1 bên là dạng String 1 bên dạng Roles vì sao ko để 2 cái dạng Role luôn vì Role ca mik có nhiều thông tin ko muốn publix ra ngoài
    @Mapping(target = "roles", expression = "java(toRoleNames(user.getRoles()))")
    UserResponse toUserResponse(User user);
// bình thg interface ko neen có hàm có nội dung dko nma deufual này nó kgacws phục điều đó ngoaiuf ra nó còn có thể tự thêm nhungcw tk nào imp nó mà ko cần sủửa tg cái 1
//method dưới để chuuyeenr từ Roles sang string
    default Set<String> toRoleNames(Set<Roles> roles) {
        Set<String> names = new LinkedHashSet<>();
        if (roles != null) {
            for (Roles role : roles) {
                names.add(role.getName());
            }
        }
        return names;
    }
}
