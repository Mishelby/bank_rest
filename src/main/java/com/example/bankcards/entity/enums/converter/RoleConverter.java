package com.example.bankcards.entity.enums.converter;

import com.example.bankcards.entity.enums.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import static java.util.Objects.nonNull;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role role) {
        if(nonNull(role)){
            return role.name();
        }

        return null;
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if(nonNull(dbData)) {
            return Role.fromString(dbData);
        }

        return null;
    }
}
