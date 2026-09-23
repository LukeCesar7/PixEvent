package com.pixevent.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StatusMesaConverter implements AttributeConverter<StatusMesa, String> {

    @Override
    public String convertToDatabaseColumn(StatusMesa attribute) {
        return attribute == null ? null : attribute.getValor();
    }

    @Override
    public StatusMesa convertToEntityAttribute(String dbData) {
        return dbData == null ? null : StatusMesa.fromValor(dbData);
    }
}
