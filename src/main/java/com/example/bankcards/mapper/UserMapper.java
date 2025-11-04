package com.example.bankcards.mapper;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.dto.UserDto;
import org.mapstruct.Builder;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface UserMapper {

    UserDto toUserDto(UserEntity user);

    @InheritInverseConfiguration
    UserEntity toEntity(UserDto userDto);
}
