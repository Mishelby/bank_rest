package com.example.bankcards.mapper;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.dto.UserDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-01T11:38:42+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toUserDto(UserEntity user) {
        if ( user == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.setId( user.getId() );
        userDto.setUsername( user.getUsername() );
        userDto.setPassword( user.getPassword() );
        userDto.setRole( user.getRole() );
        userDto.setEnabled( user.isEnabled() );
        userDto.setCreatedDate( user.getCreatedDate() );
        userDto.setUpdatedDate( user.getUpdatedDate() );

        return userDto;
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
