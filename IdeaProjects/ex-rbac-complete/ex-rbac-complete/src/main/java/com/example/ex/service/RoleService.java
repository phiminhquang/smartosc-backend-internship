package com.example.ex.service;

import com.example.ex.dto.request.RoleCreationRequest;
import com.example.ex.dto.response.RoleResponse;
import com.example.ex.exception.AppException;
import com.example.ex.exception.ErrorCode;
import com.example.ex.mapper.RoleMapper;
import com.example.ex.model.Roles;
import com.example.ex.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
// cái readonly này có cx đc ko có cx chẳng sao nos là để ns là cáinayfyf đọc th ko có chưc nnagw sửa
//Trans này là khi mà mik sợ nhỡ may bị lỗi thì cứ hoàn tác lại cho nó an toàn ưu tiên nên dùng
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        //chức năng là mik muốn get tất cả và nó trảveefeef toRoleRéponse
        return roleRepository.findAll()
                .stream()
                //cnay :: nó là viết tatws của lambda kiểu map(roleMapper -> toRoleResponse(roleMapper))
                // map là kiểu biến đổi dữ liệu
                .map(roleMapper::toRoleResponse)
                //toList bắt nó trả về dạng chuooix
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Integer id) {
        Roles role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        return roleMapper.toRoleResponse(role);
    }

    @Transactional
    public RoleResponse createRole(RoleCreationRequest request) {
        String normalizedName = request.getName().trim().toUpperCase();

        if (roleRepository.existsByName(normalizedName)) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }

        Roles role = roleMapper.toRole(request);
        role.setName(normalizedName);

        return roleMapper.toRoleResponse(roleRepository.save(role));
    }
}
