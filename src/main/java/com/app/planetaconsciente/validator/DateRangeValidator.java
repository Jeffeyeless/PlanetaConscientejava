package com.app.planetaconsciente.validator;

import java.time.LocalDate;

public class DateRangeValidator {
    public static boolean esRangoValido(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) return true;
        return !desde.isAfter(hasta);
    }
}

