// package com.cts.serviceimpl;

// import com.cts.config.JWTUtil;
// import com.cts.dto.UserDTO;
// import com.cts.entity.User;
// import com.cts.enums.Role;
// import com.cts.enums.UserStatus;
// import com.cts.exception.UserNotFoundException;
// import com.cts.repository.UserRepository;
// import com.cts.service.AuditLogService;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;

// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.Spy;
// import org.mockito.junit.jupiter.MockitoExtension;

// import com.cts.mapper.UserMapper;

// import org.springframework.security.crypto.password.PasswordEncoder;

// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;
// import static org.mockito.ArgumentMatchers.*;

// @ExtendWith(MockitoExtension.class)
// class UserServiceImplTest {

//     @Mock private UserRepository repo;
//     @Mock private PasswordEncoder encoder;
//     @Mock private JWTUtil jwtUtil;
//     @Mock private AuditLogService auditLogService;
//     @Spy private UserMapper userMapper = new UserMapper();

//     @InjectMocks
//     private UserServiceImpl service;

//     private UserDTO dto;
//     private User user;

//     @BeforeEach
//     void setup() {

//         dto = new UserDTO();
//         dto.setEmail("test@mail.com");
//         dto.setPassword("password123");
//         dto.setPhoneNo(9876543210L);

//         user = new User();
//         user.setUserId(1L);
//         user.setEmail("test@mail.com");
//         user.setPassword("encoded");
//         user.setRole(Role.CUSTOMER);
//         user.setStatus(UserStatus.ACTIVE);
//     }

//     // ✅ REGISTER
//     @Test
//     void register_success() {

//         when(encoder.encode(any())).thenReturn("encoded");
//         when(repo.save(any())).thenReturn(user);

//         assertNotNull(service.register(dto));
//     }

//     // ✅ LOGIN SUCCESS
//     @Test
//     void login_success() {

//         when(repo.findByEmail(any())).thenReturn(user);
//         when(encoder.matches(any(), any())).thenReturn(true);
//         when(jwtUtil.generateToken(any(), any(), any())).thenReturn("token");

//         assertNotNull(service.login("test@mail.com", "password123"));
//     }

//     // ✅ LOGIN FAIL (USER NULL)
//     @Test
//     void login_userNotFound() {

//         when(repo.findByEmail(any())).thenReturn(null);

//         assertThrows(UserNotFoundException.class,
//                 () -> service.login("test@mail.com", "password123"));
//     }

//     // ✅ LOGIN FAIL (WRONG PASSWORD)
//     @Test
//     void login_invalidPassword() {

//         when(repo.findByEmail(any())).thenReturn(user);
//         when(encoder.matches(any(), any())).thenReturn(false);

//         assertThrows(UserNotFoundException.class,
//                 () -> service.login("test@mail.com", "wrong"));
//     }

//     // ✅ GET ALL
//     @Test
//     void getAllUsers_withoutRoleFilter() {

//         when(repo.findAll()).thenReturn(List.of(user));

//         assertFalse(service.getAllUsers(null).isEmpty());
//     }
    
//     @Test
//     void getAllUsers_withRoleFilter() {

//         when(repo.findByRole(Role.CUSTOMER))
//                 .thenReturn(List.of(user));

//         assertFalse(service.getAllUsers(Role.CUSTOMER).isEmpty());

//         verify(repo).findByRole(Role.CUSTOMER);
//     }
//     // ✅ UPDATE STATUS
//     @Test
//     void updateUserStatus_success() {

//         when(repo.findById(1L)).thenReturn(Optional.of(user));
//         when(repo.save(any())).thenReturn(user);

//         assertNotNull(service.updateUserStatus(1L, UserStatus.SUSPENDED));
//     }
// }
