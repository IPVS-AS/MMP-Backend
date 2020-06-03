package de.unistuttgart.ipvs.as.mmp.common.repository;

import org.hibernate.search.bridge.StringBridge;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomDateBridge implements StringBridge {
    @Override
    public String objectToString(Object object) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        return object != null ? ((LocalDate) object).format(formatter) : null;
    }
}
