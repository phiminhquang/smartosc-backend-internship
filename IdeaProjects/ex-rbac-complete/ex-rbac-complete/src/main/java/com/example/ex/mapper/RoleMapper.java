package com.example.ex.mapper;

import com.example.ex.dto.request.RoleCreationRequest;
import com.example.ex.dto.response.RoleResponse;
import com.example.ex.model.Roles;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
//Mapstruct tự bt để gen ra 1 cái iml và 1 objcet cho spring quản lý sau kp new j car cư sthees gọi th
@Mapper(componentModel = "spring")
public interface RoleMapper {
//id kp đổi
    @Mapping(target = "id", ignore = true)
    //nhận vào 1 cái request tạo role và biến thành dạng Roles để lưu vào db
    Roles toRole(RoleCreationRequest request);
    //nhận vào 1 cái Roles từ db  và biến thành dạng để phản hồi lại
    RoleResponse toRoleResponse(Roles role);
}
