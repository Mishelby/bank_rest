package com.example.bankcards.service;

import com.example.bankcards.dto.SpecificationData;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.RepositoryHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.example.bankcards.util.RepositoryHelper.getPageableSortingByAscID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;
    private final RepositoryHelper repositoryHelper;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(int page,
                                     int size,
                                     Boolean enabled,
                                     LocalDateTime createdDate) {
        var pageable = getPageableSortingByAscID(page, size);
        SpecificationData specificationData = SpecificationData.builder()
                .enabled(enabled)
                .createdDate(createdDate)
                .build();

        Specification<UserEntity> specificationWithParams
                = repositoryHelper.getSpecificationWithParams(specificationData);

        Page<UserEntity> allUsers = userRepository.findAll(specificationWithParams, pageable);

        if (allUsers.isEmpty()) {
            return Collections.emptyList();
        }

        return allUsers.getContent()
                .stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long userID) {
        return userMapper.toUserDto(
                repositoryHelper.findUserEntityByID(userID)
        );
    }
}
