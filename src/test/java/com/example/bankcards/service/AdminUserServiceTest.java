package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.CardStatusException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.RepositoryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static com.example.bankcards.entity.enums.CardStatus.ACTIVE;
import static com.example.bankcards.entity.enums.CardStatus.BLOCKED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {
    @Mock
    private RepositoryHelper repositoryHelper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminUserService adminUserService;

    private UserEntity userEntity;
    private UserDto userDto;
    private CardEntity activeCard;
    private CardEntity blockedCard;

    @BeforeEach
    void setup() {
        activeCard = CardEntity.builder()
                .id(1L)
                .cardStatus(ACTIVE)
                .build();

        blockedCard = CardEntity.builder()
                .id(2L)
                .cardStatus(BLOCKED)
                .build();

        userEntity = UserEntity.builder()
                .id(1L)
                .username("testuser")
                .cards(List.of(blockedCard))
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .build();
    }


    @Test
    void getAllUsers_shouldReturnEmptyList_whenNoUsersFound() {
        Page<UserEntity> emptyPage = Page.empty();
        when(repositoryHelper.getSpecificationWithParams(any())).thenReturn(mock(Specification.class));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        List<UserDto> result = adminUserService.getAllUsers(0, 10, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void getUserByID_shouldReturnUserDto() {
        when(repositoryHelper.findUserEntityByID(1L)).thenReturn(userEntity);
        when(userMapper.toUserDto(userEntity)).thenReturn(userDto);

        UserDto result = adminUserService.getUserByID(1L);

        assertNotNull(result);
        assertEquals(userDto, result);
    }

    @Test
    void deleteUserByID_shouldDeleteUser_whenNoActiveCards() {
        when(repositoryHelper.findUserEntityByID(1L)).thenReturn(userEntity);

        adminUserService.deleteUserByID(1L);

        verify(userRepository, times(1)).delete(userEntity);
    }

    @Test
    void deleteUserByID_shouldThrowException_whenUserHasActiveCards() {
        UserEntity userWithActiveCard = UserEntity.builder()
                .id(2L)
                .cards(List.of(activeCard))
                .build();

        when(repositoryHelper.findUserEntityByID(2L)).thenReturn(userWithActiveCard);

        CardStatusException exception = assertThrows(CardStatusException.class, () ->
                adminUserService.deleteUserByID(2L)
        );

        assertEquals("USER_HAS_CARDS", exception.getErrorCode());
        verify(userRepository, never()).delete(any(UserEntity.class));
    }
}

