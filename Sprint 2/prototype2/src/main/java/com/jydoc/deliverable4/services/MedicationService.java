package com.jydoc.deliverable4.services;

import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class MedicationService {
    public Collection<Object> getUserMedications(User user) {
        return Collections.singleton(false);
    }
}
