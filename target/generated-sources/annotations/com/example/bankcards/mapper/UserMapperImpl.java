package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-04T14:23:42+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toUserDto(UserEntity user) {
        if ( user == null ) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder();

        userDto.id( user.getId() );
        userDto.username( user.getUsername() );
        userDto.password( user.getPassword() );
        userDto.role( user.getRole() );
        userDto.enabled( user.isEnabled() );
        userDto.createdDate( user.getCreatedDate() );
        userDto.updatedDate( user.getUpdatedDate() );

        return userDto.build();
    }

    @Override
    public UserEntity toEntity(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        UserEntity.UserEntityBuilder userEntity = UserEntity.builder();

        userEntity.id( userDto.getId() );
        userEntity.username( userDto.getUsername() );
        userEntity.password( userDto.getPassword() );
        userEntity.role( userDto.getRole() );
        userEntity.enabled( userDto.isEnabled() );
        userEntity.createdDate( userDto.getCreatedDate() );
        userEntity.updatedDate( userDto.getUpdatedDate() );

        return userEntity.build();
    }
}
