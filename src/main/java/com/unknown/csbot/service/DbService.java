package com.unknown.csbot.service;

import com.unknown.csbot.model.IdChangeData;
import com.unknown.csbot.model.InquiryData;
import com.unknown.csbot.model.RegistrationData;

public interface DbService {

    boolean saveRegistration(RegistrationData data);

    boolean saveIdChange(IdChangeData data);

    boolean saveInquiry(InquiryData data);
}
