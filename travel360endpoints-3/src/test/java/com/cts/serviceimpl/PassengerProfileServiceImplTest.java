// package com.cts.serviceimpl;

// import com.cts.config.AuthenticatedUserProvider;
// import com.cts.dto.PassengerProfileRequestDTO;
// import com.cts.dto.PassengerProfileResponseDTO;
// import com.cts.entity.PassengerProfile;
// import com.cts.entity.User;
// import com.cts.enums.Gender;
// import com.cts.enums.IdentificationType;
// import com.cts.enums.Nationality;
// import com.cts.exception.InvalidPassengerException;
// import com.cts.mapper.PassengerProfileMapper;
// import com.cts.repository.PassengerProfileRepository;
// import com.cts.repository.UserRepository;
// import com.cts.service.AuditLogService;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;

// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.time.LocalDate;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class PassengerProfileServiceImplTest {

//     @Mock
//     private PassengerProfileRepository profileRepo;

//     @Mock
//     private AuthenticatedUserProvider authUser;

//     @Mock
//     private AuditLogService auditLogService;

//     @Mock
//     private UserRepository userRepo;

//     private PassengerProfileServiceImpl service;

//     private User user;

//     @BeforeEach
//     void setup() {

//         user = new User();
//         user.setUserId(1L);
//         user.setEmail("test@mail.com");

//         service = new PassengerProfileServiceImpl(
//                 profileRepo,
//                 authUser,
//                 auditLogService,
//                 new PassengerProfileMapper(),
//                 userRepo);
//     }

//     private PassengerProfileRequestDTO validIndianAadhaar() {

//         PassengerProfileRequestDTO dto = new PassengerProfileRequestDTO();
//         dto.setPassengerName("John");
//         dto.setDateOfBirth(LocalDate.of(1995, 1, 1));
//         dto.setGender(Gender.MALE);
//         dto.setContactNo("9876543210");
//         dto.setEmailAddress("john@mail.com");
//         dto.setNationality(Nationality.INDIAN);
//         dto.setIdentificationType(IdentificationType.AADHAAR);
//         dto.setIdentificationNumber("123456789012");

//         return dto;
//     }

//     @Test
//     void createProfile_validIndianAadhaar_succeeds() {

//         PassengerProfileRequestDTO dto = validIndianAadhaar();

//         when(authUser.current()).thenReturn(user);
//         when(authUser.currentOrNull()).thenReturn(user);

//         when(profileRepo.save(any(PassengerProfile.class)))
//                 .thenAnswer(invocation -> {
//                     PassengerProfile profile = invocation.getArgument(0);
//                     profile.setPassengerProfileId(1L);
//                     return profile;
//                 });

//         PassengerProfileResponseDTO response = service.createProfile(dto);

//         assertNotNull(response);
//         assertEquals("John", response.getPassengerName());
//         assertEquals(IdentificationType.AADHAAR, response.getIdentificationType());
//     }

//     @Test
//     void createProfile_foreignWithPassport_succeeds() {

//         PassengerProfileRequestDTO dto = validIndianAadhaar();
//         dto.setNationality(Nationality.FOREIGN);
//         dto.setIdentificationType(IdentificationType.PASSPORT);
//         dto.setIdentificationNumber("A1234567");

//         when(authUser.current()).thenReturn(user);
//         when(authUser.currentOrNull()).thenReturn(user);

//         when(profileRepo.save(any(PassengerProfile.class)))
//                 .thenAnswer(invocation -> invocation.getArgument(0));

//         PassengerProfileResponseDTO response = service.createProfile(dto);

//         assertNotNull(response);
//     }

//     @Test
//     void createProfile_foreignWithAadhaar_throwsAndDoesNotSave() {

//         PassengerProfileRequestDTO dto = validIndianAadhaar();
//         dto.setNationality(Nationality.FOREIGN);

//         when(authUser.current()).thenReturn(user);

//         InvalidPassengerException ex = assertThrows(
//                 InvalidPassengerException.class,
//                 () -> service.createProfile(dto));

//         assertEquals("identificationNumber", ex.getField());

//         verify(profileRepo, never()).save(any());
//     }

//     @Test
//     void createProfile_malformedPan_throwsAndDoesNotSave() {

//         PassengerProfileRequestDTO dto = validIndianAadhaar();
//         dto.setIdentificationType(IdentificationType.PAN);
//         dto.setIdentificationNumber("ABC123");

//         when(authUser.current()).thenReturn(user);

//         InvalidPassengerException ex = assertThrows(
//                 InvalidPassengerException.class,
//                 () -> service.createProfile(dto));

//         assertEquals("identificationNumber", ex.getField());

//         verify(profileRepo, never()).save(any());
//     }

//     @Test
//     void updateProfile_malformedNumber_throwsAndDoesNotSave() {

//         PassengerProfileRequestDTO dto = validIndianAadhaar();
//         dto.setIdentificationNumber("12345");

//         PassengerProfile profile = new PassengerProfile();
//         profile.setPassengerProfileId(7L);

//         when(authUser.current()).thenReturn(user);

//         when(profileRepo.findByPassengerProfileIdAndUserUserId(7L, 1L))
//                 .thenReturn(Optional.of(profile));

//         assertThrows(
//                 InvalidPassengerException.class,
//                 () -> service.updateProfile(7L, dto));

//         verify(profileRepo, never()).save(any());
//     }

//     @Test
//     void createProfile_withUserId_succeeds() {

//         PassengerProfileRequestDTO dto = validIndianAadhaar();
//         dto.setUserId(2L);

//         User targetUser = new User();
//         targetUser.setUserId(2L);
//         targetUser.setEmail("customer@mail.com");

//         when(userRepo.findById(2L))
//                 .thenReturn(Optional.of(targetUser));

//         when(authUser.currentOrNull()).thenReturn(user);

//         when(profileRepo.save(any(PassengerProfile.class)))
//                 .thenAnswer(invocation -> invocation.getArgument(0));

//         PassengerProfileResponseDTO response = service.createProfile(dto);

//         assertNotNull(response);

//         verify(userRepo).findById(2L);
//     }
// }